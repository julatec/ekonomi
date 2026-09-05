package name.julatec.ekonomi.tribunet;

import java.util.HashMap;
import java.util.Map;

/**
 * Tipos de documento de referencia (Nota 10, Anexos y Estructuras v4.4).
 * <p>
 * El anexo no lo deja a criterio: «para efectos de impresión y visualización se debe mostrar
 * la descripción del código». Un «01» suelto en la representación gráfica no cumple.
 * <p>
 * La tabla es la de la v4.4. Los comprobantes viejos que este sistema todavía lee pueden traer
 * códigos que ya no están —el 10 cambió de nombre, y versiones anteriores usaban otros—; por
 * eso {@link #descripcionDe(String)} devuelve el código tal cual cuando no lo reconoce, en vez
 * de inventarle una descripción o esconderlo.
 */
public enum TipoDocumentoReferencia {

    FACTURA_ELECTRONICA("01", "Factura electrónica"),
    NOTA_DEBITO("02", "Nota de débito electrónica"),
    NOTA_CREDITO("03", "Nota de crédito electrónica"),
    TIQUETE("04", "Tiquete electrónico"),
    NOTA_DESPACHO("05", "Nota de despacho"),
    CONTRATO("06", "Contrato"),
    PROCEDIMIENTO("07", "Procedimiento"),
    CONTINGENCIA("08", "Comprobante emitido en contingencia"),
    DEVOLUCION_MERCADERIA("09", "Devolución de mercadería"),
    RECHAZADO_POR_HACIENDA("10", "Comprobante electrónico rechazado por el Ministerio de Hacienda"),
    RECHAZADO_POR_RECEPTOR("11", "Sustituye factura rechazada por el receptor del comprobante"),
    SUSTITUYE_EXPORTACION("12", "Sustituye factura de exportación"),
    MES_VENCIDO("13", "Facturación mes vencido"),
    REGIMEN_ESPECIAL("14", "Comprobante aportado por contribuyente de régimen especial"),
    SUSTITUYE_FACTURA_COMPRA("15", "Sustituye una factura electrónica de compra"),
    PROVEEDOR_NO_DOMICILIADO("16", "Comprobante de proveedor no domiciliado"),
    NOTA_CREDITO_A_COMPRA("17", "Nota de crédito a factura electrónica de compra"),
    NOTA_DEBITO_A_COMPRA("18", "Nota de débito a factura electrónica de compra"),
    OTROS("99", "Otros");

    private static final Map<String, TipoDocumentoReferencia> PORCODIGO = new HashMap<>();

    static {
        for (TipoDocumentoReferencia valor : values()) {
            PORCODIGO.put(valor.codigo, valor);
        }
    }

    public final String codigo;

    public final String descripcion;

    TipoDocumentoReferencia(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public static TipoDocumentoReferencia deCodigo(String codigo) {
        return codigo == null ? null : PORCODIGO.get(codigo.trim());
    }

    /** La descripción oficial, o el código sin traducir si no está en la tabla. */
    public static String descripcionDe(String codigo) {
        final TipoDocumentoReferencia valor = deCodigo(codigo);
        return valor == null ? codigo : valor.descripcion;
    }
}
