package name.julatec.ekonomi.accounting;

import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import name.julatec.ekonomi.report.csv.CsvBindByNameOrder;
import name.julatec.ekonomi.report.csv.CsvCurrencyConverter;
import name.julatec.ekonomi.report.csv.DefaultSeparator;
import name.julatec.ekonomi.report.csv.MappingStrategy;
import name.julatec.ekonomi.tribunet.CodigoTipoMoneda;
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
        "Tarifa 0% Art.32",
        "Base Imponible 0% Art.32",
        "Impuesto 0.5%",
        "Base Imponible 0.5%",
        "Impuesto 1%",
        "Base Imponible 1%",
        "Impuesto 2%",
        "Base Imponible 2%",
        "Impuesto 4%",
        "Base Imponible 4%",
        "Transitorio 0%",
        "Base Imponible Trans. 0%",
        "Transitorio 4%",
        "Base Imponible Trans. 4%",
        "Impuesto 8%",
        "Base Imponible 8%",
        "Impuesto 13%",
        "Base Imponible 13%",
        "Tarifa Exenta",
        "Base Imponible Exenta",
        "Tarifa 0% sin crédito",
        "Base Imponible 0% sin crédito",
        "Base Imponible Devuelto",
        "Total Otros Cargos",
        "Total Comprobante",
        "Clave",
        "Moneda"
})
@DefaultSeparator('\t')
@Entity(name = Voucher.EXPORT_NAME)
public class Voucher implements RecordComparable, Comparable<Voucher> {

    public static final MappingStrategy MAPPING_STRATEGY = new MappingStrategy<>(Voucher.class);
    public static final String EXPORT_NAME = "manual_transaction";
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

    @CsvBindByName(column = "Tarifa 0% Art.32", locale = LOCALE_CODE)
    private BigDecimal totalT01;

