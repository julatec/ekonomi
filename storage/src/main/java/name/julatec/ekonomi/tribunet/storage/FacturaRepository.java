package name.julatec.ekonomi.tribunet.storage;

import name.julatec.ekonomi.storage.MultiTenantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * Las contrapartes del tenant activo, paginadas y filtrables por nombre o cédula.
     * <p>
     * Reemplaza a {@link #getClients()}, que traía la tabla entera para de-duplicar en
     * memoria y por eso se recalculaba completa en cada login y cada cambio de tenant. Ver
     * {@link ClientesSql} para las tres diferencias de fondo: {@code union all}, agrupación
     * solo por cédula, y las cinco tablas en vez de solo {@code factura}.
     *
     * @param patron patrón de {@code like} ya armado; {@code %} para no filtrar.
     * @param desde  límite inferior del rango de la barra superior, inclusive.
     * @param hasta  límite superior, inclusive — tiene que venir ya al final del día
     *               ({@code Workspace.getDateInterval()} lo entrega así), no a medianoche.
     */
    @Query(value = ClientesSql.BUSCAR, countQuery = ClientesSql.CONTAR, nativeQuery = true)
    Page<ClienteProyeccion> buscarClientes(
            @Param("patron") String patron,
            @Param("desde") Date desde,
            @Param("hasta") Date hasta,
            Pageable pageable);

    /** Proyección de {@link #buscarClientes}: los alias del select, no columnas de una tabla. */
    interface ClienteProyeccion {
        String getNumero();

        Long getComprobantes();

        String getNombre();
    }

    @Query("select f.documento.emisor.numero from factura f where f.clave= :clave")
    String getEmisorByClave(@Param("clave") String clave);

    @Query("select f.documento.receptor.numero from factura f where f.clave= :clave")
    String getReceptorByClave(@Param("clave") String clave);

}
