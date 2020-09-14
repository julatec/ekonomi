package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.Emisor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EmisorMapper extends BaseMapper<Emisor, name.julatec.ekonomi.tribunet.storage.Emisor> {

    @Override
    public name.julatec.ekonomi.tribunet.storage.Emisor
    of(Optional<name.julatec.ekonomi.tribunet.storage.Emisor> target, Emisor source) {
        return target.orElseGet(name.julatec.ekonomi.tribunet.storage.Emisor::new)
                .setNombre(source.getNombre())
                .setNumero(source.getIdentificacion().getNumero())
                .setTipo(source.getIdentificacion().getTipo())
                ;
    }
}
