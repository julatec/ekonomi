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
                // Estas ocho columnas existen en la tabla desde siempre y nadie las
                // escribía: el comprobante traía el dato, la entidad tenía el campo y el
                // mapeo se saltaba la línea, así que quedaban en null sin que nada lo
                // dijera. No hay columna nueva acá — lo único que cambia es que de ahora
                // en adelante se llenan. Las filas viejas siguen en null; rellenarlas se
                // puede, porque el XML está guardado, pero es una migración aparte.
                .setTotalServGravados(source.getTotalServGravados())
                .setTotalServExentos(source.getTotalServExentos())
                .setTotalMercanciasGravadas(source.getTotalMercanciasGravadas())
                .setTotalMercanciasExentas(source.getTotalMercanciasExentas())
                .setTotalVenta(source.getTotalVenta())
                .setTotalDescuentos(source.getTotalDescuentos())
                .setTotalVentaNeta(source.getTotalVentaNeta())
                .setTotalOtrosCargos(source.getTotalOtrosCargos())
                .setCodigoMoneda(source.getCodigoTipoMoneda().getCodigoMoneda())
                .setTipoCambio(source.getCodigoTipoMoneda().getTipoCambio())
                .setLastModified(new Date())
                ;
    }
}
