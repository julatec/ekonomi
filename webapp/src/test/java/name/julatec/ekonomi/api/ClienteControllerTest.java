package name.julatec.ekonomi.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * El 6 oct 2026 dos sociedades «no salían en el sistema»: 3-101591364 y 3-101452333.
 * <p>
 * Hacienda guarda la cédula corrida —{@code 3101591364}— y en Costa Rica se escribe con
 * guiones, así que el {@code like '%3-101591364%'} no encontraba nada y la pantalla decía,
 * con todas sus letras, que esa contraparte no existe. La de comprobantes sí quitaba los
 * guiones antes de buscar; la de «Ventas y compras» no. La asimetría es lo que convierte un
 * buscador quisquilloso en una duda sobre los datos.
 */
class ClienteControllerTest {

    private static String patron(String texto) {
        return texto == null || texto.isBlank() ? "%" : "%" + texto.trim().toLowerCase() + "%";
    }

    private static String cedula(String texto) {
        return ClienteController.patronDeCedula(texto, patron(texto));
    }

    @Test
    @DisplayName("la cédula escrita como se escribe en Costa Rica encuentra la de la base")
    void quitaLosGuionesDeLaCedula() {
        assertEquals("%3101591364%", cedula("3-101591364"));
        assertEquals("%3101591364%", cedula("3-101-591364"));
        assertEquals("%3101452333%", cedula("3 101 452333"));
        assertEquals("%3101452333%", cedula("  3-101-452333  "));
        assertEquals("%3101591364%", cedula("3.101.591364"));
    }

    @Test
    @DisplayName("una cédula ya corrida se deja como está")
    void laCedulaSinGuionesNoCambia() {
        assertEquals("%3101591364%", cedula("3101591364"));
        assertEquals("%501230456%", cedula("501230456"));
    }

    @Test
    @DisplayName("un nombre con números adentro NO se compacta")
    void elNombreConNumerosSigueSiendoNombre() {
        // Compactarlo daría «247», que haría coincidir cédulas que nadie buscó: el patrón de
        // cédula tiene que quedarse igual al de nombre, que contra una columna numérica no
        // encuentra nada y deja que mande la coincidencia por nombre.
        assertEquals(patron("Taller 24/7"), cedula("Taller 24/7"));
        assertEquals(patron("3M de Costa Rica"), cedula("3M de Costa Rica"));
        assertEquals(patron("Tribuconta"), cedula("Tribuconta"));
    }

    @Test
    @DisplayName("sin texto no filtra: el patrón sigue siendo «%»")
    void sinTextoNoFiltra() {
        assertEquals("%", cedula(null));
        assertEquals("%", cedula(""));
        assertEquals("%", cedula("   "));
    }
}
