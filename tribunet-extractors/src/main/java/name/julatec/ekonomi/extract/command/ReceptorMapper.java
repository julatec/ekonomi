package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.Identificacion;
import name.julatec.ekonomi.tribunet.Receptor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
public class ReceptorMapper extends BaseMapper<Receptor, name.julatec.ekonomi.tribunet.storage.Receptor> {

    @Override
    public name.julatec.ekonomi.tribunet.storage.Receptor
    of(Optional<name.julatec.ekonomi.tribunet.storage.Receptor> target, Receptor source) {
        return target.orElseGet(name.julatec.ekonomi.tribunet.storage.Receptor::new)
                .setNombre(source.getNombre())
                .setNumero(ofNullable(source.getIdentificacion())
                        .map(Identificacion::getNumero)
                        .orElse(null))
                .setTipo(ofNullable(source.getIdentificacion())
                        .map(Identificacion::getTipo)
                        .orElse(null))
                ;
    }
}
