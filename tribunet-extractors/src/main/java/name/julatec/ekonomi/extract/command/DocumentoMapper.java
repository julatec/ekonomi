package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.Documento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DocumentoMapper extends BaseMapper<
        Documento, name.julatec.ekonomi.tribunet.storage.Documento> {

    private final EmisorMapper emisorMapper;

    private final ReceptorMapper receptorMapper;

    private final ResumenMapper resumenMapper;

    public DocumentoMapper(
            EmisorMapper emisorMapper,
            ReceptorMapper receptorMapper,
            ResumenMapper resumenMapper) {
        this.emisorMapper = emisorMapper;
        this.receptorMapper = receptorMapper;
        this.resumenMapper = resumenMapper;
    }

    @Override
    public name.julatec.ekonomi.tribunet.storage.Documento
    of(Optional<name.julatec.ekonomi.tribunet.storage.Documento> target, Documento source) {
        return target.orElseGet(name.julatec.ekonomi.tribunet.storage.Documento::new)
                .setEmisor(emisorMapper.of(
                        target.map(name.julatec.ekonomi.tribunet.storage.Documento::getEmisor),
                        source.getEmisor()))
                .setReceptor(receptorMapper.of(
                        target.map(name.julatec.ekonomi.tribunet.storage.Documento::getReceptor),
                        source.getReceptor()))
                .setResumen(resumenMapper.of(
                        target.map(name.julatec.ekonomi.tribunet.storage.Documento::getResumen),
                        source.getResumenFactura()))
                .setFechaEmision(source.getFechaEmisionAsDate())
                // `Efectivo` resuelve v4.3 (un único campo) contra v4.4 (partido en dos): ver
                // el javadoc del adaptador. El receptor no tiene ese problema — existe solo
                // desde v4.4.
                .setCodigoActividadEmisor(source.getCodigoActividadEmisorEfectivo())
                .setCodigoActividadReceptor(source.getCodigoActividadReceptor())
                ;
    }
}
