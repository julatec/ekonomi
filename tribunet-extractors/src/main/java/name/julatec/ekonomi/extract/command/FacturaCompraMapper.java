package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.FacturaCompra;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@SuppressWarnings({"SpringJavaAutowiredFieldsWarningInspection", "SpringJavaInjectionPointsAutowiringInspection"})
@Service
public class FacturaCompraMapper extends BaseMapper<
        FacturaCompra,
        name.julatec.ekonomi.tribunet.storage.FacturaCompra> {

    @Autowired
    DocumentoMapper documentoMapper;

    @Override
    public name.julatec.ekonomi.tribunet.storage.FacturaCompra of(
            Optional<name.julatec.ekonomi.tribunet.storage.FacturaCompra> target,
            FacturaCompra source) {
        name.julatec.ekonomi.tribunet.storage.FacturaCompra factura =
                target.orElseGet(name.julatec.ekonomi.tribunet.storage.FacturaCompra::new)
                        .setClave(source.getClave())
                        .setNumeroConsecutivo(source.getNumeroConsecutivo());
        return factura.setDocumento(documentoMapper.of(Optional.ofNullable(factura.getDocumento()), source));
    }
}
