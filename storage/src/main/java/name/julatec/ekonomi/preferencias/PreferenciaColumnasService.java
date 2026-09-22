package name.julatec.ekonomi.preferencias;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lee y guarda la preferencia de columnas de una persona.
 * <p>
 * Lo que devuelve siempre pasó por {@link ColumnasDeReporte#reconciliar}: quien lo consume
 * —el controlador del API y el que arma el {@code .xlsx}— nunca ve una columna que el catálogo
 * de hoy no tenga.
 * <p>
 * Sin {@code @Transactional} propio a propósito. Hay dos {@code PlatformTransactionManager}
 * marcados {@code @Primary} en la aplicación (uno por unidad de persistencia, ver
 * {@code SecurityConfig} y {@code StorageConfig}); los métodos del repositorio traen el suyo
 * por {@code transactionManagerRef}, y abrir una transacción acá encima obligaría a elegir a
 * mano cuál —o a que Spring eligiera mal.
 */
@Service
public class PreferenciaColumnasService {

    private static final Logger logger = LoggerFactory.getLogger(PreferenciaColumnasService.class);

    private final PreferenciaColumnasRepository repositorio;

    public PreferenciaColumnasService(PreferenciaColumnasRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** El orden completo —visibles y ocultas— de esa persona para ese reporte. */
    public List<ColumnaSeleccionada> columnasDe(String username, String reporte) {
        return ColumnasDeReporte.reconciliar(
                repositorio.findById(new PreferenciaColumnasId(username, reporte))
                        .map(PreferenciaColumnas::getColumnas)
                        .orElse(List.of()));
    }

    /**
     * Solo las visibles, en orden: lo que el libro necesita.
     * <p>
     * Que la lectura falle no puede dejar a nadie sin reporte —bajar el auxiliar es lo que más
     * se usa—, así que un fallo de la base se registra y se devuelve el catálogo completo, que
     * es el comportamiento de siempre.
     */
    public List<String> columnasVisibles(String username, String reporte) {
        try {
            return ColumnasDeReporte.visibles(columnasDe(username, reporte));
        } catch (Exception e) {
            logger.warn("No se pudo leer la preferencia de columnas de {} para {}; "
                    + "el reporte sale con el orden por omisión.", username, reporte, e);
            return ColumnasDeReporte.catalogo();
        }
    }

    /**
     * Guarda el orden y la visibilidad tal cual vienen. Quien llama ya validó contra el
     * catálogo: acá se guarda lo que se recibe y se devuelve reconciliado.
     */
    public List<ColumnaSeleccionada> guardar(String username, String reporte, List<ColumnaSeleccionada> columnas) {
        final PreferenciaColumnasId id = new PreferenciaColumnasId(username, reporte);
        final PreferenciaColumnas preferencia = repositorio.findById(id)
                .orElseGet(() -> new PreferenciaColumnas(id, List.of()));
        preferencia.setColumnas(columnas);
        return ColumnasDeReporte.reconciliar(repositorio.save(preferencia).getColumnas());
    }

    /**
     * Si esa persona guardó algo alguna vez. Lo usa la pantalla para decir si lo que muestra
     * es el orden por omisión o uno propio — que con el catálogo completo en la lista no se
     * puede distinguir mirando las columnas.
     */
    public boolean tienePreferencia(String username, String reporte) {
        return repositorio.existsById(new PreferenciaColumnasId(username, reporte));
    }

    /** Vuelve al orden por omisión borrando la fila, no guardando el catálogo entero. */
    public void restaurar(String username, String reporte) {
        repositorio.findById(new PreferenciaColumnasId(username, reporte))
                .ifPresent(repositorio::delete);
    }
}
