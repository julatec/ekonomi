package name.julatec.ekonomi.tribunet;

import java.util.Map;
import java.util.TreeMap;

public enum TipoDocumento {

    FACTURA("01", name.julatec.ekonomi.tribunet.Factura.class),
    NOTA_DEBITO("02", name.julatec.ekonomi.tribunet.NotaDebito.class),
    NOTA_CREDITO("03", name.julatec.ekonomi.tribunet.NotaCredito.class),
    TIQUETE("04", name.julatec.ekonomi.tribunet.Factura.class),
    CONFIRMACION_DE_ACEPTACION("05", name.julatec.ekonomi.tribunet.Factura.class),
    CONFIRMACION_DE_ACEPTACION_PARCIAL("06", name.julatec.ekonomi.tribunet.Factura.class),
    CONFIRMACION_DE_RECHAZO("07", name.julatec.ekonomi.tribunet.Factura.class),
    FACTURA_DE_COMPRA("08", name.julatec.ekonomi.tribunet.FacturaCompra.class),
    FACTURA_DE_EXPORTACION("09", name.julatec.ekonomi.tribunet.FacturaExportacion.class),
    DESCONOCIDO("99", name.julatec.ekonomi.tribunet.Factura.class);

    public final String code;
    public final Class<? extends Documento> targetClass;

    private static final Map<String, TipoDocumento> reverseMap;

    static {
        reverseMap = new TreeMap<>();
        for (TipoDocumento tipoDocumento : values()) {
            reverseMap.put(tipoDocumento.code, tipoDocumento);
        }
    }

    TipoDocumento(String code, Class<? extends Documento> targetClass) {
        this.code = code;
        this.targetClass = targetClass;
    }

    public static TipoDocumento fromCode(String code) {
        return reverseMap.getOrDefault(code, DESCONOCIDO);
    }

}