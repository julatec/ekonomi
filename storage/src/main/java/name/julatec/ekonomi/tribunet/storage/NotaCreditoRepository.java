package name.julatec.ekonomi.tribunet.storage;

import name.julatec.ekonomi.storage.MultiTenantRepository;
import name.julatec.util.algebraic.Interval;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;


public interface NotaCreditoRepository extends MultiTenantRepository<NotaCredito, ClaveNota, NotaCreditoRepository> {

    @Query("select f from nota_credito f " +
            "where (f.documento.receptor.numero= :numero or f.documento.emisor.numero= :numero) " +
            "and f.documento.fechaEmision between :lower and :upper")
    List<NotaCredito> search(
            @Param("numero") String numero,
            @Param("lower") Date lower,
            @Param("upper") Date upper);

    default Stream<NotaCredito> search(
            String numero,
            Interval<Date> interval) {
        return search(numero, interval.lower, interval.upper).stream();
    }
}
