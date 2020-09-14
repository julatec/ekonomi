package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.NotaDebito;
import name.julatec.ekonomi.tribunet.storage.ClaveNota;
import name.julatec.ekonomi.tribunet.storage.NotaDebitoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import static name.julatec.ekonomi.extract.command.InboxCommand.EMAIL_ATTRIBUTE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class NotaDebitoCommand extends DocumentCommand {

    private final NotaDebito nota;

    @Autowired
    NotaDebitoMapper mapper;

    @Autowired
    NotaDebitoRepository repository;

    public NotaDebitoCommand(Context<?> context, Document document, NotaDebito nota) {
        super(context, document);
        this.nota = nota;
    }

    @Override
    public void run() {
        repository.forAll(getTenants(), repository -> {
            final ClaveNota claveNota = new ClaveNota()
                    .setClave(this.nota.getClave())
                    .setNumeroConsecutivo(this.nota.getNumeroConsecutivo());
            getLogger().info("[{}][{}] processing", context.getAttribute(EMAIL_ATTRIBUTE), claveNota.getClave());
            final name.julatec.ekonomi.tribunet.storage.NotaDebito nota = mapper.of(
                    repository.findById(claveNota), this.nota);
            nota.getDocumento().setDocument(getStringFromDocument());
            repository.save(nota);
        });
    }
}
