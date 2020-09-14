package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.Resumen;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class ResumenMapper extends BaseMapper<Resumen, name.julatec.ekonomi.tribunet.storage.Resumen> {
    @Override
    public name.julatec.ekonomi.tribunet.storage.Resumen
    of(Optional<name.julatec.ekonomi.tribunet.storage.Resumen> target, Resumen source) {
        return target.orElseGet(name.julatec.ekonomi.tribunet.storage.Resumen::new)
                .setTotalComprobante(source.getTotalComprobante())
                .setTotalImpuesto(source.getTotalImpuesto())
                .setTotalIVADevuelto(source.getTotalIVADevuelto())
                .setTotalGravado(source.getTotalGravado())
                .setTotalExento(source.getTotalExento())
                .setCodigoMoneda(source.getCodigoTipoMoneda().getCodigoMoneda())
                .setTipoCambio(source.getCodigoTipoMoneda().getTipoCambio())
                .setLastModified(new Date())
                ;
    }
}
