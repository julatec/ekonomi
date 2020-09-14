package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.FacturaCompra;
import name.julatec.ekonomi.tribunet.storage.FacturaCompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
public class FacturaCompraCommand extends DocumentCommand {

    private final FacturaCompra factura;

    @Autowired
    FacturaCompraMapper mapper;

    @Autowired
    FacturaCompraRepository repository;

    public FacturaCompraCommand(Context<?> context, Document document, FacturaCompra factura) {
        super(context, document);
        this.factura = factura;
    }

    @Override
    public void run() {
        repository.forAll(getTenants(), repository -> {
            getLogger().info("[{}] processing", this.factura.getClave());
            final name.julatec.ekonomi.tribunet.storage.FacturaCompra factura = mapper.of(
                    repository.findById(this.factura.getClave()), this.factura);
            factura.getDocumento().setDocument(getStringFromDocument());
            repository.save(factura);
        });
    }

}
