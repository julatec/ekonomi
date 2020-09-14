package name.julatec.ekonomi.tribunet.storage;

import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface FacturaExportacionRepository extends MultiTenantRepository<FacturaExportacion, String, FacturaExportacionRepository> {

    @Query("select f from factura_exportacion f where f.documento.receptor.numero= :numero and f.documento.fechaEmision between :lower and :upper")
    List<FacturaCompra> searchByRepecetor(
            @Param("numero") String numero,
            @Param("lower") Date lower,
            @Param("upper") Date upper);

    @Query("select f from factura_exportacion f where f.documento.emisor.numero= :numero and f.documento.fechaEmision between :lower and :upper")
    List<FacturaCompra> searchByEmisor(
            @Param("numero") String numero,
            @Param("lower") Date lower,
            @Param("upper") Date upper);

}
