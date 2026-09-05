package name.julatec.ekonomi.tribunet;

import java.util.HashMap;
import java.util.Map;

/**
 * Códigos de referencia (Nota 9, Anexos y Estructuras v4.4): por qué un comprobante apunta a
 * otro.
 * <p>
 * La tabla de la v4.4 no tiene 03. Los comprobantes v4.2 y v4.3 que este sistema sigue
 * leyendo sí lo traen, así que un código desconocido no es un error: {@link
 * #descripcionDe(String)} devuelve el código tal cual. Inventarle la descripción de otra
 * versión sería peor que mostrar el número.
 */
public enum CodigoReferencia {

    ANULA("01", "Anula documento de referencia"),
    CORRIGE_MONTO("02", "Corrige monto"),
    OTRO_DOCUMENTO("04", "Referencia a otro documento"),
    SUSTITUYE_PROVISIONAL("05", "Sustituye comprobante provisional por contingencia"),
    DEVOLUCION("06", "Devolución de mercancía"),
    SUSTITUYE_COMPROBANTE("07", "Sustituye comprobante electrónico"),
    ENDOSADA("08", "Factura endosada"),
    NOTA_CREDITO_FINANCIERA("09", "Nota de crédito financiera"),
    NOTA_DEBITO_FINANCIERA("10", "Nota de débito financiera"),
    PROVEEDOR_NO_DOMICILIADO("11", "Proveedor no domiciliado"),
    EXONERACION_POSTERIOR("12", "Crédito por exoneración posterior a la facturación"),
    OTROS("99", "Otros");

    private static final Map<String, CodigoReferencia> PORCODIGO = new HashMap<>();

    static {
        for (CodigoReferencia valor : values()) {
            PORCODIGO.put(valor.codigo, valor);
        }
    }

    public final String codigo;

    public final String descripcion;

    CodigoReferencia(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public static CodigoReferencia deCodigo(String codigo) {
        return codigo == null ? null : PORCODIGO.get(codigo.trim());
    }

    /** La descripción oficial, o el código sin traducir si no está en la tabla. */
    public static String descripcionDe(String codigo) {
        final CodigoReferencia valor = deCodigo(codigo);
        return valor == null ? codigo : valor.descripcion;
    }
}
