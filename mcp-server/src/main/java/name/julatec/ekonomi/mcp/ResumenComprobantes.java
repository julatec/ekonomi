package name.julatec.ekonomi.mcp;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import name.julatec.ekonomi.storage.StorageConfig;
import name.julatec.ekonomi.tribunet.storage.Factura;
import name.julatec.ekonomi.tribunet.storage.FacturaCompra;
import name.julatec.ekonomi.tribunet.storage.FacturaExportacion;
import name.julatec.ekonomi.tribunet.storage.NotaCredito;
import name.julatec.ekonomi.tribunet.storage.NotaDebito;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Conteos y sumas por tipo de comprobante y moneda.
 * <p>
 * Se agrupa por moneda y no se convierte a colones: el {@code tipo_cambio} está
 * guardado por documento, y sumar montos de monedas distintas —o convertirlos con
 * un tipo de cambio del día de hoy— daría una cifra que no cuadra con ningún
 * asiento. Quien necesite el consolidado tiene los componentes para armarlo.
 * <p>
 * Las notas de crédito se reportan aparte por la misma razón: restan, y netearlas
 * automáticamente escondería el criterio detrás de un solo número.
 */
@Service
public class ResumenComprobantes {

    private static final Map<String, Class<?>> ENTIDADES = new LinkedHashMap<>() {{
        put("factura", Factura.class);
        put("factura_compra", FacturaCompra.class);
        put("factura_exportacion", FacturaExportacion.class);
        put("nota_credito", NotaCredito.class);
        put("nota_debito", NotaDebito.class);
    }};

    /** La moneda que asume Hacienda cuando el comprobante no declara `CodigoTipoMoneda`. */
    private static final String MONEDA_LOCAL = "CRC";

    @PersistenceContext(unitName = StorageConfig.PERSISTENCE_UNIT)
    private EntityManager entityManager;

    public record Linea(
            String tipo,
            String moneda,
            long comprobantes,
            BigDecimal totalComprobante,
            BigDecimal totalImpuesto) {
    }

    private <T> List<Linea> agregar(String tipo, Class<T> entidad, FiltroComprobantes filtro) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Tuple> consulta = cb.createTupleQuery();
        final Root<T> root = consulta.from(entidad);

        final Path<String> codigoMoneda = root.get("documento").get("resumen").get("codigoMoneda");
        final Path<BigDecimal> total = root.get("documento").get("resumen").get("totalComprobante");
        final Path<BigDecimal> impuesto = root.get("documento").get("resumen").get("totalImpuesto");

        // `CodigoTipoMoneda` es opcional en el esquema de Hacienda y los comprobantes viejos
        // suelen no traerlo: la ausencia significa colones. Sin el coalesce el group by deja
        // un grupo aparte con la moneda en null, y la misma contabilidad aparece dos veces
        // bajo el mismo tipo —una fila "factura/CRC" y otra "factura/(nada)"— que quien lea
        // el resumen no tiene cómo saber que hay que sumar. Es la misma regla que ya aplica
        // `Factura.key` al elegir la moneda del registro.
        final Expression<String> moneda = cb.coalesce(codigoMoneda, cb.literal(MONEDA_LOCAL));

        consulta.multiselect(moneda, cb.count(root), cb.sum(total), cb.sum(impuesto)).groupBy(moneda);

        final Predicate predicado = filtro.<T>aSpecification().toPredicate(root, consulta, cb);
        if (predicado != null) {
            consulta.where(predicado);
        }

        final List<Linea> lineas = new ArrayList<>();
        for (Tuple fila : entityManager.createQuery(consulta).getResultList()) {
            lineas.add(new Linea(
                    tipo,
                    fila.get(0, String.class),
                    fila.get(1, Long.class),
                    fila.get(2, BigDecimal.class),
                    fila.get(3, BigDecimal.class)));
        }
        return lineas;
    }

    @Transactional(transactionManager = StorageConfig.TRANSACTION_MANAGER, readOnly = true)
    public List<Linea> resumen(FiltroComprobantes filtro, java.util.Set<String> tipos) {
        final List<Linea> lineas = new ArrayList<>();
        ENTIDADES.forEach((tipo, entidad) -> {
            if (tipos == null || tipos.isEmpty() || tipos.contains(tipo)) {
                lineas.addAll(agregar(tipo, entidad, filtro));
            }
        });
        return lineas;
    }
}
