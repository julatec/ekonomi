package name.julatec.ekonomi.tribunet;

import name.julatec.ekonomi.tribunet.annotation.Adapt;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Impuesto Type adapter interface.
 */
@SuppressWarnings({"SpellCheckingInspection", "unused"})
@Adapt({
        cr.go.hacienda.tribunet.v42y2016.factura.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2017.factura.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.factura.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.factura.compra.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.tiquete.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.nota.credito.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.nota.debito.ImpuestoType.class,
})
public interface ImpuestoType {

    /**
     * Code
     *
     * @return code.
     */
    default String getCodigo() {
        return null;
    }

    /**
     * Rate code.
     *
     * @return rate code.
     */
    default String getCodigoTarifa() {
        return null;
    }

    /**
     * Rate
     *
     * @return rate.
     */
    default BigDecimal getTarifa() {
        return null;
    }

    /**
     * IVA rate.
     *
     * @return IVA rate.
     */
    default BigDecimal getFactorIVA() {
        return null;
    }

    /**
     * Tax amount.
     *
     * @return tax amount.
     */
    default BigDecimal getMonto() {
        return null;
    }

    /**
     * Export amount.
     *
     * @return export amount.
     */
    default BigDecimal getMontoExportacion() {
        return null;
    }

    /**
     * Exoneration type
     *
     * @return exoneration type.
     */
    default ExoneracionType getExoneracion() {
        return null;
    }

    /**
     * Impuesto Code enumeration.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    enum Codigo {

        /**
         * Lower bound guard, it is not a tax.
         */
        Empty(Optional.empty()),
        /**
         * See <a href="https://www.hacienda.go.cr/contenido/13001-impuesto-selectivo-de-consumo">
         * Impuesto selectivo de consumo</a>.
         */
        SelectivoDeConsumo(Optional.of("02")),
        /**
         * See <a href="https://www.hacienda.go.cr/contenido/13057-impuesto-unico-sobre-los-combustibles">
         * Impuesto &#xFA;nico sobre los combustibles</a>.
         */
        Combustivos(Optional.of("03")),
        /**
         * See <a href="https://www.hacienda.go.cr/contenido/13050-impuesto-especifico-de-consumo-sobre-bebidas-alcoholicas">
         * Impuesto espec&#xED;fico de consumo sobre bebidas alcoh&#xF3;licas</a>.
         */
        BebidasAlcoholicas(Optional.of("04")),
        /**
         * See <a href="https://www.hacienda.go.cr/contenido/12437-impuesto-especifico-sobre-bebidas-envasadas-sin-contenido-alcoholico-y-jabon-de-tocador">
         * Impuesto espec&#xED;fico sobre bebidas envasadas sin contenido alcoh&#xF3;lico y jab&#xF3;n de tocador</a>.
         */
        BebidasEnvasadas(Optional.of("05")),
        /**
         * See <a href="https://www.hacienda.go.cr/contenido/15927-tarifas-impuesto-5-al-cemento">
         * Tarifas impuesto 5% al cemento</a>.
         */
        Cemento(Optional.of("12")),
        /**
         * Any other kind of tax not included in the list below.
         */
        Otros(Optional.of("99")),
        /**
         * Not a tax kind, this guard helps to indentify all taxes related to tax base.
         */
        BaseImponible(Optional.empty()),
        /**
         * See <a href="https://www.hacienda.go.cr/contenido/13049-impuesto-a-los-productos-de-tabaco">
         * Impuesto a los productos de tabaco</a>.
         */
        ProductosDeTabaco(Optional.of("06")),
        /**
         * This guard helps to identify when the amount shall be classified separated than a tax.
         */
        OtrosCargos(Optional.empty()),
        /**
         * This guards helps to identify when the amount shall be classified separated than a tax.
         */
        ValorAgregado(Optional.of("01")),
        /**
         * See <a href="https://www.hacienda.go.cr/contenido/15102-IVA">
         * Generalidades del Impuesto sobre el Valor Agregado (IVA)</a>.
         */
        ValorAgregadoEspecial(Optional.of("07")),
        /**
         * See <a href="https://www.hacienda.go.cr/contenido/15102-IVA">
         * Generalidades del Impuesto sobre el Valor Agregado (IVA)</a>.
         */
        ValorAgregadoUsados(Optional.of("08")),
        /**
         * See <a href="https://www.hacienda.go.cr/contenido/15102-IVA">
         * Generalidades del Impuesto sobre el Valor Agregado (IVA)</a>.
         */
        Total(Optional.empty());

        /**
         * Code of the case in the case it is defined as guard.
         */
        public final Optional<String> code;

        /**
         * Constructor.
         *
         * @param code of the tax.
         */
        Codigo(Optional<String> code) {
            this.code = code;
        }

        private static final TreeMap<String, Codigo> reverseMap;

        static {
            reverseMap = new TreeMap<>();
            for (Codigo codigo : values()) {
                reverseMap.put(codigo.code.orElse(""), codigo);
            }
            reverseMap.put("", Empty);
        }

        /**
         * Gets the Codigo from the String id.
         *
         * @param codigo to lookup.
         * @return Code assiciated with the code.
         */
        public static Codigo of(String codigo) {
            return reverseMap.get(Optional.ofNullable(codigo).orElse(""));
        }
    }
}
