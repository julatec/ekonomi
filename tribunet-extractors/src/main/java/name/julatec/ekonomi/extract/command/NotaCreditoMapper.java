package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.storage.ClaveNota;
import name.julatec.ekonomi.tribunet.NotaCredito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NotaCreditoMapper extends BaseMapper<NotaCredito, name.julatec.ekonomi.tribunet.storage.NotaCredito> {

    @Autowired
    DocumentoMapper documentoMapper;

    @Override
    public name.julatec.ekonomi.tribunet.storage.NotaCredito
    of(Optional<name.julatec.ekonomi.tribunet.storage.NotaCredito> target, NotaCredito source) {
        final name.julatec.ekonomi.tribunet.storage.NotaCredito nota =
                target.orElseGet(name.julatec.ekonomi.tribunet.storage.NotaCredito::new)
                        .setClaveNota(new ClaveNota()
                                .setClave(source.getClave())
                                .setNumeroConsecutivo(source.getNumeroConsecutivo()));
        return nota.setDocumento(documentoMapper.of(Optional.ofNullable(nota.getDocumento()), source));
    }
}
