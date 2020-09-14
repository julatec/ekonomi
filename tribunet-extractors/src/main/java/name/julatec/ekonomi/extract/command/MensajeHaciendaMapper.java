package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.storage.Mensaje;
import name.julatec.ekonomi.tribunet.MensajeHacienda;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MensajeHaciendaMapper extends BaseMapper<MensajeHacienda, Mensaje> {
    @Override
    public Mensaje of(Optional<Mensaje> target, MensajeHacienda source) {
        return target.orElseGet(Mensaje::new)
                .setClave(source.getClave())
                .setEmisorNumero(source.getNumeroCedulaEmisor())
                .setTotalImpuesto(source.getMontoTotalImpuesto())
                .setTotalComprobante(source.getTotalFactura())
                ;
    }
}
