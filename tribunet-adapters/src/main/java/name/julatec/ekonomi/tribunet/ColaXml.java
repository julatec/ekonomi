package name.julatec.ekonomi.tribunet;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Recorta lo que venga pegado DESPUÉS del cierre del elemento raíz de un XML.
 * <p>
 * Hay comprobantes que llegan con basura al final —otro documento concatenado, una firma
 * suelta, relleno de un relé de correo—. Xerces corta con «Content is not allowed in trailing
 * section» y el comprobante se descarta entero, aunque la parte válida esté completa y bien
 * formada. En producción eso dejó un mensaje reintentando cuatro veces al día desde el 30 set
 * 2026 sin entrar nunca.
 * <p>
 * Vive en su propia clase, y no dentro de quien parsea, porque el proyecto parsea comprobantes
 * en DOS lugares y el primer intento de arreglarlo tocó solo uno: {@link DocumentoAdapterService}
 * —que es por donde entran los documentos ya en memoria— y
 * {@code XmlAttachmentCommand} —que es por donde entran los adjuntos del correo, con su propio
 * {@code DocumentBuilderFactory}—. El arreglo sirve en el segundo tanto como en el primero.
 */
public final class ColaXml {

    private ColaXml() {
    }

    /**
     * Recorta lo que venga después del primer cierre del elemento raíz.
     * <p>
     * Trabaja sobre los BYTES y no sobre un {@code String}: el comprobante declara su propia
     * codificación y decodificarlo para volver a codificarlo lo corrompería. Los nombres de
     * elemento de los esquemas de Hacienda son ASCII, así que la secuencia {@code </Raiz>} son
     * los mismos bytes en UTF-8 y en ISO-8859-1 — basta con truncar el arreglo.
     * <p>
     * Se corta en el PRIMER cierre, no en el último: si lo que viene pegado es otro documento
     * con la misma raíz, cortar al final dejaría los dos y seguiría sin ser válido.
     *
     * @return el arreglo recortado, o {@code null} si no hay nada que recortar.
     */
    public static byte[] recortarTrasElCierreDeLaRaiz(byte[] bytes) {
        final String raiz = nombreDeLaRaiz(bytes);
        if (raiz == null) {
            return null;
        }
        final byte[] cierre = ("</" + raiz + ">").getBytes(StandardCharsets.US_ASCII);
        final int desde = indiceDe(bytes, cierre);
        if (desde < 0) {
            return null;
        }
        final int corte = desde + cierre.length;
        if (corte >= bytes.length || soloEspaciosDesde(bytes, corte)) {
            return null; // No sobra nada: el error es otro y recortar no ayudaría.
        }
        return Arrays.copyOf(bytes, corte);
    }

    /** Nombre del elemento raíz, saltándose prólogo, comentarios y DOCTYPE. */
    private static String nombreDeLaRaiz(byte[] bytes) {
        int i = 0;
        while (i < bytes.length) {
            if (bytes[i] != '<') {
                i++;
                continue;
            }
            if (i + 1 < bytes.length && (bytes[i + 1] == '?' || bytes[i + 1] == '!')) {
                i++; // Prólogo, comentario o DOCTYPE: no es la raíz.
                continue;
            }
            final int inicio = i + 1;
            int fin = inicio;
            while (fin < bytes.length && !esDelimitadorDeNombre(bytes[fin])) {
                fin++;
            }
            return fin > inicio ? new String(bytes, inicio, fin - inicio, StandardCharsets.US_ASCII) : null;
        }
        return null;
    }

    private static boolean esDelimitadorDeNombre(byte b) {
        return b == '>' || b == '/' || b == ' ' || b == '\t' || b == '\r' || b == '\n';
    }

    private static boolean soloEspaciosDesde(byte[] bytes, int desde) {
        for (int i = desde; i < bytes.length; i++) {
            final byte b = bytes[i];
            if (b != ' ' && b != '\t' && b != '\r' && b != '\n' && b != 0) {
                return false;
            }
        }
        return true;
    }

    private static int indiceDe(byte[] heno, byte[] aguja) {
        outer:
        for (int i = 0; i <= heno.length - aguja.length; i++) {
            for (int j = 0; j < aguja.length; j++) {
                if (heno[i + j] != aguja[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
}
