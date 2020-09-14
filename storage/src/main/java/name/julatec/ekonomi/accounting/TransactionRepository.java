package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TransactionRepository extends MultiTenantRepository<PaperVoucher, String, TransactionRepository> {

    @Query("select f from manual_transaction f where f.receptorNumero = :numero and f.fecha between :lower and :upper")
    List<PaperVoucher> searchByRepecetor(
            @Param("numero") String numero,
            @Param("lower") Date lower,
            @Param("upper") Date upper);

    @Query("select f from manual_transaction f where f.emisorNumero = :numero and f.fecha between :lower and :upper")
    List<PaperVoucher> searchByEmisor(
            @Param("numero") String numero,
            @Param("lower") Date lower,
            @Param("upper") Date upper);


}
