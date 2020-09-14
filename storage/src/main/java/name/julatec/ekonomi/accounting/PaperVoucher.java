package name.julatec.ekonomi.accounting;


import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import name.julatec.ekonomi.report.csv.CsvBindByNameOrder;
import name.julatec.ekonomi.report.csv.CsvCurrencyConverter;
import name.julatec.ekonomi.report.csv.DefaultSeparator;
import name.julatec.ekonomi.report.csv.MappingStrategy;
import name.julatec.ekonomi.tribunet.DetailedDocument;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.poi.ss.usermodel.Workbook;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static name.julatec.ekonomi.tribunet.DetailedDocument.TaxAccumulated.empty;
import static name.julatec.ekonomi.tribunet.FactorIVA.*;

@CsvBindByNameOrder({
        "Fecha",
        "Consecutivo",
        "Emisor",
        "Nombre Emisor",
        "Receptor",
        "Nombre Receptor",
        "Total Excento",
        "Total Exonerado",
        "Impuesto 1%",
        "Base Imponible 1%",
        "Impuesto 2%",
        "Base Imponible 2%",
        "Impuesto 4%",
        "Base Imponible 4%",
        "Impuesto 8%",
        "Base Imponible 8%",
        "Impuesto 13%",
        "Base Imponible 13%",
        "Base Imponible Devuelto",
        "Total Otros Cargos",
        "Total Comprobante",
        "Clave",
        "Moneda"
})
@DefaultSeparator('\t')
@Entity(name = "manual_transaction")
public class PaperVoucher implements RecordComparable, Comparable<PaperVoucher>{

    public static final MappingStrategy MAPPING_STRATEGY = new MappingStrategy<>(PaperVoucher.class);
    public static final String EXPORT_NAME = "paperVoucher";
    public static final String LOCALE_CODE = "es_CR";
    public static final String CRC = "CRC";
    public static final Currency CRC_CURRENCY = Currency.getInstance(CRC);

    @Id
    @CsvBindByName(column = "Clave")
    private String clave;

    @CsvBindByName(column = "Consecutivo")
    private String consecutivo;

    @CsvBindByName(column = "Fecha")
    @CsvDate("yyyy/MM/dd")
    private Date fecha;

    @CsvBindByName(column = "Emisor")
    private String emisorNumero;

    @CsvBindByName(column = "Receptor")
    private String receptorNumero;

    @CsvBindByName(column = "Nombre Emisor")
    private String emisorNombre;

    @CsvBindByName(column = "Nombre Receptor")
    private String receptorNombre;

    @CsvBindByName(column = "Total Excento", locale = LOCALE_CODE)
    private BigDecimal totalExcento;

    @CsvBindByName(column = "Total Exonerado", locale = LOCALE_CODE)
    private BigDecimal totalExonerado;

    @CsvBindByName(column = "Impuesto 1%", locale = LOCALE_CODE)
    private BigDecimal totalF01;

