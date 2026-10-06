package name.julatec.ekonomi.extract.command;

import name.julatec.ekonomi.tribunet.Resumen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * La prueba que faltaba, y el motivo por el que faltaba.
 * <p>
 * Ocho columnas de la tabla —{@code total_venta}, {@code total_descuentos},
 * {@code total_venta_neta}, {@code total_otros_cargos} y las cuatro de servicios y
 * mercancías— existían en el esquema, tenían su campo en la entidad y su setter, y el mapeo
 * simplemente no las llamaba. El comprobante traía el dato y se descartaba en silencio: una
 * columna en null no se distingue de un comprobante que no traía el valor.
 * <p>
 * Por eso esta prueba no revisa campo por campo sino <b>por reflexión</b>: con una fuente en
 * la que todo viene con valor, cualquier campo de la entidad que quede en null es un campo
 * que el mapeo se saltó. El día que alguien agregue el noveno, la prueba lo pide sola.
 */
class ResumenMapperTest {

    /** Una fuente donde cada getter devuelve algo distinto de null. */
    private static Resumen fuenteCompleta() {
        return new Resumen() {
            @Override
            public String getCodigoMoneda() {
                return "USD";
            }

            @Override
            public BigDecimal getTipoCambio() {
                return new BigDecimal("512.50");
            }

            @Override
            public BigDecimal getTotalServGravados() {
                return new BigDecimal("1");
            }

            @Override
            public BigDecimal getTotalServExentos() {
                return new BigDecimal("2");
            }

            @Override
            public BigDecimal getTotalMercanciasGravadas() {
                return new BigDecimal("3");
            }

            @Override
            public BigDecimal getTotalMercanciasExentas() {
                return new BigDecimal("4");
            }

            @Override
            public BigDecimal getTotalGravado() {
                return new BigDecimal("5");
            }

            @Override
            public BigDecimal getTotalExento() {
                return new BigDecimal("6");
            }

            @Override
            public BigDecimal getTotalVenta() {
                return new BigDecimal("7");
            }

            @Override
            public BigDecimal getTotalDescuentos() {
                return new BigDecimal("8");
            }

            @Override
            public BigDecimal getTotalVentaNeta() {
                return new BigDecimal("9");
            }

            @Override
            public BigDecimal getTotalImpuesto() {
                return new BigDecimal("10");
            }

            @Override
            public BigDecimal getTotalIVADevuelto() {
                return new BigDecimal("11");
            }

            @Override
            public BigDecimal getTotalOtrosCargos() {
                return new BigDecimal("12");
            }

            @Override
            public BigDecimal getTotalComprobante() {
                return new BigDecimal("13");
            }
        };
    }

    @Test
    @DisplayName("ninguna columna de la entidad queda en null cuando la fuente trae todo")
    void noDejaNingunCampoSinMapear() throws IllegalAccessException {
        final name.julatec.ekonomi.tribunet.storage.Resumen resumen =
                new ResumenMapper().of(Optional.empty(), fuenteCompleta());

        final List<String> sinMapear = new ArrayList<>();
        for (Field campo : name.julatec.ekonomi.tribunet.storage.Resumen.class.getDeclaredFields()) {
            if (campo.isSynthetic() || java.lang.reflect.Modifier.isStatic(campo.getModifiers())) {
                continue;
            }
            campo.setAccessible(true);
            if (campo.get(resumen) == null) {
                sinMapear.add(campo.getName());
            }
        }

        assertTrue(sinMapear.isEmpty(),
                "el mapeo se saltó estos campos y quedarían en null en la base: " + sinMapear);
    }

    @Test
    @DisplayName("cada valor cae en su propia columna, no corrido")
    void cadaValorEnSuColumna() {
        final name.julatec.ekonomi.tribunet.storage.Resumen resumen =
                new ResumenMapper().of(Optional.empty(), fuenteCompleta());

        assertEquals("USD", resumen.getCodigoMoneda());
        assertEquals(0, new BigDecimal("512.50").compareTo(resumen.getTipoCambio()));
        assertEquals(0, new BigDecimal("1").compareTo(resumen.getTotalServGravados()));
        assertEquals(0, new BigDecimal("4").compareTo(resumen.getTotalMercanciasExentas()));
        assertEquals(0, new BigDecimal("7").compareTo(resumen.getTotalVenta()));
        assertEquals(0, new BigDecimal("8").compareTo(resumen.getTotalDescuentos()));
        assertEquals(0, new BigDecimal("9").compareTo(resumen.getTotalVentaNeta()));
        assertEquals(0, new BigDecimal("12").compareTo(resumen.getTotalOtrosCargos()));
        assertEquals(0, new BigDecimal("13").compareTo(resumen.getTotalComprobante()));
    }

    @Test
    @DisplayName("una fuente vieja, sin esos nodos, no revienta: deja null como antes")
    void conUnaFuenteSinEsosNodosNoRevienta() {
        // Los getters nuevos son `default` que devuelven null — un comprobante v4.2 no trae
        // ninguno. El mapeo tiene que escribir null, que es exactamente lo que había antes.
        final name.julatec.ekonomi.tribunet.storage.Resumen resumen =
                new ResumenMapper().of(Optional.empty(), new Resumen() {
                    @Override
                    public BigDecimal getTotalImpuesto() {
                        return new BigDecimal("100");
                    }

                    @Override
                    public BigDecimal getTotalComprobante() {
                        return new BigDecimal("1100");
                    }
                });

        assertEquals(0, new BigDecimal("1100").compareTo(resumen.getTotalComprobante()));
        assertEquals(null, resumen.getTotalVenta());
        assertEquals(null, resumen.getTotalOtrosCargos());
    }
}
