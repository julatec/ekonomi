package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.storage.Mensaje;
import name.julatec.ekonomi.tribunet.MensajeReceptor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MensajeReceptorMapper extends BaseMapper<MensajeReceptor, Mensaje> {
    @Override
    public Mensaje of(Optional<Mensaje> target, MensajeReceptor source) {
        return target.orElseGet(Mensaje::new)
                .setClave(source.getClave())
                .setEmisorNumero(source.getNumeroCedulaEmisor())
                .setReceptorNumero(source.getNumeroCedulaReceptor())
                .setReceptorFecha(source.getFechaEmisionDocAsDate())
                .setTotalImpuesto(source.getMontoTotalImpuesto())
                .setTotalComprobante(source.getTotalFactura())
                ;
    }
}
