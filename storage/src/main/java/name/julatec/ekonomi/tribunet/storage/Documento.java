package name.julatec.ekonomi.tribunet.storage;

import org.apache.commons.lang3.builder.ToStringBuilder;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Lob;
import java.util.Date;

@Embeddable
public class Documento {

    @Column(name = "fecha_emision")
    private Date fechaEmision;

    @Embedded
    private Emisor emisor;

    @Embedded
    private Receptor receptor;

    @Embedded
    private Resumen resumen;

    @Lob
    @Column
    private String document;

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public Documento setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
        return this;
    }

    public Emisor getEmisor() {
        return emisor;
    }

    public Documento setEmisor(Emisor emisor) {
        this.emisor = emisor;
        return this;
    }

    public Receptor getReceptor() {
        return receptor;
    }

    public Documento setReceptor(Receptor receptor) {
        this.receptor = receptor;
        return this;
    }

    public Resumen getResumen() {
        return resumen;
    }

    public Documento setResumen(Resumen resumen) {
        this.resumen = resumen;
        return this;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("fechaEmision", fechaEmision)
                .append("emisor", emisor)
                .append("receptor", receptor)
                .toString();
    }

    public String getDocument() {
        return document;
    }

    public Documento setDocument(String document) {
        this.document = document;
        return this;
    }

}
