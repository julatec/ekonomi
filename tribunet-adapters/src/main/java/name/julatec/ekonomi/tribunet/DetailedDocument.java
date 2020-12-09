package name.julatec.ekonomi.tribunet;

import name.julatec.util.collection.Bag;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.BigDecimal.ZERO;
import static java.util.Optional.ofNullable;
import static name.julatec.ekonomi.tribunet.FactorIVA.*;

/**
 * Decorator class for {@link Documento} including Tax calculations.
 */
public class DetailedDocument implements Documento {

    /**
     * Original Document.
     */
    private final Documento original;

    /**
     * Tax Calculations.
     */
    private final Bag<FactorIVA, TaxAccumulated> taxes;

    /**
     * Constructor with the Original Document and Tax Calculations.
     *
     * @param original original document.
     * @param taxes    tax calculations.
     */
    private DetailedDocument(Documento original, Bag<FactorIVA, TaxAccumulated> taxes) {
        this.original = original;
        this.taxes = taxes;
    }

    @Override
    public String getClave() {
        return original.getClave();
    }

    @Override
    public String getNumeroConsecutivo() {
        return original.getNumeroConsecutivo();
    }

    @Override
    public XMLGregorianCalendar getFechaEmision() {
        return original.getFechaEmision();
    }

    @Override
    public Emisor getEmisor() {
        return original.getEmisor();
    }

    @Override
    public Receptor getReceptor() {
        return original.getReceptor();
    }

    @Override
    public Resumen getResumenFactura() {
        return original.getResumenFactura();
    }

    @Override
    public DetalleServicio getDetalleServicio() {
        return original.getDetalleServicio();
    }

    @Override
    public Stream<InformacionReferencia> getInformacionReferencia() {
        return original.getInformacionReferencia();
    }

    /**
     * Gets the tax calculations.
     *
     * @return Tax Summary Information.
     */
    public Bag<FactorIVA, TaxAccumulated> getTaxes() {
        return taxes;
    }

    /**
     * Gets a new {@link DetailedDocument} from the given {@link Documento}.
     *
     * @param original Document to provide details.
     * @return a new {@link DetailedDocument} decorating the given original document with the Tax Information.
     */
    public static DetailedDocument of(Documento original) {
        return original
                .getDetalleServicio()
                .getLineaDetalle()
                .collect(new ImpuestoTypeCollector(original));
    }

    private static class ImpuestoTypeCollector implements Collector<
            LineaDetalle,
            Bag<FactorIVA, TaxAccumulated>,
            DetailedDocument> {

        private final Documento documento;

        public ImpuestoTypeCollector(Documento documento) {
            this.documento = documento;
        }

        @Override
        public Supplier<Bag<FactorIVA, TaxAccumulated>> supplier() {
            return () -> new Bag<>(TaxAccumulated::add);
        }

        @Override
        public BiConsumer<Bag<FactorIVA, TaxAccumulated>, LineaDetalle> accumulator() {
            return (buffer, linea) -> {
                FactorIVA factorIVA = Excento;
                BigDecimal baseImponible = linea.getSubTotal();
                BigDecimal impuesto = ZERO;
                for (ImpuestoType impuestoType : linea.getImpuesto().collect(Collectors.toList())) {
                    if (impuestoType.getExoneracion().getNumeroDocumento() != null) {
                        factorIVA = Exonerado;
                        buffer.add(Exonerado, new TaxAccumulated(
                                ZERO,
                                ZERO,
                                impuestoType.getMonto().subtract(impuestoType.getExoneracion().getMontoExoneracion())));
                    } else {
                        final FactorIVA impuestoFactor = Optional.ofNullable(
                                FactorIVA.valueOf(impuestoType.getTarifa()))
                                .orElse(Exonerado);
                        final ImpuestoType.Codigo codigo = ImpuestoType.Codigo.of(impuestoType.getCodigo());
                        if (codigo.compareTo(ImpuestoType.Codigo.BaseImponible) < 0) {
                            baseImponible = baseImponible.add(impuestoType.getMonto());
                        } else if (codigo.compareTo(ImpuestoType.Codigo.OtrosCargos) < 0) {
                            buffer.add(Otros, new TaxAccumulated(
                                    impuestoType.getMonto(),
                                    ZERO,
                                    ZERO));
                        } else {
                            impuesto = impuesto.add(impuestoType.getMonto());
                        }
                        if (factorIVA.compareTo(impuestoFactor) < 0) {
                            factorIVA = impuestoFactor;
                        }
                    }
                }
                switch (factorIVA) {
                    case Otros:
                    case Excento:
                        buffer.add(Excento, new TaxAccumulated(
                                baseImponible,
                                linea.getMontoTotalLinea(),
                                impuesto));
                        break;
                    case Exonerado:
                        buffer.add(Exonerado, new TaxAccumulated(
                                baseImponible,
                                linea.getMontoTotalLinea(),
                                impuesto));
                        break;
                    default:
                        buffer.add(factorIVA, new TaxAccumulated(
                                baseImponible,
                                linea.getMontoTotalLinea(),
                                impuesto));
                        break;
                }
            };
        }

        @Override
        public BinaryOperator<Bag<FactorIVA, TaxAccumulated>> combiner() {
            return Bag::merge;
        }

        @Override
        public Function<Bag<FactorIVA, TaxAccumulated>, DetailedDocument> finisher() {
            return taxes -> new DetailedDocument(documento, taxes);
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }

    /**
     * Summary for Tax Information.
     */
    public static final class TaxAccumulated {

        /**
         * Subtotal. The tax base used for tax calculation.
         */
        public final BigDecimal subTotal;

        /**
         * Total, includes the total and the taxes.
         */
        public final BigDecimal total;

        /**
         * Taxes.
         */
        public final BigDecimal taxed;

        /**
         * Default, empty value.
         */
        public static final TaxAccumulated empty = new TaxAccumulated(
                ZERO,
                ZERO,
                ZERO);

        /**
         * Constructor, the the three required values.
         *
         * @param subTotal sub total, the tax base
         * @param total    total including tax base and taxes.
         * @param taxed    taxes calculated.
         */
        public TaxAccumulated(BigDecimal subTotal, BigDecimal total, BigDecimal taxed) {
            this.subTotal = ofNullable(subTotal).orElse(ZERO);
            this.total = ofNullable(total).orElse(ZERO);
            this.taxed = ofNullable(taxed).orElse(ZERO);
        }

        /**
         * Performs addition of each field.
         *
         * @param that instance to combine
         * @return a new instance with result of each sum.
         */
        public TaxAccumulated add(TaxAccumulated that) {
            return new TaxAccumulated(
                    this.subTotal.add(that.subTotal),
                    this.total.add(that.total),
                    this.taxed.add(that.taxed));
        }


        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                    .append("subTotal", subTotal)
                    .append("total", total)
                    .append("taxed", taxed)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            TaxAccumulated that = (TaxAccumulated) o;

            return new EqualsBuilder()
                    .append(subTotal, that.subTotal)
                    .append(total, that.total)
                    .append(taxed, that.taxed)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(subTotal)
                    .append(total)
                    .append(taxed)
                    .toHashCode();
        }
    }
}
