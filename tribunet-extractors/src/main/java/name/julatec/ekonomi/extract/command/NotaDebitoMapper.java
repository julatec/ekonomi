package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.storage.ClaveNota;
import name.julatec.ekonomi.tribunet.NotaDebito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotaDebitoMapper extends BaseMapper<NotaDebito, name.julatec.ekonomi.tribunet.storage.NotaDebito> {

    @Autowired
    DocumentoMapper documentoMapper;

    @Override
    public name.julatec.ekonomi.tribunet.storage.NotaDebito
    of(Optional<name.julatec.ekonomi.tribunet.storage.NotaDebito> target, NotaDebito source) {
        final name.julatec.ekonomi.tribunet.storage.NotaDebito nota =
                target.orElseGet(name.julatec.ekonomi.tribunet.storage.NotaDebito::new)
                        .setClaveNota(new ClaveNota()
                                .setClave(source.getClave())
                                .setNumeroConsecutivo(source.getNumeroConsecutivo()));
        return nota.setDocumento(documentoMapper.of(Optional.ofNullable(nota.getDocumento()), source));
    }
}
