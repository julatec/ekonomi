package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.math.BigDecimal;

/**
 * Resumen adapter interface.
 */
@SuppressWarnings({"SpellCheckingInspection", "unused"})
@Adapt({
        cr.go.hacienda.tribunet.v42y2016.factura.FacturaElectronica.ResumenFactura.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.TiqueteElectronico.ResumenFactura.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.NotaCreditoElectronica.ResumenFactura.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.NotaDebitoElectronica.ResumenFactura.class,
        cr.go.hacienda.tribunet.v42y2017.factura.FacturaElectronica.ResumenFactura.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.TiqueteElectronico.ResumenFactura.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.NotaCreditoElectronica.ResumenFactura.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.NotaDebitoElectronica.ResumenFactura.class,
        cr.go.hacienda.tribunet.v43.factura.FacturaElectronica.ResumenFactura.class,
        cr.go.hacienda.tribunet.v43.factura.compra.FacturaElectronicaCompra.ResumenFactura.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.FacturaElectronicaExportacion.ResumenFactura.class,
        cr.go.hacienda.tribunet.v43.tiquete.TiqueteElectronico.ResumenFactura.class,
        cr.go.hacienda.tribunet.v43.nota.credito.NotaCreditoElectronica.ResumenFactura.class,
        cr.go.hacienda.tribunet.v43.nota.debito.NotaDebitoElectronica.ResumenFactura.class,
})
public interface Resumen extends CodigoTipoMoneda {

    /**
     * Currency code.
     *
     * @return currency code.
     */
    default String getCodigoMoneda() {
        return null;
    }

    /**
     * Exchange rate.
     *
     * @return exchange rate.
     */
    default BigDecimal getTipoCambio() {
        return null;
    }

    /**
     * Currency information.
     *
     * @return currency information.
     */
    default CodigoTipoMoneda getCodigoTipoMoneda() {
        return this;
    }

    /**
     * Taxed total services.
     *
     * @return taxed total services.
     */
    default BigDecimal getTotalServGravados() {
        return null;
    }

    /**
     * Free tax total services.
     *
     * @return free tax total services.
     */
    default BigDecimal getTotalServExentos() {
        return null;
    }

    /**
     * Taxed total merchandise.
     *
     * @return taxed total merchandise.
     */
    default BigDecimal getTotalMercanciasGravadas() {
        return null;
    }

    /**
     * Free tax total merchandise.
     *
     * @return free tax total merchandise.
     */
    default BigDecimal getTotalMercanciasExentas() {
        return null;
    }

    /**
     * Taxed total.
     *
     * @return taxed total.
     */
    default BigDecimal getTotalGravado() {
        return null;
    }

    /**
     * Free tax total.
     *
     * @return free tax total.
     */
    default BigDecimal getTotalExento() {
        return null;
    }

    /**
     * Total before discount.
     *
     * @return total before discount.
     */
    default BigDecimal getTotalVenta() {
        return null;
    }

    /**
     * Discount total.
     *
     * @return discount total.
     */
    default BigDecimal getTotalDescuentos() {
        return null;
    }

    /**
     * Total with discount.
     *
     * @return total with discount.
     */
    default BigDecimal getTotalVentaNeta() {
        return null;
    }

    /**
     * Tax total.
     *
     * @return tax toal
     */
    BigDecimal getTotalImpuesto();

    /**
     * Returned tax total.
     *
     * @return returned tax total.
     */
    default BigDecimal getTotalIVADevuelto() {
        return null;
    }

    /**
     * Other charges total.
     *
     * @return other charges total.
     */
    default BigDecimal getTotalOtrosCargos() {
        return null;
    }

    /**
     * Document total.
     *
     * @return document total.
     */
    BigDecimal getTotalComprobante();
}