    @CsvBindByName(column = "Base Imponible 1%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoF01;

    @CsvBindByName(column = "Impuesto 2%", locale = LOCALE_CODE)
    private BigDecimal totalF02;

    @CsvBindByName(column = "Base Imponible 2%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoF02;

    @CsvBindByName(column = "Impuesto 4%", locale = LOCALE_CODE)
    private BigDecimal totalF04;

    @CsvBindByName(column = "Base Imponible 4%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoF04;

    @CsvBindByName(column = "Impuesto 8%", locale = LOCALE_CODE)
    private BigDecimal totalF08;

    @CsvBindByName(column = "Base Imponible 8%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoF08;

    @CsvBindByName(column = "Impuesto 13%", locale = LOCALE_CODE)
    private BigDecimal totalF13;

    @CsvBindByName(column = "Base Imponible 13%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoF13;

    @CsvBindByName(column = "Total Otros Cargos", locale = LOCALE_CODE)
    private BigDecimal totalOtrosCargos;

    @CsvBindByName(column = "Base Imponible Devuelto", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoDevuelto;

    @CsvBindByName(column = "Total Comprobante", locale = LOCALE_CODE)
    private BigDecimal totalComprobante;

    @CsvCustomBindByName(column = "Moneda", converter = CsvCurrencyConverter.class)
    private Currency currency = Currency.getInstance(Locale.getDefault());

    @Transient
    @CsvIgnore
    private final Record.Key key = new Record.Key(
            this::getFecha,
            () -> getTotalComprobante(),
            this::getCurrency);
    //@CsvCustomBindByName(column = "Comprobante Bancario", converter = CsvOptionalUuidConverter.class)
    @CsvIgnore
    private transient Optional<UUID> bankTransaction = Optional.empty();
    //@CsvCustomBindByName(column = "Comprobante Fisico", converter = CsvOptionalUuidConverter.class)
    @CsvIgnore
    private transient Optional<UUID> manualTransaction = Optional.empty();
    //@CsvCustomBindByName(column = "Comprobante Electronico", converter = CsvOptionalUuidConverter.class)
    @CsvIgnore
    private transient Optional<UUID> electronicTransaction = Optional.empty();

    private static BigDecimal getExchageRate(name.julatec.ekonomi.tribunet.Documento documento) {

        final String currency = Optional.ofNullable(documento
                .getResumenFactura()
                .getCodigoTipoMoneda()
                .getCodigoMoneda())
                .orElse(CRC);

        if (CRC.equals(currency)) {
            return ONE;
        }

        final BigDecimal exchangeRate = documento.getResumenFactura().getCodigoTipoMoneda().getTipoCambio();
        if (exchangeRate == null) {
            return ONE;
        }
        if (ONE.compareTo(exchangeRate)  == 0) {
            return ONE;
        }
        if (ZERO.compareTo(exchangeRate)  == 0) {
            return ONE;
        }
        return exchangeRate;
    }

    public static PaperVoucher of(name.julatec.ekonomi.tribunet.Documento documento) {
        final DetailedDocument detailedDocument = DetailedDocument.of(documento);
        final boolean preserve = !(documento instanceof name.julatec.ekonomi.tribunet.NotaCredito);
        final Currency currency = Optional.ofNullable(documento
                .getResumenFactura()
                .getCodigoTipoMoneda()
                .getCodigoMoneda())
                .map(Currency::getInstance)
                .orElse(CRC_CURRENCY);
        final BigDecimal exchangeRate = getExchageRate(documento);
        final DetailedDocument.TaxAccumulated otros = detailedDocument.getTaxes().getOrElse(Otros, empty);
        final DetailedDocument.TaxAccumulated excento = detailedDocument.getTaxes().getOrElse(Excento, empty);
        final DetailedDocument.TaxAccumulated exonerado = detailedDocument.getTaxes().getOrElse(Exonerado, empty);
        final DetailedDocument.TaxAccumulated f01 = detailedDocument.getTaxes().getOrElse(F01, empty);
        final DetailedDocument.TaxAccumulated f02 = detailedDocument.getTaxes().getOrElse(F02, empty);
        final DetailedDocument.TaxAccumulated f04 = detailedDocument.getTaxes().getOrElse(F04, empty);
        final DetailedDocument.TaxAccumulated f08 = detailedDocument.getTaxes().getOrElse(F08, empty);
        final DetailedDocument.TaxAccumulated f13 = detailedDocument.getTaxes().getOrElse(F13, empty);
        final DetailedDocument.TaxAccumulated all = excento.add(exonerado).add(f01).add(f02).add(f04).add(f08).add(f13).add(otros);
        final BigDecimal totalComprobante = detailedDocument.getResumenFactura().getTotalComprobante();
        final BigDecimal totalLineas = all.subTotal.add(all.taxed);
        final BigDecimal otrosCargos = Optional.ofNullable(detailedDocument.getResumenFactura().getTotalOtrosCargos())
                .orElse(ZERO)
                .add(otros.taxed);
        if (totalLineas.subtract(totalComprobante).abs().compareTo(BigDecimal.TEN) > 0) {
            System.out.println("Lineas " + totalLineas + " != " + totalComprobante);
        }

        return new PaperVoucher()
                .setClave(detailedDocument.getClave())
                .setConsecutivo(detailedDocument.getNumeroConsecutivo())
                .setFecha(detailedDocument.getFechaEmision().toGregorianCalendar().getTime())
                .setEmisorNumero(detailedDocument.getEmisor().getIdentificacion().getNumero())
                .setEmisorNombre(detailedDocument.getEmisor().getNombre())
                .setReceptorNumero(detailedDocument.getReceptor().getIdentificacion().getNumero())
                .setReceptorNombre(detailedDocument.getReceptor().getNombre())
                .setTotalOtrosCargos(preserveSign(preserve, exchangeRate, otrosCargos))
                .setTotalImpuestoDevuelto(preserveSign(preserve, exchangeRate, detailedDocument.getResumenFactura().getTotalIVADevuelto()))
                .setTotalComprobante(preserveSign(preserve, exchangeRate, detailedDocument.getResumenFactura().getTotalComprobante()))
                .setTotalExcento(preserveSign(preserve, exchangeRate, excento.subTotal))
                .setTotalExonerado(preserveSign(preserve, exchangeRate, exonerado.subTotal))
                .setTotalF01(preserveSign(preserve, exchangeRate, f01.taxed))
                .setTotalF02(preserveSign(preserve, exchangeRate, f02.taxed))
                .setTotalF04(preserveSign(preserve, exchangeRate, f04.taxed))
                .setTotalF13(preserveSign(preserve, exchangeRate, f13.taxed))
                .setTotalF08(preserveSign(preserve, exchangeRate, f08.taxed))
                .setTotalImpuestoF01(preserveSign(preserve, exchangeRate,  f01.subTotal))
                .setTotalImpuestoF02(preserveSign(preserve, exchangeRate, f02.subTotal))
                .setTotalImpuestoF04(preserveSign(preserve, exchangeRate, f04.subTotal))
                .setTotalImpuestoF08(preserveSign(preserve, exchangeRate, f08.subTotal))
                .setTotalImpuestoF13(preserveSign(preserve, exchangeRate, f13.subTotal))
                .setCurrency(currency)
                ;
    }

    private static BigDecimal preserveSign(boolean preserve, BigDecimal exchangeRate, BigDecimal value) {
        return Optional.ofNullable(value)
                .map(exchangeRate::multiply)
                .map(v -> preserve ? v : v.negate())
                .orElse(ZERO);
    }

    public static PaperVoucher of(name.julatec.ekonomi.report.bank.BankTransaction<?> bankTransaction) {
        return new PaperVoucher()
                .setConsecutivo(bankTransaction.getDocumentNumber())
                .setFecha(bankTransaction.getDate())
                .setTotalComprobante(bankTransaction.getAmount().negate())
                .setCurrency(bankTransaction.getCurrency())
                .setEmisorNombre(bankTransaction.getDescription())
                .setBankTransaction(Optional.of(bankTransaction.getId()))
                ;
    }

    public static Stream<PaperVoucher> fromCsv(InputStream stream, Optional<Currency> currency) {
        final Character separator = Optional.ofNullable(PaperVoucher.class.getAnnotation(DefaultSeparator.class))
                .map(DefaultSeparator::value)
                .orElse(',');
        final InputStreamReader reader = new InputStreamReader(stream);
        final CsvToBean<PaperVoucher> csvToBean = new CsvToBeanBuilder(reader)
                .withType(PaperVoucher.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withSeparator(separator)
                .build();
        return csvToBean
                .stream()
                .map(t -> currency.map(t::setCurrency).orElse(t));
    }

    public static Workbook toWorkbook(Iterable<PaperVoucher> transactionList) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        return MAPPING_STRATEGY.toWorkbook(transactionList);
    }

    private static <T extends Comparable<T>> T min(Function<PaperVoucher, T> property, PaperVoucher l, PaperVoucher r) {
        return min(property.apply(l), property.apply(r));
    }

    private static <T extends Comparable<T>> T max(Function<PaperVoucher, T> property, PaperVoucher l, PaperVoucher r) {
        return max(property.apply(l), property.apply(r));
    }

    private static <T extends Comparable<T>> T min(T l, T r) {
        if (l == null) {
            return r;
        }
        if (r == null) {
            return l;
        }
        return l.compareTo(r) <= 0 ? l : r;
    }

    private static <T extends Comparable<T>> T max(T l, T r) {
        if (l == null) {
            return r;
        }
        if (r == null) {
            return l;
        }
        return l.compareTo(r) >= 0 ? l : r;
    }

    public Optional<UUID> getBankTransaction() {
        return bankTransaction;
    }

    public PaperVoucher setBankTransaction(Optional<UUID> bankTransaction) {
        this.bankTransaction = bankTransaction;
        return this;
    }

    public Optional<UUID> getManualTransaction() {
        return manualTransaction;
    }

    public PaperVoucher setManualTransaction(Optional<UUID> manualTransaction) {
        this.manualTransaction = manualTransaction;
        return this;
    }

    public String getEmisorNumero() {
        return emisorNumero;
    }

    public Optional<UUID> getElectronicTransaction() {
        return electronicTransaction;
    }

    public String getReceptorNumero() {
        return receptorNumero;
    }

    public PaperVoucher setElectronicTransaction(Optional<UUID> electronicTransaction) {
        this.electronicTransaction = electronicTransaction;
        return this;
    }


    public String getClave() {
        return clave;
    }

    public BigDecimal getTotalComprobante() {
        return totalComprobante;
    }

    public PaperVoucher setClave(String clave) {
        this.clave = clave;
        return this;
    }

    public Date getFecha() {
        return fecha;
    }

    public PaperVoucher setEmisorNumero(String emisorNumero) {
        this.emisorNumero = emisorNumero;
        return this;
    }

    public String getConsecutivo() {
        return consecutivo;
    }

    public PaperVoucher setReceptorNumero(String receptorNumero) {
        this.receptorNumero = receptorNumero;
        return this;
    }

    public BigDecimal getTotalExcento() {
        return totalExcento;
    }


    public BigDecimal getTotalExonerado() {
        return totalExonerado;
    }

    public PaperVoucher setTotalComprobante(BigDecimal totalComprobante) {
        this.totalComprobante = totalComprobante;
        return this;
    }

    public BigDecimal getTotalImpuestoDevuelto() {
        return totalImpuestoDevuelto;
    }

    public PaperVoucher setFecha(Date fecha) {
        this.fecha = fecha;
        return this;
    }

    public String getEmisorNombre() {
        return emisorNombre;
    }

    public PaperVoucher setConsecutivo(String consecutivo) {
        this.consecutivo = consecutivo != null && consecutivo.length() > 0 && consecutivo.charAt(0) == '\'' ?
                consecutivo.substring(1) :
                consecutivo;
        return this;
    }

    public String getReceptorNombre() {
        return receptorNombre;
    }

    public PaperVoucher setTotalExcento(BigDecimal totalGravado) {
        this.totalExcento = totalGravado;
        return this;
    }

    public Currency getCurrency() {
        return currency;
    }

    public PaperVoucher setTotalExonerado(BigDecimal totalExento) {
        this.totalExonerado = totalExento;
        return this;
    }

    public PaperVoucher setTotalImpuestoDevuelto(BigDecimal totalImpuestoDevuelto) {
        this.totalImpuestoDevuelto = totalImpuestoDevuelto;
        return this;
    }

    public PaperVoucher setEmisorNombre(String emisorNombre) {
        this.emisorNombre = emisorNombre;
        return this;
    }

    public PaperVoucher setReceptorNombre(String receptorNombre) {
        this.receptorNombre = receptorNombre;
        return this;
    }

    public PaperVoucher setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public BigDecimal getTotalImpuestoF01() {
        return totalImpuestoF01;
    }

    public PaperVoucher setTotalImpuestoF01(BigDecimal totalImpuestoF01) {
        this.totalImpuestoF01 = totalImpuestoF01;
        return this;
    }

    public BigDecimal getTotalImpuestoF02() {
        return totalImpuestoF02;
    }

    public PaperVoucher setTotalImpuestoF02(BigDecimal totalImpuestoF02) {
        this.totalImpuestoF02 = totalImpuestoF02;
        return this;
    }

    public BigDecimal getTotalImpuestoF04() {
        return totalImpuestoF04;
    }

    public PaperVoucher setTotalImpuestoF04(BigDecimal totalImpuestoF04) {
        this.totalImpuestoF04 = totalImpuestoF04;
        return this;
    }

    public BigDecimal getTotalImpuestoF08() {
        return totalImpuestoF08;
    }

    public PaperVoucher setTotalImpuestoF08(BigDecimal totalImpuestoF08) {
        this.totalImpuestoF08 = totalImpuestoF08;
        return this;
    }

    public BigDecimal getTotalImpuestoF13() {
        return totalImpuestoF13;
    }

    public PaperVoucher setTotalImpuestoF13(BigDecimal totalImpuestoF12) {
        this.totalImpuestoF13 = totalImpuestoF12;
        return this;
    }

    public BigDecimal getTotalF01() {
        return totalF01;
    }

    public PaperVoucher setTotalF01(BigDecimal totalF01) {
        this.totalF01 = totalF01;
        return this;
    }

    public BigDecimal getTotalF02() {
        return totalF02;
    }

    public PaperVoucher setTotalF02(BigDecimal totalF02) {
        this.totalF02 = totalF02;
        return this;
    }

    public BigDecimal getTotalF04() {
        return totalF04;
    }

    public PaperVoucher setTotalF04(BigDecimal totalF04) {
        this.totalF04 = totalF04;
        return this;
    }

    public BigDecimal getTotalF08() {
        return totalF08;
    }

    public PaperVoucher setTotalF08(BigDecimal totalF08) {
        this.totalF08 = totalF08;
        return this;
    }

    public BigDecimal getTotalF13() {
        return totalF13;
    }

    public PaperVoucher setTotalF13(BigDecimal totalF13) {
        this.totalF13 = totalF13;
        return this;
    }

    public BigDecimal getTotalOtrosCargos() {
        return totalOtrosCargos;
    }

    public PaperVoucher setTotalOtrosCargos(BigDecimal totalOtrosCargos) {
        this.totalOtrosCargos = totalOtrosCargos;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("fecha", fecha)
                .append("totalComprobante", totalComprobante)
                .append("emisorNumero", emisorNumero)
                .append("clave", clave)
                .append("consecutivo", consecutivo)
                .append("receptorNumero", receptorNumero)
                .append("emisorNombre", emisorNombre)
                .append("receptorNombre", receptorNombre)
                .append("totalGravado", totalExcento)
                .append("totalExento", totalExonerado)
                .append("totalImpuestoDevuelto", totalImpuestoDevuelto)
                .append("currency", currency)
                .toString();
    }

    public PaperVoucher with(PaperVoucher that) {
        return this
                .setBankTransaction(getBankTransaction().or(that::getBankTransaction))
                .setElectronicTransaction(getElectronicTransaction().or(that::getElectronicTransaction))
                .setManualTransaction(getManualTransaction().or(that::getManualTransaction))
                .setClave(min(PaperVoucher::getClave, this, that))
                .setConsecutivo(max(PaperVoucher::getConsecutivo, this, that))
                .setFecha(min(PaperVoucher::getFecha, this, that))
                .setEmisorNumero(max(PaperVoucher::getEmisorNumero, this, that))
                .setEmisorNombre(max(PaperVoucher::getEmisorNombre, this, that))
                .setReceptorNumero(max(PaperVoucher::getReceptorNumero, this, that))
                .setReceptorNombre(max(PaperVoucher::getReceptorNombre, this, that))
                .setTotalExcento(max(PaperVoucher::getTotalExcento, this, that))
                .setTotalExonerado(max(PaperVoucher::getTotalExonerado, this, that))
                .setTotalImpuestoDevuelto(max(PaperVoucher::getTotalImpuestoDevuelto, this, that))
                .setTotalComprobante(max(PaperVoucher::getTotalComprobante, this, that));
    }

    public PaperVoucher with(Optional<PaperVoucher> r) {
        return r.isPresent() ? this.with(r.get()) : this;
    }

    @Override
    public Record.Key getKey() {
        return key;
    }

    @Override
    public int compareTo(PaperVoucher paperVoucher) {
        return new CompareToBuilder()
                .append(key, paperVoucher.key)
                .append(this.clave, paperVoucher.clave).toComparison();
    }
}
