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

        /* Verison 4.2 2016 */

        cr.go.hacienda.tribunet.v42y2016.factura.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2016.tiquete.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.debito.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2016.nota.credito.ImpuestoType.class,

        /* Verison 4.2 2017 */

        cr.go.hacienda.tribunet.v42y2017.factura.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2017.tiquete.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.credito.ImpuestoType.class,
        cr.go.hacienda.tribunet.v42y2017.nota.debito.ImpuestoType.class,

        /* Verison 4.3 */

        cr.go.hacienda.tribunet.v43.factura.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.factura.compra.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.factura.exportacion.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.tiquete.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.nota.credito.ImpuestoType.class,
        cr.go.hacienda.tribunet.v43.nota.debito.ImpuestoType.class,

        /* Verison 4.4 */

        cr.go.hacienda.tribunet.v44.factura.ImpuestoType.class,
        cr.go.hacienda.tribunet.v44.factura.compra.ImpuestoType.class,
        cr.go.hacienda.tribunet.v44.factura.exportacion.ImpuestoType.class,
        cr.go.hacienda.tribunet.v44.tiquete.ImpuestoType.class,
        cr.go.hacienda.tribunet.v44.nota.debito.ImpuestoType.class,
        cr.go.hacienda.tribunet.v44.nota.credito.ImpuestoType.class,

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
     * Rate code. Called {@code CodigoTarifa} in the v4.3 schema; absent in v4.2 and v4.4.
     *
     * @return rate code.
     */
    default String getCodigoTarifa() {
        return null;
    }

    /**
     * Rate code. Called {@code CodigoTarifaIVA} in the v4.4 schema (renamed from
     * {@code CodigoTarifa}); absent in v4.2 and v4.3.
     *
     * @return rate code.
     */
    default String getCodigoTarifaIVA() {
        return null;
    }

    /**
     * Rate code resolved across schema versions: prefers the v4.4 field name, falls back to
     * the v4.3 one, and is {@code null} for v4.2 (which has no rate code field at all).
     *
     * @return the resolved rate code, or {@code null} when the document has none.
     */
    default String getCodigoTarifaResuelto() {
        return Optional.ofNullable(getCodigoTarifaIVA()).orElseGet(this::getCodigoTarifa);
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

        /**
         * How a tax code's amount folds into the report: added to the taxable base, reported
         * as a separate charge, counted as the IVA itself, or neither (guard values).
         * <p>
         * Replaces an earlier ordinal-based {@code compareTo} classification that broke
         * silently if the enum constants were ever reordered.
         */
        public enum Clasificacion {
            /** Specific consumption taxes that add to the IVA taxable base. */
            BaseImponible,
            /** Reported as its own charge, neither part of the base nor the IVA collected. */
            OtrosCargos,
            /** The IVA itself (Valor Agregado, Especial, Bienes Usados). */
            Iva,
            /** Guard/sentinel values with no fiscal meaning of their own. */
            Guard
        }

        /**
         * Classification used to decide how this tax code's amount folds into the report.
         *
         * @return the classification category.
         */
        public Clasificacion getClasificacion() {
            switch (this) {
                case SelectivoDeConsumo:
                case Combustivos:
                case BebidasAlcoholicas:
                case BebidasEnvasadas:
                case Cemento:
                case Otros:
                    return Clasificacion.BaseImponible;
                case ProductosDeTabaco:
                    return Clasificacion.OtrosCargos;
                case ValorAgregado:
                case ValorAgregadoEspecial:
                case ValorAgregadoUsados:
                    return Clasificacion.Iva;
                default:
                    return Clasificacion.Guard;
            }
        }
    }
}
