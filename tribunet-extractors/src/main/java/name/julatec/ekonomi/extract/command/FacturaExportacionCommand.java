package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.FacturaExportacion;
import name.julatec.ekonomi.tribunet.storage.FacturaExportacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import static name.julatec.ekonomi.extract.command.InboxCommand.EMAIL_ATTRIBUTE;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
 @SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class FacturaExportacionCommand extends DocumentCommand<FacturaExportacionCommand> {

    private final FacturaExportacion factura;

    @Autowired
    FacturaExportacionMapper mapper;

    @Autowired
    FacturaExportacionRepository repository;

    public FacturaExportacionCommand(Context<?> context, Document document, FacturaExportacion factura) {
        super(context, document);
        this.factura = factura;
    }

    @Override
    public void run() {
        repository.forAll(getTenants(), repository -> {
            getLogger().info("[{}][{}] processing", context.getAttribute(EMAIL_ATTRIBUTE), this.factura.getClave());
            final name.julatec.ekonomi.tribunet.storage.FacturaExportacion factura = mapper.of(
                    repository.findById(this.factura.getClave()), this.factura);
            factura.getDocumento().setDocument(getStringFromDocument());
            repository.save(factura);
        });
    }
}
