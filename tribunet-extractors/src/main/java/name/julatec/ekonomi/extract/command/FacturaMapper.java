package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.Factura;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FacturaMapper extends BaseMapper<Factura, name.julatec.ekonomi.tribunet.storage.Factura> {

    private final DocumentoMapper documentoMapper;

    public FacturaMapper(DocumentoMapper documentoMapper) {
        this.documentoMapper = documentoMapper;
    }

    @Override
    public name.julatec.ekonomi.tribunet.storage.Factura of(
            Optional<name.julatec.ekonomi.tribunet.storage.Factura> target,
            Factura source) {
        name.julatec.ekonomi.tribunet.storage.Factura factura =
                target.orElseGet(name.julatec.ekonomi.tribunet.storage.Factura::new)
                .setClave(source.getClave())
                .setNumeroConsecutivo(source.getNumeroConsecutivo());
        return factura.setDocumento(documentoMapper.of(Optional.ofNullable(factura.getDocumento()), source));
    }
}
