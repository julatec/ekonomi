package name.julatec.ekonomi.mcp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Las barandas de {@code consulta_sql}.
 * <p>
 * Esta herramienta se portó del servidor en Python, donde corría en un proceso
 * local contra un túnel. Acá cuelga de un endpoint HTTP en el servidor de
 * producción, así que las barandas dejaron de ser una formalidad.
 */
class ConsultaSqlGuardasTest {

    @ParameterizedTest
    @DisplayName("solo pasan los SELECT")
    @ValueSource(strings = {
            "update factura set clave = '1'",
            "delete from factura",
            "drop table factura",
            "insert into factura values (1)",
            "  CREATE TABLE x (a int)  ",
    })
    void rechazaLoQueNoEsSelect(String sql) {
        assertThrows(IllegalArgumentException.class, () -> EkonomiMcpTools.sqlValidado(sql, 200));
    }

    @ParameterizedTest
    @DisplayName("la base de seguridad está vetada")
    @ValueSource(strings = {
            "select * from ekonomi_primary.users",
            "select * from inbox",
            "select password from users",
            "select * from issuer",
    })
    void vetaLaBaseDeSeguridad(String sql) {
        final IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> EkonomiMcpTools.sqlValidado(sql, 200));
        assertTrue(error.getMessage().contains("seguridad"));
    }

    @Test
    @DisplayName("no se cuela un segundo statement")
    void rechazaVariosStatements() {
        assertThrows(IllegalArgumentException.class,
                () -> EkonomiMcpTools.sqlValidado("select 1 from factura; drop table factura", 200));
    }

    @Test
    @DisplayName("un punto y coma final es solo puntuación, no un segundo statement")
    void toleraElPuntoYComaFinal() {
        assertEquals("select clave from factura LIMIT 200",
                EkonomiMcpTools.sqlValidado("select clave from factura;", 200));
    }

    @Test
    @DisplayName("si no trae LIMIT se le impone uno")
    void imponeLimite() {
        assertEquals("select clave from factura LIMIT 50",
                EkonomiMcpTools.sqlValidado("select clave from factura", 50));
    }

    @Test
    @DisplayName("si ya trae LIMIT se respeta el de la consulta")
    void respetaElLimitePropio() {
        assertEquals("select clave from factura limit 5",
                EkonomiMcpTools.sqlValidado("select clave from factura limit 5", 200));
    }

    @Test
    @DisplayName("una consulta legítima sobre comprobantes pasa")
    void dejaPasarLoLegitimo() {
        final String sql = "select emisor_nombre, count(*) from factura group by emisor_nombre";
        assertEquals(sql + " LIMIT 200", EkonomiMcpTools.sqlValidado(sql, 200));
    }
}
