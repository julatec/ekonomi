package name.julatec.ekonomi.accounting;

import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface VoucherRepository extends MultiTenantRepository<Voucher, String, VoucherRepository> {

    @Query("select f from manual_transaction f where f.receptorNumero = :numero and f.fecha between :lower and :upper")
    List<Voucher> searchByRepecetor(
            @Param("numero") String numero,
            @Param("lower") Date lower,
            @Param("upper") Date upper);

    @Query("select f from manual_transaction f where f.emisorNumero = :numero and f.fecha between :lower and :upper")
    List<Voucher> searchByEmisor(
            @Param("numero") String numero,
            @Param("lower") Date lower,
            @Param("upper") Date upper);


}
