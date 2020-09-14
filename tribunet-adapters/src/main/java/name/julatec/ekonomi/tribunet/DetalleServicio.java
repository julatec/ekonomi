package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.util.stream.Stream;

/**
 * Document details adapter.
 */
@Adapt({
        cr.go.hacienda.tribunet.v42y2016.factura.FacturaElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.TiqueteElectronico.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.NotaCreditoElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.NotaDebitoElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2017.factura.FacturaElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.TiqueteElectronico.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.NotaCreditoElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.NotaDebitoElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.factura.FacturaElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.factura.compra.FacturaElectronicaCompra.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.FacturaElectronicaExportacion.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.tiquete.TiqueteElectronico.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.nota.credito.NotaCreditoElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.nota.debito.NotaDebitoElectronica.DetalleServicio.class,
})
public interface DetalleServicio {

    /***
     * Line details.
     * @return line details as Stream.
     */
    Stream<LineaDetalle> getLineaDetalle();

}
