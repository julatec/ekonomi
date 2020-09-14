package name.julatec.ekonomi.tribunet.storage;

import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface FacturaRepository extends MultiTenantRepository<Factura, String, FacturaRepository> {

    @Query("select f from factura f where f.documento.receptor.numero= :numero and f.documento.fechaEmision between :lower and :upper")
    List<Factura> searchByRepecetor(
            @Param("numero") String numero,
            @Param("lower") Date lower,
            @Param("upper") Date upper);

    @Query("select f from factura f where f.documento.emisor.numero= :numero and f.documento.fechaEmision between :lower and :upper")
    List<Factura> searchByEmisor(
            @Param("numero") String numero,
            @Param("lower") Date lower,
            @Param("upper") Date upper);

    @Query(value = "select numero, sum(c) as c, nombre from\n" +
            "((select emisor_numero as numero, emisor_nombre as nombre, sum(1) as c\n" +
            "from factura\n" +
            "group by numero, nombre)\n" +
            "union\n" +
            "(select receptor_numero as numero, receptor_nombre as nombre, sum(1) as c\n" +
            "from factura\n" +
            "group by numero, nombre)) names1\n" +
            "where numero is not null and nombre is not null\n" +
            "group by numero, nombre" +
            ";", nativeQuery = true)
    List<Object[]> getClients();

    @Query("select f.documento.emisor.numero from factura f where f.clave= :clave")
    String getEmisorByClave(@Param("clave") String clave);

    @Query("select f.documento.receptor.numero from factura f where f.clave= :clave")
    String getReceptorByClave(@Param("clave") String clave);

}
