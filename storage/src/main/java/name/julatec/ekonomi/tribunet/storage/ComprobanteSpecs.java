package name.julatec.ekonomi.tribunet.storage;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Filtros componibles sobre los comprobantes electrónicos.
 * <p>
 * Los cinco tipos —factura, factura de compra, factura de exportación, nota de
 * crédito y nota de débito— son entidades distintas en tablas distintas, pero
 * todas embeben el mismo {@link Documento}. Eso permite escribir un filtro una
 * sola vez y aplicarlo a las cinco, que es lo que hace {@code <T>} acá: no hay
 * superclase común, solo una forma común.
 * <p>
 * Las rutas a la clave y al consecutivo son la excepción: las facturas las
 * llevan sueltas ({@code @Id String clave}) y las notas dentro de un
 * {@link ClaveNota} embebido. {@link #clave(Root)} resuelve cuál corresponde
 * consultando el metamodelo, en vez de exigir dos versiones de cada filtro.
 */
public final class ComprobanteSpecs {

    private ComprobanteSpecs() {
    }

    private static <T> boolean llevaClaveEmbebida(Root<T> root) {
        return root.getModel().getAttributes().stream()
                .anyMatch(atributo -> "claveNota".equals(atributo.getName()));
    }

    private static <T> Path<String> clave(Root<T> root) {
        return llevaClaveEmbebida(root) ? root.get("claveNota").get("clave") : root.get("clave");
    }

    private static <T> Path<String> consecutivo(Root<T> root) {
        return llevaClaveEmbebida(root)
                ? root.get("claveNota").get("numeroConsecutivo")
                : root.get("numeroConsecutivo");
    }

    public static <T> Specification<T> clave(String valor) {
        return (root, query, cb) -> cb.equal(clave(root), valor);
    }

    public static <T> Specification<T> claveContiene(String fragmento) {
        return (root, query, cb) -> cb.like(clave(root), "%" + fragmento + "%");
    }

    public static <T> Specification<T> consecutivo(String valor) {
        return (root, query, cb) -> cb.equal(consecutivo(root), valor);
    }

    public static <T> Specification<T> emisorNumero(String numero) {
        return (root, query, cb) -> cb.equal(root.get("documento").get("emisor").get("numero"), numero);
    }

    public static <T> Specification<T> receptorNumero(String numero) {
        return (root, query, cb) -> cb.equal(root.get("documento").get("receptor").get("numero"), numero);
    }

    /** La cédula de cualquiera de las dos partes: quien emitió o quien recibió. */
    public static <T> Specification<T> parteNumero(String numero) {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("documento").get("emisor").get("numero"), numero),
                cb.equal(root.get("documento").get("receptor").get("numero"), numero));
    }

    private static Predicate contiene(
            jakarta.persistence.criteria.CriteriaBuilder cb, Path<String> campo, String fragmento) {
        return cb.like(cb.lower(campo), "%" + fragmento.toLowerCase() + "%");
    }

    public static <T> Specification<T> emisorNombreContiene(String fragmento) {
        return (root, query, cb) -> contiene(cb, root.get("documento").get("emisor").get("nombre"), fragmento);
    }

    public static <T> Specification<T> receptorNombreContiene(String fragmento) {
        return (root, query, cb) -> contiene(cb, root.get("documento").get("receptor").get("nombre"), fragmento);
    }

    /**
     * El nombre de cualquiera de las dos partes.
     * <p>
     * No hay índice sobre {@code emisor_nombre} ni {@code receptor_nombre}, y el
     * comodín inicial impide usarlo aunque lo hubiera: esto es un barrido de
     * tabla. Va bien en los volúmenes actuales; si algún día deja de irlo, el
     * arreglo es un índice de texto completo, no otro filtro.
     */
    public static <T> Specification<T> nombreContiene(String fragmento) {
        return (root, query, cb) -> cb.or(
                contiene(cb, root.get("documento").get("emisor").get("nombre"), fragmento),
                contiene(cb, root.get("documento").get("receptor").get("nombre"), fragmento));
    }

    public static <T> Specification<T> desde(Date lower) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("documento").get("fechaEmision"), lower);
    }

    public static <T> Specification<T> hasta(Date upper) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("documento").get("fechaEmision"), upper);
    }

    public static <T> Specification<T> montoDesde(BigDecimal minimo) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(
                root.get("documento").get("resumen").get("totalComprobante"), minimo);
    }

    public static <T> Specification<T> montoHasta(BigDecimal maximo) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(
                root.get("documento").get("resumen").get("totalComprobante"), maximo);
    }

    public static <T> Specification<T> moneda(String codigo) {
        return (root, query, cb) -> cb.equal(
                root.get("documento").get("resumen").get("codigoMoneda"), codigo);
    }

    /**
     * Combina los filtros no nulos con Y lógico.
     * <p>
     * Spring Data 4 dejó de aceptar {@code null} en {@code and()}, así que los
     * ausentes se descartan antes en vez de encadenarse.
     */
    @SafeVarargs
    public static <T> Specification<T> todos(Specification<T>... filtros) {
        final List<Specification<T>> presentes = new ArrayList<>();
        for (Specification<T> filtro : filtros) {
            if (filtro != null) {
                presentes.add(filtro);
            }
        }
        return presentes.isEmpty() ? Specification.unrestricted() : Specification.allOf(presentes);
    }
}
