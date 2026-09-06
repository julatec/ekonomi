package name.julatec.ekonomi.extract.command;

import jakarta.mail.Folder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Fija cuántos días atrás mira el extractor en el perfil {@code production}.
 * <p>
 * El número estuvo clavado en 1 durante años, y por eso existía el perfil {@code month-import}:
 * para poder recuperar un mes había que cambiar de perfil, y ese perfil se quedó puesto en
 * producción, releyendo 45 días de correo cada seis horas. La prueba existe para que el valor
 * por omisión no vuelva a moverse sin que nadie lo note, y para dejar dicho que se puede subir
 * desde la configuración sin recompilar — que es lo que hace falta el día que el servicio pase
 * más de una semana caído.
 */
class VentanaDeProduccionTest {

    private FolderCommand comando(Map<String, Object> propiedades) {
        final FolderCommand comando = new FolderCommand(
                new Context<>(null, LoggerFactory.getLogger(VentanaDeProduccionTest.class)),
                mock(Folder.class));
        final StandardEnvironment ambiente = new StandardEnvironment();
        if (!propiedades.isEmpty()) {
            ambiente.getPropertySources().addFirst(new MapPropertySource("prueba", propiedades));
        }
        comando.setEnvironment(ambiente);
        return comando;
    }

    @Test
    @DisplayName("sin configurar nada, la ventana de production es de 7 días")
    void porOmisionSieteDias() {
        assertEquals(7, comando(Map.of()).ventanaDeProduccion());
    }

    @Test
    @DisplayName("la ventana se puede subir desde la configuración, sin recompilar")
    void configurable() {
        assertEquals(30, comando(Map.of("ekonomi.extract.ventana-dias", "30")).ventanaDeProduccion());
    }
}