    @CsvBindByName(column = "Base Imponible 0% Art.32", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT01;

    @CsvBindByName(column = "Impuesto 0.5%", locale = LOCALE_CODE)
    private BigDecimal totalT09;

    @CsvBindByName(column = "Base Imponible 0.5%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT09;

    @CsvBindByName(column = "Impuesto 1%", locale = LOCALE_CODE)
    private BigDecimal totalT02;

    @CsvBindByName(column = "Base Imponible 1%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT02;

    @CsvBindByName(column = "Impuesto 2%", locale = LOCALE_CODE)
    private BigDecimal totalT03;

    @CsvBindByName(column = "Base Imponible 2%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT03;

    @CsvBindByName(column = "Impuesto 4%", locale = LOCALE_CODE)
    private BigDecimal totalT04;

    @CsvBindByName(column = "Base Imponible 4%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT04;

    @CsvBindByName(column = "Transitorio 0%", locale = LOCALE_CODE)
    private BigDecimal totalT05;

    @CsvBindByName(column = "Base Imponible Trans. 0%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT05;

    @CsvBindByName(column = "Transitorio 4%", locale = LOCALE_CODE)
    private BigDecimal totalT06;

    @CsvBindByName(column = "Base Imponible Trans. 4%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT06;

    @CsvBindByName(column = "Impuesto 8%", locale = LOCALE_CODE)
    private BigDecimal totalT07;

    @CsvBindByName(column = "Base Imponible 8%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT07;

    @CsvBindByName(column = "Impuesto 13%", locale = LOCALE_CODE)
    private BigDecimal totalT08;

    @CsvBindByName(column = "Base Imponible 13%", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT08;

    @CsvBindByName(column = "Tarifa Exenta", locale = LOCALE_CODE)
    private BigDecimal totalT10;

    @CsvBindByName(column = "Base Imponible Exenta", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT10;

    @CsvBindByName(column = "Tarifa 0% sin crédito", locale = LOCALE_CODE)
    private BigDecimal totalT11;

    @CsvBindByName(column = "Base Imponible 0% sin crédito", locale = LOCALE_CODE)
    private BigDecimal totalImpuestoT11;

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

    public static Voucher of(name.julatec.ekonomi.tribunet.Documento documento) {
        final DetailedDocument detailedDocument = DetailedDocument.of(documento);
        final boolean preserve = !(documento instanceof name.julatec.ekonomi.tribunet.NotaCredito);
        final CodigoTipoMoneda codigoTipoMoneda = documento.getResumenFactura().getCodigoTipoMoneda().cannonical();
        final BigDecimal exchangeRate = codigoTipoMoneda.getTipoCambio();
        final DetailedDocument.TaxAccumulated otros = detailedDocument.getTaxes().getOrElse(Otros, empty);
        final DetailedDocument.TaxAccumulated excento = detailedDocument.getTaxes().getOrElse(Excento, empty);
        final DetailedDocument.TaxAccumulated exonerado = detailedDocument.getTaxes().getOrElse(Exonerado, empty);
        final DetailedDocument.TaxAccumulated t01 = detailedDocument.getTaxes().getOrElse(T01, empty);
        final DetailedDocument.TaxAccumulated t02 = detailedDocument.getTaxes().getOrElse(T02, empty);
        final DetailedDocument.TaxAccumulated t03 = detailedDocument.getTaxes().getOrElse(T03, empty);
        final DetailedDocument.TaxAccumulated t04 = detailedDocument.getTaxes().getOrElse(T04, empty);
        final DetailedDocument.TaxAccumulated t05 = detailedDocument.getTaxes().getOrElse(T05, empty);
        final DetailedDocument.TaxAccumulated t06 = detailedDocument.getTaxes().getOrElse(T06, empty);
        final DetailedDocument.TaxAccumulated t07 = detailedDocument.getTaxes().getOrElse(T07, empty);
        final DetailedDocument.TaxAccumulated t08 = detailedDocument.getTaxes().getOrElse(T08, empty);
        final DetailedDocument.TaxAccumulated t09 = detailedDocument.getTaxes().getOrElse(T09, empty);
        final DetailedDocument.TaxAccumulated t10 = detailedDocument.getTaxes().getOrElse(T10, empty);
        final DetailedDocument.TaxAccumulated t11 = detailedDocument.getTaxes().getOrElse(T11, empty);
        final DetailedDocument.TaxAccumulated all = excento.add(exonerado).add(t01).add(t02).add(t03).add(t04).add(t05).add(t06).add(t07).add(t08).add(t09).add(t10).add(t11).add(otros);
        final BigDecimal totalComprobante = detailedDocument.getResumenFactura().getTotalComprobante();
        final BigDecimal totalLineas = all.subTotal.add(all.taxed);
        final BigDecimal otrosCargos = Optional.ofNullable(detailedDocument.getResumenFactura().getTotalOtrosCargos())
                .orElse(ZERO)
                .add(otros.taxed);

        return new Voucher()
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
                .setTotalT01(preserveSign(preserve, exchangeRate, t01.taxed))
                .setTotalT02(preserveSign(preserve, exchangeRate, t02.taxed))
                .setTotalT03(preserveSign(preserve, exchangeRate, t03.taxed))
                .setTotalT04(preserveSign(preserve, exchangeRate, t04.taxed))
                .setTotalT05(preserveSign(preserve, exchangeRate, t05.taxed))
                .setTotalT06(preserveSign(preserve, exchangeRate, t06.taxed))
                .setTotalT07(preserveSign(preserve, exchangeRate, t07.taxed))
                .setTotalT08(preserveSign(preserve, exchangeRate, t08.taxed))
                .setTotalT09(preserveSign(preserve, exchangeRate, t09.taxed))
                .setTotalT10(preserveSign(preserve, exchangeRate, t10.taxed))
                .setTotalT11(preserveSign(preserve, exchangeRate, t11.taxed))
                .setTotalImpuestoT01(preserveSign(preserve, exchangeRate, t01.subTotal))
                .setTotalImpuestoT02(preserveSign(preserve, exchangeRate, t02.subTotal))
                .setTotalImpuestoT03(preserveSign(preserve, exchangeRate, t03.subTotal))
                .setTotalImpuestoT04(preserveSign(preserve, exchangeRate, t04.subTotal))
                .setTotalImpuestoT05(preserveSign(preserve, exchangeRate, t05.subTotal))
                .setTotalImpuestoT06(preserveSign(preserve, exchangeRate, t06.subTotal))
                .setTotalImpuestoT07(preserveSign(preserve, exchangeRate, t07.subTotal))
                .setTotalImpuestoT08(preserveSign(preserve, exchangeRate, t08.subTotal))
                .setTotalImpuestoT09(preserveSign(preserve, exchangeRate, t09.subTotal))
                .setTotalImpuestoT10(preserveSign(preserve, exchangeRate, t10.subTotal))
                .setTotalImpuestoT11(preserveSign(preserve, exchangeRate, t11.subTotal))
                .setCurrency(codigoTipoMoneda.getCurrency())
                ;
    }

    private static BigDecimal preserveSign(boolean preserve, BigDecimal exchangeRate, BigDecimal value) {
        return Optional.ofNullable(value)
                .map(exchangeRate::multiply)
                .map(v -> preserve ? v : v.negate())
                .orElse(ZERO);
    }

    public static Voucher of(name.julatec.ekonomi.report.bank.BankTransaction<?> bankTransaction) {
        return new Voucher()
                .setConsecutivo(bankTransaction.getDocumentNumber())
                .setFecha(bankTransaction.getDate())
                .setTotalComprobante(bankTransaction.getAmount().negate())
                .setCurrency(bankTransaction.getCurrency())
                .setEmisorNombre(bankTransaction.getDescription())
                .setBankTransaction(Optional.of(bankTransaction.getId()))
                ;
    }

    public static Stream<Voucher> fromCsv(InputStream stream, Optional<Currency> currency) {
        final Character separator = Optional.ofNullable(Voucher.class.getAnnotation(DefaultSeparator.class))
                .map(DefaultSeparator::value)
                .orElse(',');
        final InputStreamReader reader = new InputStreamReader(stream);
        final CsvToBean<Voucher> csvToBean = new CsvToBeanBuilder(reader)
                .withType(Voucher.class)
                .withIgnoreLeadingWhiteSpace(true)
                .withSeparator(separator)
                .build();
        return csvToBean
                .stream()
                .map(t -> currency.map(t::setCurrency).orElse(t));
    }

    public static Workbook toWorkbook(Iterable<Voucher> transactionList) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException {
        return MAPPING_STRATEGY.toWorkbook(transactionList);
    }

    private static <T extends Comparable<T>> T min(Function<Voucher, T> property, Voucher l, Voucher r) {
        return min(property.apply(l), property.apply(r));
    }

    private static <T extends Comparable<T>> T max(Function<Voucher, T> property, Voucher l, Voucher r) {
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

    public Voucher setBankTransaction(Optional<UUID> bankTransaction) {
        this.bankTransaction = bankTransaction;
        return this;
    }

    public Optional<UUID> getManualTransaction() {
        return manualTransaction;
    }

    public Voucher setManualTransaction(Optional<UUID> manualTransaction) {
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

    public Voucher setElectronicTransaction(Optional<UUID> electronicTransaction) {
        this.electronicTransaction = electronicTransaction;
        return this;
    }


    public String getClave() {
        return clave;
    }

    public BigDecimal getTotalComprobante() {
        return totalComprobante;
    }

    public Voucher setClave(String clave) {
        this.clave = clave;
        return this;
    }

    public Date getFecha() {
        return fecha;
    }

    public Voucher setEmisorNumero(String emisorNumero) {
        this.emisorNumero = emisorNumero;
        return this;
    }

    public String getConsecutivo() {
        return consecutivo;
    }

    public Voucher setReceptorNumero(String receptorNumero) {
        this.receptorNumero = receptorNumero;
        return this;
    }

    public BigDecimal getTotalExcento() {
        return totalExcento;
    }


    public BigDecimal getTotalExonerado() {
        return totalExonerado;
    }

    public Voucher setTotalComprobante(BigDecimal totalComprobante) {
        this.totalComprobante = totalComprobante;
        return this;
    }

    public BigDecimal getTotalImpuestoDevuelto() {
        return totalImpuestoDevuelto;
    }

    public Voucher setFecha(Date fecha) {
        this.fecha = fecha;
        return this;
    }

    public String getEmisorNombre() {
        return emisorNombre;
    }

    public Voucher setConsecutivo(String consecutivo) {
        this.consecutivo = consecutivo != null && consecutivo.length() > 0 && consecutivo.charAt(0) == '\'' ?
                consecutivo.substring(1) :
                consecutivo;
        return this;
    }

    public String getReceptorNombre() {
        return receptorNombre;
    }

    public Voucher setTotalExcento(BigDecimal totalGravado) {
        this.totalExcento = totalGravado;
        return this;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Voucher setTotalExonerado(BigDecimal totalExento) {
        this.totalExonerado = totalExento;
        return this;
    }

    public Voucher setTotalImpuestoDevuelto(BigDecimal totalImpuestoDevuelto) {
        this.totalImpuestoDevuelto = totalImpuestoDevuelto;
        return this;
    }

    public Voucher setEmisorNombre(String emisorNombre) {
        this.emisorNombre = emisorNombre;
        return this;
    }

    public Voucher setReceptorNombre(String receptorNombre) {
        this.receptorNombre = receptorNombre;
        return this;
    }

    public Voucher setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public BigDecimal getTotalImpuestoT02() {
        return totalImpuestoT02;
    }

    public Voucher setTotalImpuestoT02(BigDecimal totalImpuestoT02) {
        this.totalImpuestoT02 = totalImpuestoT02;
        return this;
    }

    public BigDecimal getTotalImpuestoT03() {
        return totalImpuestoT03;
    }

    public Voucher setTotalImpuestoT03(BigDecimal totalImpuestoT03) {
        this.totalImpuestoT03 = totalImpuestoT03;
        return this;
    }

    public BigDecimal getTotalImpuestoT04() {
        return totalImpuestoT04;
    }

    public Voucher setTotalImpuestoT04(BigDecimal totalImpuestoT04) {
        this.totalImpuestoT04 = totalImpuestoT04;
        return this;
    }

    public BigDecimal getTotalImpuestoT07() {
        return totalImpuestoT07;
    }

    public Voucher setTotalImpuestoT07(BigDecimal totalImpuestoT07) {
        this.totalImpuestoT07 = totalImpuestoT07;
        return this;
    }

    public BigDecimal getTotalImpuestoT08() {
        return totalImpuestoT08;
    }

    public Voucher setTotalImpuestoT08(BigDecimal totalImpuestoT08) {
        this.totalImpuestoT08 = totalImpuestoT08;
        return this;
    }

    public BigDecimal getTotalT02() {
        return totalT02;
    }

    public Voucher setTotalT02(BigDecimal totalT02) {
        this.totalT02 = totalT02;
        return this;
    }

    public BigDecimal getTotalT03() {
        return totalT03;
    }

    public Voucher setTotalT03(BigDecimal totalT03) {
        this.totalT03 = totalT03;
        return this;
    }

    public BigDecimal getTotalT04() {
        return totalT04;
    }

    public Voucher setTotalT04(BigDecimal totalT04) {
        this.totalT04 = totalT04;
        return this;
    }

    public BigDecimal getTotalT07() {
        return totalT07;
    }

    public Voucher setTotalT07(BigDecimal totalT07) {
        this.totalT07 = totalT07;
        return this;
    }

    public BigDecimal getTotalT08() {
        return totalT08;
    }

    public Voucher setTotalT08(BigDecimal totalT08) {
        this.totalT08 = totalT08;
        return this;
    }

    public BigDecimal getTotalT09() {
        return totalT09;
    }

    public Voucher setTotalT09(BigDecimal totalT09) {
        this.totalT09 = totalT09;
        return this;
    }

    public BigDecimal getTotalImpuestoT09() {
        return totalImpuestoT09;
    }

    public Voucher setTotalImpuestoT09(BigDecimal totalImpuestoT09) {
        this.totalImpuestoT09 = totalImpuestoT09;
        return this;
    }

    public BigDecimal getTotalT01() {
        return totalT01;
    }

    public Voucher setTotalT01(BigDecimal totalT01) {
        this.totalT01 = totalT01;
        return this;
    }

    public BigDecimal getTotalImpuestoT01() {
        return totalImpuestoT01;
    }

    public Voucher setTotalImpuestoT01(BigDecimal totalImpuestoT01) {
        this.totalImpuestoT01 = totalImpuestoT01;
        return this;
    }

    public BigDecimal getTotalT05() {
        return totalT05;
    }

    public Voucher setTotalT05(BigDecimal totalT05) {
        this.totalT05 = totalT05;
        return this;
    }

    public BigDecimal getTotalImpuestoT05() {
        return totalImpuestoT05;
    }

    public Voucher setTotalImpuestoT05(BigDecimal totalImpuestoT05) {
        this.totalImpuestoT05 = totalImpuestoT05;
        return this;
    }

    public BigDecimal getTotalT06() {
        return totalT06;
    }

    public Voucher setTotalT06(BigDecimal totalT06) {
        this.totalT06 = totalT06;
        return this;
    }

    public BigDecimal getTotalImpuestoT06() {
        return totalImpuestoT06;
    }

    public Voucher setTotalImpuestoT06(BigDecimal totalImpuestoT06) {
        this.totalImpuestoT06 = totalImpuestoT06;
        return this;
    }

    public BigDecimal getTotalT11() {
        return totalT11;
    }

    public Voucher setTotalT11(BigDecimal totalT11) {
        this.totalT11 = totalT11;
        return this;
    }

    public BigDecimal getTotalImpuestoT11() {
        return totalImpuestoT11;
    }

    public Voucher setTotalImpuestoT11(BigDecimal totalImpuestoT11) {
        this.totalImpuestoT11 = totalImpuestoT11;
        return this;
    }

    public BigDecimal getTotalT10() {
        return totalT10;
    }

    public Voucher setTotalT10(BigDecimal totalT10) {
        this.totalT10 = totalT10;
        return this;
    }

    public BigDecimal getTotalImpuestoT10() {
        return totalImpuestoT10;
    }

    public Voucher setTotalImpuestoT10(BigDecimal totalImpuestoT10) {
        this.totalImpuestoT10 = totalImpuestoT10;
        return this;
    }

    public BigDecimal getTotalOtrosCargos() {
        return totalOtrosCargos;
    }

    public Voucher setTotalOtrosCargos(BigDecimal totalOtrosCargos) {
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

    public Voucher with(Voucher that) {
        return this
                .setBankTransaction(getBankTransaction().or(that::getBankTransaction))
                .setElectronicTransaction(getElectronicTransaction().or(that::getElectronicTransaction))
                .setManualTransaction(getManualTransaction().or(that::getManualTransaction))
                .setClave(min(Voucher::getClave, this, that))
                .setConsecutivo(max(Voucher::getConsecutivo, this, that))
                .setFecha(min(Voucher::getFecha, this, that))
                .setEmisorNumero(max(Voucher::getEmisorNumero, this, that))
                .setEmisorNombre(max(Voucher::getEmisorNombre, this, that))
                .setReceptorNumero(max(Voucher::getReceptorNumero, this, that))
                .setReceptorNombre(max(Voucher::getReceptorNombre, this, that))
                .setTotalExcento(max(Voucher::getTotalExcento, this, that))
                .setTotalExonerado(max(Voucher::getTotalExonerado, this, that))
                .setTotalImpuestoDevuelto(max(Voucher::getTotalImpuestoDevuelto, this, that))
                .setTotalComprobante(max(Voucher::getTotalComprobante, this, that));
    }

    public Voucher with(Optional<Voucher> r) {
        return r.isPresent() ? this.with(r.get()) : this;
    }

    @Override
    public Record.Key getKey() {
        return key;
    }

    @Override
    public int compareTo(Voucher paperVoucher) {
        return new CompareToBuilder()
                .append(key, paperVoucher.key)
                .append(this.clave, paperVoucher.clave).toComparison();
    }
}
