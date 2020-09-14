package name.julatec.ekonomi.tribunet.storage;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Date;

@Embeddable
public class Resumen {

    @Column(name = "codigo_moneda")
    protected String codigoMoneda;

    @Column(name = "tipo_cambio")
    protected BigDecimal tipoCambio;

    @Column(name = "total_servicios_gravados")
    protected BigDecimal totalServGravados;

    @Column(name = "total_servicios_exentos")
    protected BigDecimal totalServExentos;

    @Column(name = "total_mercancias_gravadas")
    protected BigDecimal totalMercanciasGravadas;

    @Column(name = "total_mercancias_exentas")
    protected BigDecimal totalMercanciasExentas;

    @Column(name = "total_gravado")
    protected BigDecimal totalGravado;

    @Column(name = "total_exento")
    protected BigDecimal totalExento;

    @Column(name = "total_venta")
    protected BigDecimal totalVenta;

    @Column(name = "total_descuentos")
    protected BigDecimal totalDescuentos;

    @Column(name = "total_venta_neta")
    protected BigDecimal totalVentaNeta;

    @Column(name = "total_impuesto")
    protected BigDecimal totalImpuesto;

    @Column(name = "total_iva_devuelto")
    protected BigDecimal totalIVADevuelto;

    @Column(name = "total_otros_cargos")
    protected BigDecimal totalOtrosCargos;

    @Column(name = "total_comprobante")
    protected BigDecimal totalComprobante;

    @Column(name = "last_modified")
    protected Date lastModified;

    public String getCodigoMoneda() {
        return codigoMoneda;
    }

    public Resumen setCodigoMoneda(String codigoMoneda) {
        this.codigoMoneda = codigoMoneda;
        return this;
    }

    public BigDecimal getTipoCambio() {
        return tipoCambio;
    }

    public Resumen setTipoCambio(BigDecimal tipoCambio) {
        this.tipoCambio = tipoCambio;
        return this;
    }


    public BigDecimal getTotalServGravados() {
        return totalServGravados;
    }

    public Resumen setTotalServGravados(BigDecimal totalServGravados) {
        this.totalServGravados = totalServGravados;
        return this;
    }


    public BigDecimal getTotalServExentos() {
        return totalServExentos;
    }

    public Resumen setTotalServExentos(BigDecimal totalServExentos) {
        this.totalServExentos = totalServExentos;
        return this;
    }

    public BigDecimal getTotalMercanciasGravadas() {
        return totalMercanciasGravadas;
    }

    public Resumen setTotalMercanciasGravadas(BigDecimal totalMercanciasGravadas) {
        this.totalMercanciasGravadas = totalMercanciasGravadas;
        return this;
    }

    public BigDecimal getTotalMercanciasExentas() {
        return totalMercanciasExentas;
    }

    public Resumen setTotalMercanciasExentas(BigDecimal totalMercanciasExentas) {
        this.totalMercanciasExentas = totalMercanciasExentas;
        return this;
    }

    public BigDecimal getTotalGravado() {
        return totalGravado;
    }

    public Resumen setTotalGravado(BigDecimal totalGravado) {
        this.totalGravado = totalGravado;
        return this;
    }

    public BigDecimal getTotalExento() {
        return totalExento;
    }

    public Resumen setTotalExento(BigDecimal totalExento) {
        this.totalExento = totalExento;
        return this;
    }


    public BigDecimal getTotalVenta() {
        return totalVenta;
    }

    public Resumen setTotalVenta(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
        return this;
    }


    public BigDecimal getTotalDescuentos() {
        return totalDescuentos;
    }

    public Resumen setTotalDescuentos(BigDecimal totalDescuentos) {
        this.totalDescuentos = totalDescuentos;
        return this;
    }


    public BigDecimal getTotalVentaNeta() {
        return totalVentaNeta;
    }

    public Resumen setTotalVentaNeta(BigDecimal totalVentaNeta) {
        this.totalVentaNeta = totalVentaNeta;
        return this;
    }


    public BigDecimal getTotalImpuesto() {
        return totalImpuesto;
    }

    public Resumen setTotalImpuesto(BigDecimal totalImpuesto) {
        this.totalImpuesto = totalImpuesto;
        return this;
    }


    public BigDecimal getTotalIVADevuelto() {
        return totalIVADevuelto;
    }

    public Resumen setTotalIVADevuelto(BigDecimal totalIVADevuelto) {
        this.totalIVADevuelto = totalIVADevuelto;
        return this;
    }

    public BigDecimal getTotalOtrosCargos() {
        return totalOtrosCargos;
    }

    public Resumen setTotalOtrosCargos(BigDecimal totalOtrosCargos) {
        this.totalOtrosCargos = totalOtrosCargos;
        return this;
    }


    public BigDecimal getTotalComprobante() {
        return totalComprobante;
    }

    public Resumen setTotalComprobante(BigDecimal totalComprobante) {
        this.totalComprobante = totalComprobante;
        return this;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public Resumen setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Resumen resumen = (Resumen) o;

        return new EqualsBuilder()
                .append(codigoMoneda, resumen.codigoMoneda)
                .append(tipoCambio, resumen.tipoCambio)
                .append(totalServGravados, resumen.totalServGravados)
                .append(totalServExentos, resumen.totalServExentos)
                .append(totalMercanciasGravadas, resumen.totalMercanciasGravadas)
                .append(totalMercanciasExentas, resumen.totalMercanciasExentas)
                .append(totalGravado, resumen.totalGravado)
                .append(totalExento, resumen.totalExento)
                .append(totalVenta, resumen.totalVenta)
                .append(totalDescuentos, resumen.totalDescuentos)
                .append(totalVentaNeta, resumen.totalVentaNeta)
                .append(totalImpuesto, resumen.totalImpuesto)
                .append(totalIVADevuelto, resumen.totalIVADevuelto)
                .append(totalOtrosCargos, resumen.totalOtrosCargos)
                .append(totalComprobante, resumen.totalComprobante)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(codigoMoneda)
                .append(tipoCambio)
                .append(totalServGravados)
                .append(totalServExentos)
                .append(totalMercanciasGravadas)
                .append(totalMercanciasExentas)
                .append(totalGravado)
                .append(totalExento)
                .append(totalVenta)
                .append(totalDescuentos)
                .append(totalVentaNeta)
                .append(totalImpuesto)
                .append(totalIVADevuelto)
                .append(totalOtrosCargos)
                .append(totalComprobante)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("codigoMoneda", codigoMoneda)
                .append("tipoCambio", tipoCambio)
                .append("totalServGravados", totalServGravados)
                .append("totalServExentos", totalServExentos)
                .append("totalMercanciasGravadas", totalMercanciasGravadas)
                .append("totalMercanciasExentas", totalMercanciasExentas)
                .append("totalGravado", totalGravado)
                .append("totalExento", totalExento)
                .append("totalVenta", totalVenta)
                .append("totalDescuentos", totalDescuentos)
                .append("totalVentaNeta", totalVentaNeta)
                .append("totalImpuesto", totalImpuesto)
                .append("totalIVADevuelto", totalIVADevuelto)
                .append("totalOtrosCargos", totalOtrosCargos)
                .append("totalComprobante", totalComprobante)
                .toString();
    }

}