package name.julatec.ekonomi.api;

import name.julatec.ekonomi.tribunet.Documento;
import name.julatec.ekonomi.tribunet.ExoneracionType;
import name.julatec.ekonomi.tribunet.FactorIVA;
import name.julatec.ekonomi.tribunet.ImpuestoType;
import name.julatec.ekonomi.tribunet.InformacionReferencia;
import name.julatec.ekonomi.tribunet.LineaDetalle;
import org.springframework.stereotype.Service;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Convierte el {@code Documento} adaptado —el que sale de re-parsear el XML— al DTO del
 * visualizador.
 * <p>
 * Se re-parsea el XML en vez de leer la base porque {@code storage} no persiste las líneas de
 * detalle ni las referencias en forma estructurada: de cada comprobante guarda las partes, el
 * resumen y el documento completo en una columna. Las líneas solo existen dentro de ese XML.
 * <p>
 * ⚠️ Varios getters del dominio devuelven {@code Stream<>}, que es de un solo uso. Acá se
 * materializan <b>una vez</b> con {@code toList()} apenas se obtienen: consumirlos dos veces
 * no falla al compilar, falla en ejecución y con un mensaje que no señala la causa.
 */
@Service
public class DetalleComprobanteMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private static String fecha(XMLGregorianCalendar valor) {
        return valor == null
                ? null
                : ISO.format(valor.toGregorianCalendar().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate());
    }

    private static DetalleComprobante.Parte parte(String nombre, String tipo, String numero) {
        return new DetalleComprobante.Parte(nombre, tipo, numero);
    }

    private DetalleComprobante.Resumen resumen(name.julatec.ekonomi.tribunet.Resumen resumen) {
        if (resumen == null) {
            return null;
        }
        return new DetalleComprobante.Resumen(
                resumen.getCodigoMoneda(),
                resumen.getTipoCambio(),
                resumen.getTotalGravado(),
                resumen.getTotalExento(),
                resumen.getTotalVenta(),
                resumen.getTotalDescuentos(),
                resumen.getTotalVentaNeta(),
                resumen.getTotalImpuesto(),
                resumen.getTotalOtrosCargos(),
                resumen.getTotalComprobante());
    }

    private DetalleComprobante.Exoneracion exoneracion(ExoneracionType exoneracion) {
        // El adaptador devuelve un objeto aunque el XML no traiga el nodo; lo que distingue
        // una exoneración real es que tenga número de documento.
        if (exoneracion == null || exoneracion.getNumeroDocumento() == null) {
            return null;
        }
        return new DetalleComprobante.Exoneracion(
                exoneracion.getTipoDocumento(),
                exoneracion.getNumeroDocumento(),
                exoneracion.getNombreInstitucion(),
                exoneracion.getPorcentajeExoneracion(),
                exoneracion.getMontoExoneracion());
    }

    private DetalleComprobante.Impuesto impuesto(ImpuestoType impuesto) {
        final String codigoTarifa = impuesto.getCodigoTarifaResuelto();
        final FactorIVA factor = FactorIVA.fromCodigo(codigoTarifa);
        return new DetalleComprobante.Impuesto(
                impuesto.getCodigo(),
                codigoTarifa,
                factor == null ? null : factor.descripcion,
                impuesto.getTarifa(),
                impuesto.getMonto(),
                exoneracion(impuesto.getExoneracion()));
    }

    private DetalleComprobante.Linea linea(LineaDetalle linea) {
        final List<ImpuestoType> impuestos = linea.getImpuesto().toList();
        return new DetalleComprobante.Linea(
                linea.getNumeroLinea(),
                linea.getDetalle(),
                linea.getCantidad(),
                linea.getPrecioUnitario(),
                linea.getMontoTotal(),
                linea.getSubTotal(),
                linea.getImpuestoNeto(),
                linea.getMontoTotalLinea(),
                impuestos.stream().map(this::impuesto).toList());
    }

    private DetalleComprobante.Referencia referencia(InformacionReferencia referencia) {
        return new DetalleComprobante.Referencia(
                referencia.getTipoDoc(),
                referencia.getNumero(),
                fecha(referencia.getFechaEmision()),
                referencia.getCodigo(),
                referencia.getRazon());
    }

    public DetalleComprobante de(String tenant, String tipo, Documento documento, String xml) {
        final List<LineaDetalle> lineas = Optional.ofNullable(documento.getDetalleServicio())
                .map(detalle -> detalle.getLineaDetalle().toList())
                .orElse(List.of());
        final List<InformacionReferencia> referencias =
                Optional.ofNullable(documento.getInformacionReferencia())
                        .map(java.util.stream.Stream::toList)
                        .orElse(List.of());

        return new DetalleComprobante(
                tenant,
                tipo,
                documento.getClave(),
                documento.getNumeroConsecutivo(),
                fecha(documento.getFechaEmision()),
                Optional.ofNullable(documento.getEmisor())
                        .map(e -> parte(e.getNombre(),
                                e.getIdentificacion() == null ? null : e.getIdentificacion().getTipo(),
                                e.getIdentificacion() == null ? null : e.getIdentificacion().getNumero()))
                        .orElse(null),
                Optional.ofNullable(documento.getReceptor())
                        .map(r -> parte(r.getNombre(),
                                r.getIdentificacion() == null ? null : r.getIdentificacion().getTipo(),
                                r.getIdentificacion() == null ? null : r.getIdentificacion().getNumero()))
                        .orElse(null),
                resumen(documento.getResumenFactura()),
                lineas.stream().map(this::linea).toList(),
                referencias.stream().map(this::referencia).toList(),
                xml);
    }
}
