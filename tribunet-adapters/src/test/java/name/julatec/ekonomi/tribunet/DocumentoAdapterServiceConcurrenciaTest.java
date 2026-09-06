package name.julatec.ekonomi.tribunet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link DocumentoAdapterService} es un {@code @Service} —o sea, un singleton— y la ingesta lo
 * llama desde un pool de varios hilos: {@code ExtractExecutor} arranca con
 * {@code min(8, núcleos)}, que en el Pi de producción son <b>cuatro</b>.
 * <p>
 * Antes de este arreglo compartía un único {@code Unmarshaller} de JAXB, que la especificación
 * declara explícitamente no reentrante. En producción eso se veía como 125 «Unable to adapt»
 * repartidos parejo entre los cuatro hilos (33/29/35/28) — un reparto uniforme es la firma del
 * estado mutable compartido, no la de unos pocos documentos rotos. Cada uno de esos fallos es
 * un comprobante que llegó por correo y no se guardó.
 * <p>
 * La prueba corre el mismo documento por muchos hilos a la vez y exige que <b>todas</b> las
 * conversiones salgan bien y con el mismo resultado. Contra la versión con el
 * {@code Unmarshaller} compartido falla; contra la corregida, pasa.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@SpringBootTest
class DocumentoAdapterServiceConcurrenciaTest {

    private static final int HILOS = 8;
    private static final int POR_HILO = 40;

    // El mismo singleton que usa la ingesta: el mapa de ciclos de vida lo llena Spring por
    // @Autowired, así que construirlo a mano no sirve para reproducir el escenario real.
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    DocumentoAdapterService servicio;

    @Value("classpath:factura.xml")
    Resource factura;

    @Test
    @DisplayName("adapt() aguanta varios hilos sin fallar ni devolver algo distinto")
    void adaptEsReentrante() throws Exception {
        final String xml = new String(factura.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // La referencia, calculada en un solo hilo y sin competencia.
        final Optional<Documento> solo = servicio.adapt(xml, e -> {
            throw new AssertionError("La conversión de referencia falló", e);
        });
        final String claveEsperada = solo.orElseThrow().getClave();

        final AtomicInteger fallos = new AtomicInteger();
        final AtomicInteger distintos = new AtomicInteger();

        try (ExecutorService pool = Executors.newFixedThreadPool(HILOS)) {
            final List<Callable<Void>> tareas = java.util.stream.IntStream.range(0, HILOS)
                    .<Callable<Void>>mapToObj(h -> () -> {
                        for (int i = 0; i < POR_HILO; i++) {
                            final Optional<Documento> adaptado =
                                    servicio.adapt(xml, e -> fallos.incrementAndGet());
                            if (adaptado.isEmpty()) {
                                fallos.incrementAndGet();
                            } else if (!claveEsperada.equals(adaptado.get().getClave())) {
                                // El modo de falla que asusta: no revienta, devuelve otra cosa.
                                distintos.incrementAndGet();
                            }
                        }
                        return null;
                    })
                    .toList();
            for (Future<Void> f : pool.invokeAll(tareas)) {
                f.get();
            }
        }

        assertEquals(0, fallos.get(),
                "conversiones fallidas de " + (HILOS * POR_HILO) + " con " + HILOS + " hilos");
        assertEquals(0, distintos.get(),
                "conversiones que devolvieron una clave distinta sin lanzar nada");
    }
}
