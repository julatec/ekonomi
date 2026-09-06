package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.extract.ExtractExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.search.ReceivedDateTerm;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static jakarta.mail.search.ComparisonTerm.GE;
import static name.julatec.ekonomi.extract.command.InboxCommand.EMAIL_ATTRIBUTE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class FolderCommand extends BaseCommand<FolderCommand> {

    protected final Folder folder;

    /**
     * Se levanta cuando la conexión con el buzón se cae a mitad del lote.
     * <p>
     * Sin esto, cada mensaje que queda por procesar vuelve a intentar abrir el folder, falla, y
     * escribe su propia traza completa: el 5 de setiembre de 2026, con el correo del hosting
     * caído, fueron <b>1.553 trazas idénticas en una hora</b>. Es la misma razón por la que
     * {@code process} ya se rinde en silencio durante un redespliegue —«insistir acá es lo que
     * llenó catalina.out»—, aplicada al otro caso en que el lote está perdido de antemano.
     */
    private final AtomicBoolean conexionPerdida = new AtomicBoolean(false);

    private Environment environment;

    private ExtractExecutor extractExecutor;

    public FolderCommand(Context<?> context, Folder folder) {
        super(context);
        this.folder = folder;
    }

    private Stream<Message> getAllMessages() throws MessagingException {
        return Arrays.stream(folder.getMessages());
    }

    private Stream<Message> getMessagesSince(long units, TimeUnit timeUnit) throws MessagingException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, (int) timeUnit.toDays(units));
        final Date dateLimit = calendar.getTime();
        return Arrays.stream(folder.search(new ReceivedDateTerm(GE, dateLimit)))
                .filter(message -> {
                    final Date receivedDate;
                    try {
                        receivedDate = message.getReceivedDate();
                    } catch (MessagingException e) {
                        getLogger().error("[{}] Error reading messages", context.getAttribute(EMAIL_ATTRIBUTE), e);
                        return false;
                    }
                    if (receivedDate.before(dateLimit)) {
                        getLogger().info("[{}] Ignoring message from {} < {}",
                                context.getAttribute(EMAIL_ATTRIBUTE),
                                receivedDate,
                                dateLimit);
                        return false;
                    }
                    return true;
                });
    }

    /**
     * Reabre el folder si hiciera falta.
     *
     * <p>Sincronizado porque varias tareas del pool comparten este folder: sin esto dos hilos
     * pueden pasar el {@code isOpen()} a la vez y el segundo {@code open()} revienta.
     */
    private void ensureOpen() throws MessagingException {
        synchronized (folder) {
            if (!folder.isOpen()) {
                folder.open(Folder.READ_ONLY);
            }
        }
    }

    private void process(Message message) {
        // En un redespliegue el contexto se cierra con el lote a medias. Rendirse en silencio:
        // insistir acá es lo que llenó catalina.out con trazas de un classloader ya cerrado.
        if (extractExecutor.isShuttingDown()) {
            return;
        }
        // El buzón ya se cayó en este lote: los mensajes que faltan no se van a poder leer.
        if (conexionPerdida.get()) {
            return;
        }
        try {
            ensureOpen();
        } catch (MessagingException e) {
            // Una sola línea por lote, y con la causa. Que el resto se rinda callado no esconde
            // nada: el lote entero falló por lo mismo.
            if (conexionPerdida.compareAndSet(false, true)) {
                getLogger().error("[{}] Se perdió la conexión con el buzón; se abandona el lote",
                        context.getAttribute(EMAIL_ATTRIBUTE), e);
            }
            return;
        }
        try {
            final Date receivedDate = message.getReceivedDate();
            if (message.getContentType().contains("multipart")) {
                getLogger().info("[{}][{}/{}]: {}",
                        context.getAttribute(EMAIL_ATTRIBUTE),
                        receivedDate,
                        message.getMessageNumber(), message.getSubject());
                if (message.getSubject().contains("00200001010000015211")) {
                    getLogger().info("[{}] stop", context.getAttribute(EMAIL_ATTRIBUTE));
                }
                final MessageCommand command = commandFactory.getCommand(this, message);
                command.run();
            } else {
                getLogger().info("[{}] Ignoring message from {}", receivedDate, context.getAttribute(EMAIL_ATTRIBUTE));
            }
        } catch (Exception e) {
            getLogger().error("[{}] Error reading messages", context.getAttribute(EMAIL_ATTRIBUTE), e);
        }
    }

    /**
     * Ventana por omisión del perfil {@code production}, en días.
     * <p>
     * Eran <b>1</b>, y un día es poco: si la aplicación pasa una noche caída —el 5 de setiembre
     * de 2026 estuvo doce horas abajo porque la base del hosting no respondía— la primera
     * pasada al volver ya no alcanza a ver los correos del hueco, y esos comprobantes no se
     * vuelven a mirar nunca. La pasada es idempotente: un comprobante que ya está guardado se
     * reconoce por su clave, así que solapar cuesta tiempo, no correctitud.
     * <p>
     * Siete días cubren un fin de semana largo con la casa sin luz y siguen siendo ~500 mensajes
     * por buzón, contra los 3.138 de {@code month-import}.
     */
    private static final int VENTANA_PRODUCCION_POR_OMISION = 7;

    /**
     * Cuántos días atrás mirar en el buzón.
     * <p>
     * Configurable a propósito, y no una constante: el número correcto depende de cuánto
     * estuvo caído el servicio, que es justo lo que no se sabe de antemano. Poder subirlo desde
     * {@code catalina.properties} y reiniciar —sin recompilar ni desplegar— es la diferencia
     * entre recuperar una semana perdida en dos minutos o en una tarde. El perfil
     * {@code month-import} existía precisamente porque este número estaba clavado en el código.
     */
    int ventanaDeProduccion() {
        return environment.getProperty("ekonomi.extract.ventana-dias", Integer.class,
                VENTANA_PRODUCCION_POR_OMISION);
    }

    protected Stream<Message> getMessages() throws MessagingException {
        final Set<String> profiles = Set.of(environment.getActiveProfiles());
        return profiles.contains("production") ? getMessagesSince(-ventanaDeProduccion(), TimeUnit.DAYS) :
                profiles.contains("month-import") ? getMessagesSince(-45, TimeUnit.DAYS) :
                        profiles.contains("full-import") ? getAllMessages() :
                                Stream.empty();
    }


    @Override
    public void run() {
        try {
            folder.open(Folder.READ_ONLY);
            // Pool propio del webapp, NO ForkJoinPool.commonPool(): sus hilos sobreviven al
            // undeploy y se quedan girando contra un classloader muerto. Ver ExtractExecutor.
            // forEach() espera a que todo el lote termine, porque StoreCommand cierra el Store
            // apenas regresamos de acá.
            extractExecutor.forEach(getMessages(), this::process);
        } catch (MessagingException e) {
            getLogger().error("[{}] Error reading messages", context.getAttribute(EMAIL_ATTRIBUTE), e);
        } catch (Exception e) {
            getLogger().error("[{}] Error reading messages", context.getAttribute(EMAIL_ATTRIBUTE), e);
        }

    }


    @Autowired
    FolderCommand setEnvironment(Environment environment) {
        this.environment = environment;
        return this;
    }

    @Autowired
    FolderCommand setExtractExecutor(ExtractExecutor extractExecutor) {
        this.extractExecutor = extractExecutor;
        return this;
    }
}
