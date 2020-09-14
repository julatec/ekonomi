package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;

@Adapt({
        cr.go.hacienda.tribunet.v42y2016.factura.FacturaElectronica.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.TiqueteElectronico.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.NotaCreditoElectronica.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.NotaDebitoElectronica.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v42y2017.factura.FacturaElectronica.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.TiqueteElectronico.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.NotaCreditoElectronica.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.NotaDebitoElectronica.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v43.factura.FacturaElectronica.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v43.factura.compra.FacturaElectronicaCompra.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.FacturaElectronicaExportacion.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v43.tiquete.TiqueteElectronico.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v43.nota.credito.NotaCreditoElectronica.InformacionReferencia.class,
        cr.go.hacienda.tribunet.v43.nota.debito.NotaDebitoElectronica.InformacionReferencia.class,
})
public interface InformacionReferencia {


    String getTipoDoc();

    default TipoDocumento getTipoDocumento() {
        return TipoDocumento.fromCode(getTipoDoc());
    }

    String getNumero();

    XMLGregorianCalendar getFechaEmision();

    default Date getFechaEmisionAsDate() {
        return getFechaEmision().toGregorianCalendar().getTime();
    }

    String getCodigo();

    String getRazon();


}
