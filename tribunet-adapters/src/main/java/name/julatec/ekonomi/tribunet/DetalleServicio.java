package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.util.stream.Stream;

/**
 * Document details adapter.
 */
@Adapt({

        /*<Version 4.2 2016>*/

        cr.go.hacienda.tribunet.v42y2016.factura.FacturaElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.TiqueteElectronico.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.NotaCreditoElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.NotaDebitoElectronica.DetalleServicio.class,

        /*<Version 4.2 2017>*/

        cr.go.hacienda.tribunet.v42y2017.factura.FacturaElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.TiqueteElectronico.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.NotaCreditoElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.NotaDebitoElectronica.DetalleServicio.class,

        /*<Version 4.3>*/

        cr.go.hacienda.tribunet.v43.factura.FacturaElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.factura.compra.FacturaElectronicaCompra.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.FacturaElectronicaExportacion.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.tiquete.TiqueteElectronico.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.nota.credito.NotaCreditoElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v43.nota.debito.NotaDebitoElectronica.DetalleServicio.class,

        /*<Version 4.4>*/

        cr.go.hacienda.tribunet.v44.factura.FacturaElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v44.factura.compra.FacturaElectronicaCompra.DetalleServicio.class,
        cr.go.hacienda.tribunet.v44.factura.exportacion.FacturaElectronicaExportacion.DetalleServicio.class,
        cr.go.hacienda.tribunet.v44.tiquete.TiqueteElectronico.DetalleServicio.class,
        cr.go.hacienda.tribunet.v44.nota.credito.NotaCreditoElectronica.DetalleServicio.class,
        cr.go.hacienda.tribunet.v44.nota.debito.NotaDebitoElectronica.DetalleServicio.class,
})
public interface DetalleServicio {

    /***
     * Line details.
     * @return line details as Stream.
     */
    Stream<LineaDetalle> getLineaDetalle();

}
