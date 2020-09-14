package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.storage.Mensaje;
import name.julatec.ekonomi.tribunet.storage.MensajeRepository;
import name.julatec.ekonomi.tribunet.MensajeHacienda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import static name.julatec.ekonomi.extract.command.InboxCommand.EMAIL_ATTRIBUTE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class MensajeHaciendaCommand extends DocumentCommand<MensajeHaciendaCommand> {

    final MensajeHacienda mensaje;

    @Autowired
    MensajeHaciendaMapper mapper;

    @Autowired
    MensajeRepository repository;

    protected MensajeHaciendaCommand(Context<?> context, Document document, MensajeHacienda mensaje) {
        super(context, document);
        this.mensaje = mensaje;
    }

    @Override
    public void run() {
        repository.forAll(getTenants(), repository -> {
            getLogger().info("[{}][{}] processing", context.getAttribute(EMAIL_ATTRIBUTE), this.mensaje.getClave());
            final Mensaje mensaje = mapper.of(repository.findById(this.mensaje.getClave()), this.mensaje);
            mensaje.setMensajeHacienda(super.getStringFromDocument());
            repository.save(mensaje);
        });
    }
}
