package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.FacturaExportacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FacturaExportacionMapper extends BaseMapper<
        FacturaExportacion,
        name.julatec.ekonomi.tribunet.storage.FacturaExportacion> {

    private final DocumentoMapper documentoMapper;

    public FacturaExportacionMapper(DocumentoMapper documentoMapper) {
        this.documentoMapper = documentoMapper;
    }

    @Override
    public name.julatec.ekonomi.tribunet.storage.FacturaExportacion of(
            Optional<name.julatec.ekonomi.tribunet.storage.FacturaExportacion> target,
            FacturaExportacion source) {
        name.julatec.ekonomi.tribunet.storage.FacturaExportacion factura =
                target.orElseGet(name.julatec.ekonomi.tribunet.storage.FacturaExportacion::new)
                        .setClave(source.getClave())
                        .setNumeroConsecutivo(source.getNumeroConsecutivo());
        return factura.setDocumento(documentoMapper.of(Optional.ofNullable(factura.getDocumento()), source));
    }
}
