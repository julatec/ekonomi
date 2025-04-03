package name.julatec.ekonomi.tribunet;

import cr.go.hacienda.tribunet.v43.nota.credito.CodigoType;
import cr.go.hacienda.tribunet.v43.nota.credito.DescuentoType;
import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.stream.Stream;

/**
 * Impuesto type Interface Adapter.
 */
@Adapt({

        /* Verison 4.2 2016 */

        cr.go.hacienda.tribunet.v42y2016.factura.FacturaElectronica.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.TiqueteElectronico.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.NotaCreditoElectronica.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.NotaDebitoElectronica.DetalleServicio.LineaDetalle.class,

        /* Verison 4.2 2017 */

        cr.go.hacienda.tribunet.v42y2017.factura.FacturaElectronica.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.TiqueteElectronico.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.NotaCreditoElectronica.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.NotaDebitoElectronica.DetalleServicio.LineaDetalle.class,

        /* Verison 4.3 */

        cr.go.hacienda.tribunet.v43.factura.FacturaElectronica.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v43.factura.compra.FacturaElectronicaCompra.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.FacturaElectronicaExportacion.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v43.tiquete.TiqueteElectronico.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v43.nota.credito.NotaCreditoElectronica.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v43.nota.debito.NotaDebitoElectronica.DetalleServicio.LineaDetalle.class,

        /* Verison 4.4 */

        cr.go.hacienda.tribunet.v44.factura.FacturaElectronica.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v44.factura.compra.FacturaElectronicaCompra.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v44.factura.exportacion.FacturaElectronicaExportacion.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v44.tiquete.TiqueteElectronico.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v44.nota.credito.NotaCreditoElectronica.DetalleServicio.LineaDetalle.class,
        cr.go.hacienda.tribunet.v44.nota.debito.NotaDebitoElectronica.DetalleServicio.LineaDetalle.class,
})
public interface LineaDetalle {

    BigInteger getNumeroLinea();

    String getPartidaArancelaria();

    String getCodigo();

    Stream<CodigoType> getCodigoComercial();

    BigDecimal getCantidad();

    String getUnidadMedidad();

    String getDetalle();

    BigDecimal getPrecioUnitario();
    /**
     * Line total, before tax.
     *
     * @return line total.
     */
    BigDecimal getMontoTotal();

    Stream <DescuentoType> getDescuento();
    /**
     * Line subtotal.
     *
     * @return line subtotal.
     */
    BigDecimal getSubTotal();

    /**
     * Tax base.
     *
     * @return tax base.
     */

    default BigDecimal getBaseImponible() {
        return null;
    }

    /**
     * Taxes.
     *
     * @return taxes as stream.
     */
    Stream<ImpuestoType> getImpuesto();

    /**
     * Tax net.
     *
     * @return tax net.
     */
    default BigDecimal getImpuestoNeto() {
        return null;
    }

    /**
     * Line total after tax.
     *
     * @return line total after tax.
     */
    BigDecimal getMontoTotalLinea();

}
