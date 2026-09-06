package name.julatec.ekonomi.session;

import name.julatec.ekonomi.AppConfig;
import name.julatec.ekonomi.security.ImportBankTransaction;
import name.julatec.ekonomi.security.ImportTransaction;
import name.julatec.ekonomi.security.Inbox;
import name.julatec.ekonomi.security.User;
import name.julatec.ekonomi.storage.MultiTenantRepository;
import name.julatec.util.algebraic.Interval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class Workspace {

    public static final String Interval_LOWER_COOKIE = "rangeLower";
    public static final String Interval_UPPER_COOKIE = "rangeUpper";
    public static final String TENANT_COOKIE = "tenant";

    ResourceBundleMessageSource messages;

    final User user;
    private Locale locale;
    private Interval<Date> dateInterval = getDefaultDateInterval();
    private Map<UUID, ImportTransaction<?>> importTransactionMap;
    private Map<String, Set<String>> importTransactionMapByOwnerId = new HashMap<>();
    private String targetPersistanceUnit;

    /**
     * Rango por omisión cuando todavía no hay cookies de fecha: el mes en curso completo,
     * del día 1 al último, no "los últimos 3 meses" contados desde hoy.
     * <p>
     * Antes de esto, "desde"/"hasta" arrancaban recortados al día de hoy —el 6 de un mes de
     * 30 días mostraba 6 días, no el mes—, así que un usuario que entraba a media semana veía
     * una vista parcial sin haber tocado ningún filtro. Fijar el límite superior en el último
     * día del mes (23:59:59.999) es lo que hace que la vista por omisión sea "el mes completo"
     * y no "lo que va del mes".
     */
    public static Interval<Date> getDefaultDateInterval() {
        final Calendar inicio = Calendar.getInstance();
        inicio.set(Calendar.DAY_OF_MONTH, 1);
        inicio.set(Calendar.HOUR_OF_DAY, 0);
        inicio.set(Calendar.MINUTE, 0);
        inicio.set(Calendar.SECOND, 0);
        inicio.set(Calendar.MILLISECOND, 0);

        final Calendar fin = (Calendar) inicio.clone();
        fin.set(Calendar.DAY_OF_MONTH, fin.getActualMaximum(Calendar.DAY_OF_MONTH));
        fin.set(Calendar.HOUR_OF_DAY, 23);
        fin.set(Calendar.MINUTE, 59);
        fin.set(Calendar.SECOND, 59);
        fin.set(Calendar.MILLISECOND, 999);

        return Interval.of(inicio.getTime(), fin.getTime());
    }

    public String getTargetPersistanceUnit() {
        return targetPersistanceUnit;
    }

    public Workspace setTargetPersistanceUnit(String targetPersistanceUnit) {
        this.targetPersistanceUnit = targetPersistanceUnit;
        return this;
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Workspace(User user) {
        this.user = user;
        this.importTransactionMap = user.getImportBankAccounts()
                .stream()
                .collect(Collectors.toMap(ImportBankTransaction::getAccount, importBankAccount -> importBankAccount));
        user.getImportManualTransactions()
                .forEach(transaction -> this.importTransactionMap.put(transaction.getAccount(), transaction));
        user.getImportBankOperations()
                .forEach(transaction -> this.importTransactionMap.put(transaction.getAccount(), transaction));
        this.importTransactionMap.values().forEach(
                importTransaction -> {
                    importTransactionMapByOwnerId.putIfAbsent(importTransaction.getOwnerId(), new TreeSet<>());
                    importTransactionMapByOwnerId.get(importTransaction.getOwnerId()).add(importTransaction.getPersistenceUnit());
                });
    }

    private static Optional<Date> getDateFromIsoCookie(HttpServletRequest request, String cookieName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return Optional.ofNullable(WebUtils.getCookie(request, cookieName))
                .map(Cookie::getValue)
                .map(v -> v.substring(0, 10))
                .flatMap(source -> {
                    try {
                        return Optional.of(dateFormat.parse(source));
                    } catch (ParseException e) {
                        return Optional.empty();
                    }
                });
    }

    /**
     * El último instante del día de {@code medianoche} (23:59:59.999), no medianoche misma.
     * <p>
     * {@link #getDateFromIsoCookie} parsea "aaaa-mm-dd" a las 00:00:00 del día, correcto para
     * el límite inferior pero no para el superior: un {@code hasta} en medianoche deja fuera
     * de cualquier {@code BETWEEN}/{@code <=} directo contra esa fecha prácticamente todo el
     * día que la persona sí quiso incluir —lo mismo que ya resuelve
     * {@code FiltroComprobantes.fechaFinal} para el buscador de comprobantes, aplicado acá
     * para todo lo que usa {@link #getDateInterval()} directo: los reportes y el conteo de
     * comprobantes por contraparte.
     */
    private static Date finDelDia(Date medianoche) {
        return Date.from(medianoche.toInstant().atZone(ZoneId.systemDefault())
                .toLocalDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant());
    }

    /**
     * Determina el tenant activo a partir de la cookie del cliente.
     * <p>
     * La cookie la controla quien hace la petición, así que su valor solo se
     * acepta si está entre los datasources asignados al usuario. Sin ese filtro,
     * cambiar la cookie bastaba para leer la contabilidad de cualquier otro
     * tenant. Un valor ajeno o vencido cae al predeterminado en vez de fallar,
     * para que una cookie vieja no rompa la sesión.
     */
    private static String getTenantFromCookie(
            final HttpServletRequest request,
            final String cookieName,
            final Set<String> allowedTenants,
            final String defaultValue) {
        return Optional.ofNullable(WebUtils.getCookie(request, cookieName))
                .map(Cookie::getValue)
                .filter(allowedTenants::contains)
                .orElse(defaultValue);
    }

    public ImportTransaction<?> getImportBankAccount(UUID uuid) {
        return this.importTransactionMap.get(uuid);
    }

    public Interval<Date> getDateInterval() {
        return dateInterval;
    }

    public Session getSession() {
        return new Session()
                .setUsername(user.getDisplayName())
                .setLowerDate(dateInterval.lower)
                .setUpperDate(dateInterval.upper)
                .setTenants(new TreeSet<>(user.getDatasources()))
                .setTenant(targetPersistanceUnit)
                .setInboxes(user.getInboxes()
                        .stream()
                        .map(Inbox::getEmail)
                        .collect(toCollection(TreeSet::new)))
                .setImportAccounts(
                        this.importTransactionMap
                                .values()
                                .stream()
                                .map(Session.ImportAccount::of)
                                .collect(toCollection(TreeSet::new)));
    }

    public Workspace setRequest(HttpServletRequest request) {
        final Interval<Date> dateInterval = this.dateInterval;
        final Date lower = getDateFromIsoCookie(request, Interval_LOWER_COOKIE).orElse(dateInterval.lower);
        final Date upper = getDateFromIsoCookie(request, Interval_UPPER_COOKIE)
                .map(Workspace::finDelDia)
                .orElse(dateInterval.upper);
        final String tenant = getTenantFromCookie(
                request,
                TENANT_COOKIE,
                user.getDatasources(),
                user.getDatasources().iterator().next());
        // El tenant vive en un ThreadLocal y Tomcat reutiliza los hilos, así que
        // hay que fijarlo en cada petición, no solo cuando cambia.
        MultiTenantRepository.setCurrentDb(tenant);
        this.dateInterval = Interval.of(lower, upper);
        this.locale = request.getLocale();
        if (this.targetPersistanceUnit == null || !this.targetPersistanceUnit.equals(tenant)) {
            setTargetPersistanceUnit(tenant);
        }
        return this;
    }

    public Set<String> getBankPersistenceUnits(String id) {
        return importTransactionMapByOwnerId.getOrDefault(id, Collections.emptySet());
    }

    public <T extends Enum<T>> String getLocalizedMessage(T template, Object... args) {
        final String templateName = String.format("%s.%s", template.getClass().getName(), template.name());
        return messages.getMessage(templateName, args, locale);
    }

    @Autowired
    Workspace setMessages(
            @Qualifier(AppConfig.MESSAGES) ResourceBundleMessageSource messages) {
        this.messages = messages;
        return this;
    }
}
