package name.julatec.ekonomi.mcp;

import io.modelcontextprotocol.spec.McpSchema;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import name.julatec.ekonomi.accounting.Voucher;
import name.julatec.ekonomi.actividad.ActividadEconomica;
import name.julatec.ekonomi.actividad.ActividadEconomicaRepository;
import name.julatec.ekonomi.cabys.CabysItem;
import name.julatec.ekonomi.cabys.CabysItemRepository;
import name.julatec.ekonomi.cabys.CabysVersion;
import name.julatec.ekonomi.cabys.CabysVersionRepository;
import name.julatec.ekonomi.report.ComprobanteReportService;
import name.julatec.ekonomi.security.User;
import name.julatec.ekonomi.storage.StorageConfig;
import name.julatec.ekonomi.tribunet.storage.ElectronicReceipt;
import name.julatec.ekonomi.tribunet.storage.FacturaRepository;
import name.julatec.util.algebraic.Interval;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Las herramientas MCP de Ekonomi.
 * <p>
 * Portadas del servidor en Python que vivía en {@code ekonomi/mcp/}. La
 * diferencia de fondo no es el lenguaje: aquel entraba por MySQL y reconstruía
 * los nombres de columna leyendo {@code information_schema}, porque desde afuera
 * no tenía el modelo. Este corre dentro de la aplicación, así que consulta las
 * mismas entidades JPA que usa el webapp y puede además generar los reportes con
 * el mismo código que sirve la pantalla de descargas.
 * <p>
 * Todas son de solo lectura. El tenant nunca se toma por bueno: lo valida
 * {@link AccesoTenant} contra el certificado presentado.
 */
@Component
public class EkonomiMcpTools {

    /** Tope duro de filas por respuesta, aunque pidan más. */
    private static final int TOPE_FILAS = 500;
    private static final int FILAS_POR_DEFECTO = 50;

    private static final String XLSX =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * La base de seguridad y sus tablas: vetadas en {@code consulta_sql}.
     * {@code inbox} guarda credenciales de correo y {@code users} las identidades.
     */
    private static final Pattern VETADOS =
            Pattern.compile("ekonomi_primary|\\binbox\\b|\\busers?\\b|\\bissuer\\b", Pattern.CASE_INSENSITIVE);

    private static final Pattern NO_LECTURA = Pattern.compile(
            "(?i)\\b(insert|update|delete|replace|drop|alter|create|truncate|grant|revoke|"
                    + "call|handler|load|outfile|dumpfile|lock|unlock|set|use|into)\\b");

    private final AccesoTenant acceso;
    private final BusquedaComprobantes busqueda;
    private final ResumenComprobantes resumen;
    private final ComprobanteReportService reportes;
    private final FacturaRepository facturas;
    private final CabysItemRepository cabysItems;
    private final CabysVersionRepository cabysVersiones;
    private final ActividadEconomicaRepository actividades;

    @PersistenceContext(unitName = StorageConfig.PERSISTENCE_UNIT)
    private EntityManager entityManager;

    public EkonomiMcpTools(
            AccesoTenant acceso,
            BusquedaComprobantes busqueda,
            ResumenComprobantes resumen,
            ComprobanteReportService reportes,
            FacturaRepository facturas,
            CabysItemRepository cabysItems,
            CabysVersionRepository cabysVersiones,
            ActividadEconomicaRepository actividades) {
        this.acceso = acceso;
        this.busqueda = busqueda;
        this.resumen = resumen;
        this.reportes = reportes;
        this.facturas = facturas;
        this.cabysItems = cabysItems;
        this.cabysVersiones = cabysVersiones;
        this.actividades = actividades;
    }

    private static int limite(Integer pedido) {
        if (pedido == null || pedido <= 0) {
            return FILAS_POR_DEFECTO;
        }
        return Math.min(pedido, TOPE_FILAS);
    }

    private static Set<String> tipos(String lista) {
        if (lista == null || lista.isBlank()) {
            return Set.of();
        }
        final Set<String> pedidos = new LinkedHashSet<>();
        for (String tipo : lista.split(",")) {
            final String limpio = tipo.trim().toLowerCase();
            if (!limpio.isEmpty()) {
                if (!BusquedaComprobantes.TIPOS.contains(limpio)) {
                    throw new IllegalArgumentException(
                            "Tipo de comprobante desconocido: " + limpio
                                    + ". Válidos: " + String.join(", ", BusquedaComprobantes.TIPOS));
                }
                pedidos.add(limpio);
            }
        }
        return pedidos;
    }

    // ------------------------------------------------------------------
    // Orientación
    // ------------------------------------------------------------------

    @McpTool(name = "listar_tenants",
            description = "Las contabilidades que el certificado presentado puede abrir. "
                    + "Empezar por acá: el resto de herramientas exige un `tenant` de esta lista.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public Map<String, Object> listarTenants() {
        final User usuario = acceso.usuario();
        final Map<String, Object> salida = new LinkedHashMap<>();
        salida.put("usuario", usuario.getUsername());
        salida.put("nombre", usuario.getDisplayName());
        salida.put("tenants", acceso.tenantsDelUsuario());
        return salida;
    }

    @McpTool(name = "estado_ekonomi",
            description = "Diagnóstico: quién está autenticado, qué contabilidades tiene y si la "
                    + "base responde. Usarlo primero cuando otra herramienta falle.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public Map<String, Object> estadoEkonomi(
            @McpToolParam(description = "Contabilidad a probar. Si se omite, solo se reporta la identidad.",
                    required = false) String tenant) {
        final Map<String, Object> salida = new LinkedHashMap<>();
        final User usuario = acceso.usuario();
        salida.put("usuario", usuario.getUsername());
        salida.put("tenants_disponibles", acceso.tenantsDelUsuario());
        salida.put("tipos_de_comprobante", BusquedaComprobantes.TIPOS);
        if (tenant == null || tenant.isBlank()) {
            salida.put("base_probada", false);
            return salida;
        }
        try {
            final Map<String, Long> conteos = acceso.en(tenant, this::conteosPorTipo);
            salida.put("base_probada", true);
            salida.put("tenant", tenant);
            salida.put("comprobantes_por_tipo", conteos);
        } catch (RuntimeException e) {
            salida.put("base_probada", false);
            salida.put("tenant", tenant);
            salida.put("error", String.valueOf(e.getMessage()));
        }
        return salida;
    }

    private Map<String, Long> conteosPorTipo() {
        final FiltroComprobantes ninguno = FiltroComprobantes.de(null, null, null, null, null, null, null, null, null, null, null, null);
        return busqueda.buscar(ninguno, Set.of(), 1).porTipo();
    }

    @McpTool(name = "esquema",
            description = "Los tipos de comprobante disponibles, cuántos hay de cada uno y qué campos "
                    + "trae cada fila de resultado. Útil antes de armar filtros o una consulta SQL.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public Map<String, Object> esquema(
            @McpToolParam(description = "Contabilidad, de `listar_tenants`.", required = true) String tenant) {
        return acceso.en(tenant, () -> {
            final Map<String, Object> salida = new LinkedHashMap<>();
            salida.put("tenant", tenant);
            salida.put("comprobantes_por_tipo", conteosPorTipo());
            salida.put("campos_de_resultado", List.of(
                    "tipo", "clave", "consecutivo", "fechaEmision",
                    "emisorNumero", "emisorNombre", "receptorNumero", "receptorNombre",
                    "codigoActividadEmisor", "codigoActividadReceptor",
                    "moneda", "tipoCambio", "totalGravado", "totalExento",
                    "totalImpuesto", "totalComprobante"));
            salida.put("tablas_sql", Map.of(
                    "factura", "factura", "factura_compra", "factura_compra",
                    "factura_exportacion", "factura_exportacion",
                    "nota_credito", "nota_credito", "nota_debito", "nota_debito"));
            salida.put("nota", "Los montos van en la moneda del documento; multiplicar por "
                    + "tipoCambio da colones. Las notas de crédito restan.");
            return salida;
        });
    }

    // ------------------------------------------------------------------
    // Búsqueda
    // ------------------------------------------------------------------

    @McpTool(name = "buscar_comprobantes",
            description = "Busca facturas, facturas de compra y de exportación, y notas de crédito y "
                    + "débito. Los filtros son opcionales y se combinan con Y; hay que dar al menos uno. "
                    + "`cedula` y `nombre` buscan en ambas partes (emisor y receptor). Fechas AAAA-MM-DD.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public BusquedaComprobantes.Resultado buscarComprobantes(
            @McpToolParam(description = "Contabilidad, de `listar_tenants`.", required = true) String tenant,
            @McpToolParam(description = "Cédula de emisor o receptor, exacta.", required = false) String cedula,
            @McpToolParam(description = "Fragmento del nombre de emisor o receptor; no distingue mayúsculas.",
                    required = false) String nombre,
            @McpToolParam(description = "Clave del comprobante, total o parcial (50 dígitos).",
                    required = false) String clave,
            @McpToolParam(description = "Número consecutivo, exacto.", required = false) String consecutivo,
            @McpToolParam(description = "Fecha de emisión desde, AAAA-MM-DD.", required = false) String desde,
            @McpToolParam(description = "Fecha de emisión hasta, AAAA-MM-DD, inclusive.",
                    required = false) String hasta,
            @McpToolParam(description = "Total del comprobante mínimo, en la moneda del documento.",
                    required = false) String montoMinimo,
            @McpToolParam(description = "Total del comprobante máximo.", required = false) String montoMaximo,
            @McpToolParam(description = "Código de moneda, por ejemplo CRC o USD.", required = false) String moneda,
            @McpToolParam(description = "Código de actividad económica, 6 dígitos, de emisor o receptor. "
                    + "Solo existe desde v4.3 de Hacienda.", required = false) String codigoActividad,
            @McpToolParam(description = "Tipos a consultar, separados por coma. Por omisión, los cinco.",
                    required = false) String tiposDeComprobante,
            @McpToolParam(description = "Máximo de filas; por omisión 50, tope 500.",
                    required = false) Integer limite) {

        final FiltroComprobantes filtro = FiltroComprobantes.de(clave, consecutivo, cedula, nombre, null, null, desde, hasta, montoMinimo, montoMaximo, moneda, codigoActividad);
        if (filtro.vacio()) {
            throw new IllegalArgumentException(
                    "Hace falta al menos un filtro. Sin ninguno esto devolvería un recorte arbitrario "
                            + "de toda la contabilidad. Para ver volúmenes, usar `esquema` o `resumen_periodo`.");
        }
        final Set<String> tipos = tipos(tiposDeComprobante);
        final int tope = limite(limite);
        return acceso.en(tenant, () -> busqueda.buscar(filtro, tipos, tope));
    }

    @McpTool(name = "detalle_comprobante",
            description = "Un comprobante por su clave exacta, con el XML original de Hacienda. "
                    + "Busca en los cinco tipos.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public Map<String, Object> detalleComprobante(
            @McpToolParam(description = "Contabilidad, de `listar_tenants`.", required = true) String tenant,
            @McpToolParam(description = "Clave del comprobante: 50 dígitos, exacta.", required = true) String clave,
            @McpToolParam(description = "Incluir el XML completo. Son decenas de kilobytes; "
                    + "por omisión no se incluye.", required = false) Boolean incluirXml) {

        if (clave == null || clave.isBlank()) {
            throw new IllegalArgumentException("Falta la clave. Se obtiene de `buscar_comprobantes`.");
        }
        final boolean conXml = Boolean.TRUE.equals(incluirXml);
        return acceso.en(tenant, () -> {
            final Optional<Map.Entry<String, ElectronicReceipt>> encontrado =
                    busqueda.porClave(clave.trim());
            if (encontrado.isEmpty()) {
                throw new IllegalArgumentException(
                        "No hay ningún comprobante con esa clave en " + tenant
                                + ". Verificar los 50 dígitos, o buscarla parcialmente con "
                                + "`buscar_comprobantes` usando el parámetro `clave`.");
            }
            final Map<String, Object> salida = new LinkedHashMap<>();
            final ComprobanteResumen fila =
                    ComprobanteResumen.de(encontrado.get().getKey(), encontrado.get().getValue());
            salida.put("comprobante", fila);
            if (conXml) {
                salida.put("xml", encontrado.get().getValue().getDocumento().getDocument());
            } else {
                salida.put("xml_omitido", "Volver a pedir con incluir_xml=true para traer el XML.");
            }
            return salida;
        });
    }

    @McpTool(name = "resumen_periodo",
            description = "Conteos y sumas por tipo de comprobante y moneda, con los mismos filtros que "
                    + "`buscar_comprobantes`. No convierte monedas ni netea las notas de crédito: "
                    + "las reporta aparte para que el criterio contable lo ponga quien consulta.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public Map<String, Object> resumenPeriodo(
            @McpToolParam(description = "Contabilidad, de `listar_tenants`.", required = true) String tenant,
            @McpToolParam(description = "Cédula de emisor o receptor.", required = false) String cedula,
            @McpToolParam(description = "Fragmento del nombre de emisor o receptor.", required = false) String nombre,
            @McpToolParam(description = "Desde, AAAA-MM-DD.", required = false) String desde,
            @McpToolParam(description = "Hasta, AAAA-MM-DD, inclusive.", required = false) String hasta,
            @McpToolParam(description = "Tipos separados por coma; por omisión los cinco.",
                    required = false) String tiposDeComprobante) {

        final FiltroComprobantes filtro = FiltroComprobantes.de(null, null, cedula, nombre, null, null, desde, hasta, null, null, null, null);
        final Set<String> tipos = tipos(tiposDeComprobante);
        return acceso.en(tenant, () -> {
            final List<ResumenComprobantes.Linea> lineas = resumen.resumen(filtro, tipos);
            final Map<String, Object> salida = new LinkedHashMap<>();
            salida.put("tenant", tenant);
            salida.put("lineas", lineas);
            salida.put("nota", "Las notas de crédito restan del total facturado. Los montos están en "
                    + "la moneda de cada documento y no se consolidaron.");
            return salida;
        });
    }

    @McpTool(name = "clientes_frecuentes",
            description = "Emisores y receptores que aparecen en las facturas, con cuántas tiene cada uno. "
                    + "Sirve para encontrar la cédula exacta a partir de un nombre aproximado.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public Map<String, Object> clientesFrecuentes(
            @McpToolParam(description = "Contabilidad, de `listar_tenants`.", required = true) String tenant,
            @McpToolParam(description = "Filtrar por fragmento del nombre; sin él, salen todos.",
                    required = false) String nombre,
            @McpToolParam(description = "Máximo de filas; por omisión 50, tope 500.",
                    required = false) Integer limite) {

        final int tope = limite(limite);
        final String filtro = nombre == null ? null : nombre.trim().toLowerCase();
        return acceso.en(tenant, () -> {
            final List<Map<String, Object>> clientes = new ArrayList<>();
            for (Object[] fila : facturas.getClients()) {
                final String numero = (String) fila[0];
                final BigDecimal cuantas = (BigDecimal) fila[1];
                final String nombreCliente = String.valueOf(fila[2]);
                if (filtro != null && !nombreCliente.toLowerCase().contains(filtro)) {
                    continue;
                }
                final Map<String, Object> cliente = new LinkedHashMap<>();
                cliente.put("numero", numero);
                cliente.put("nombre", nombreCliente);
                cliente.put("comprobantes", cuantas);
                clientes.add(cliente);
            }
            clientes.sort((a, b) -> ((BigDecimal) b.get("comprobantes"))
                    .compareTo((BigDecimal) a.get("comprobantes")));
            final Map<String, Object> salida = new LinkedHashMap<>();
            salida.put("tenant", tenant);
            salida.put("total", clientes.size());
            salida.put("clientes", clientes.size() > tope ? clientes.subList(0, tope) : clientes);
            return salida;
        });
    }

    // ------------------------------------------------------------------
    // Reportes
    // ------------------------------------------------------------------

    private McpSchema.CallToolResult reporte(
            String tenant, String cedula, String desde, String hasta, String formato, boolean compras) {

        if (cedula == null || cedula.isBlank()) {
            throw new IllegalArgumentException(
                    "Falta la cédula de quien reporta. Es la de la empresa o persona cuya contabilidad "
                            + "se está armando; `clientes_frecuentes` ayuda a encontrarla.");
        }
        final Date lower = FiltroComprobantes.fecha("desde", desde);
        final Date upper = FiltroComprobantes.fechaFinal("hasta", hasta);
        if (lower == null || upper == null) {
            throw new IllegalArgumentException("Hay que dar `desde` y `hasta`, ambas AAAA-MM-DD.");
        }
        final boolean comoArchivo = "xlsx".equalsIgnoreCase(formato);

        return acceso.en(tenant, () -> {
            final Interval<Date> rango = Interval.of(lower, upper);
            final Iterable<Voucher> vouchers = compras
                    ? reportes.purchases(cedula.trim(), rango)
                    : reportes.sales(cedula.trim(), rango);

            final List<Map<String, Object>> filas = new ArrayList<>();
            for (Voucher voucher : vouchers) {
                final Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("fecha", voucher.getFecha());
                fila.put("consecutivo", voucher.getConsecutivo());
                fila.put("emisorNumero", voucher.getEmisorNumero());
                fila.put("emisorNombre", voucher.getEmisorNombre());
                fila.put("receptorNumero", voucher.getReceptorNumero());
                fila.put("receptorNombre", voucher.getReceptorNombre());
                fila.put("totalComprobante", voucher.getTotalComprobante());
                fila.put("clave", voucher.getClave());
                filas.add(fila);
            }

            final String nombre = String.format("%s-%s-%s-a-%s.xlsx",
                    compras ? "compras" : "ventas", cedula.trim(), desde, hasta);

            final Map<String, Object> estructurado = new LinkedHashMap<>();
            estructurado.put("tenant", tenant);
            estructurado.put("reporte", compras ? "compras" : "ventas");
            estructurado.put("cedula", cedula.trim());
            estructurado.put("desde", desde);
            estructurado.put("hasta", hasta);
            estructurado.put("filas", filas.size());
            estructurado.put("archivo", nombre);

            final McpSchema.CallToolResult.Builder resultado = McpSchema.CallToolResult.builder();

            if (comoArchivo) {
                final byte[] xlsx = aXlsx(vouchers);
                estructurado.put("bytes", xlsx.length);
                resultado.addContent(new McpSchema.EmbeddedResource(null,
                        new McpSchema.BlobResourceContents(
                                "ekonomi://reporte/" + nombre, XLSX,
                                Base64.getEncoder().encodeToString(xlsx))));
                resultado.addTextContent(String.format(
                        "Reporte de %s de %s, %s a %s: %d filas, %d bytes de xlsx adjuntos como %s.",
                        compras ? "compras" : "ventas", cedula.trim(), desde, hasta,
                        filas.size(), xlsx.length, nombre));
            } else {
                estructurado.put("detalle", filas);
                resultado.addTextContent(String.format(
                        "Reporte de %s de %s, %s a %s: %d filas. "
                                + "Volver a pedir con formato=\"xlsx\" para el archivo.",
                        compras ? "compras" : "ventas", cedula.trim(), desde, hasta, filas.size()));
            }

            return resultado.structuredContent(estructurado).build();
        });
    }

    private static byte[] aXlsx(Iterable<Voucher> vouchers) {
        try (Workbook workbook = Voucher.toWorkbook(vouchers);
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            workbook.write(salida);
            return salida.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el xlsx: " + e.getMessage(), e);
        }
    }

    @McpTool(name = "reporte_compras",
            description = "El reporte auxiliar de compras de un período: el mismo que genera la pantalla "
                    + "de descargas del webapp. Por omisión devuelve las filas como datos; con "
                    + "formato=\"xlsx\" adjunta además el archivo Excel.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public McpSchema.CallToolResult reporteCompras(
            @McpToolParam(description = "Contabilidad, de `listar_tenants`.", required = true) String tenant,
            @McpToolParam(description = "Cédula de quien reporta.", required = true) String cedula,
            @McpToolParam(description = "Desde, AAAA-MM-DD.", required = true) String desde,
            @McpToolParam(description = "Hasta, AAAA-MM-DD, inclusive.", required = true) String hasta,
            @McpToolParam(description = "\"resumen\" (por omisión) o \"xlsx\".", required = false) String formato) {
        return reporte(tenant, cedula, desde, hasta, formato, true);
    }

    @McpTool(name = "reporte_ventas",
            description = "El reporte auxiliar de ventas de un período, equivalente a `reporte_compras` "
                    + "pero por el lado del emisor.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public McpSchema.CallToolResult reporteVentas(
            @McpToolParam(description = "Contabilidad, de `listar_tenants`.", required = true) String tenant,
            @McpToolParam(description = "Cédula de quien reporta.", required = true) String cedula,
            @McpToolParam(description = "Desde, AAAA-MM-DD.", required = true) String desde,
            @McpToolParam(description = "Hasta, AAAA-MM-DD, inclusive.", required = true) String hasta,
            @McpToolParam(description = "\"resumen\" (por omisión) o \"xlsx\".", required = false) String formato) {
        return reporte(tenant, cedula, desde, hasta, formato, false);
    }

    // ------------------------------------------------------------------
    // Escotilla SQL
    // ------------------------------------------------------------------

    /**
     * Deja pasar un SELECT y solo uno, o explica por qué no.
     * <p>
     * Es visible para las pruebas a propósito: estas barandas son lo único que
     * separa a {@code consulta_sql} de ser una consola SQL abierta sobre la
     * contabilidad, y merecen verificarse una por una.
     */
    static String sqlValidado(String sql, int tope) {
        final String limpio = sql == null ? "" : sql.strip().replaceAll(";\\s*$", "").strip();
        if (limpio.length() < 8) {
            throw new IllegalArgumentException("La consulta está vacía o es demasiado corta.");
        }
        if (limpio.contains(";")) {
            throw new IllegalArgumentException("Un solo statement por consulta: hay un ';' en el medio.");
        }
        if (!limpio.toLowerCase().startsWith("select")) {
            throw new IllegalArgumentException("Solo se aceptan SELECT.");
        }
        if (NO_LECTURA.matcher(limpio).find()) {
            throw new IllegalArgumentException(
                    "La consulta contiene palabras que no son de lectura pura. Solo SELECT, sin INTO ni SET.");
        }
        if (VETADOS.matcher(limpio).find()) {
            throw new IllegalArgumentException(
                    "Esa consulta toca la base de seguridad (ekonomi_primary / inbox / users / issuer): "
                            + "vetada por diseño, guarda credenciales de correo e identidades.");
        }
        return limpio.toLowerCase().matches("(?s).*\\blimit\\s+\\d+.*")
                ? limpio
                : limpio + " LIMIT " + tope;
    }

    @McpTool(name = "consulta_sql",
            description = "Un SELECT sobre la contabilidad indicada, para lo que las otras herramientas "
                    + "no cubren. Solo lectura: un statement, sin escrituras, con LIMIT forzado y sin "
                    + "acceso a la base de seguridad. Consultar `esquema` antes para los nombres.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    @Transactional(transactionManager = StorageConfig.TRANSACTION_MANAGER, readOnly = true)
    public Map<String, Object> consultaSql(
            @McpToolParam(description = "Contabilidad, de `listar_tenants`.", required = true) String tenant,
            @McpToolParam(description = "Un único SELECT, sin punto y coma final.", required = true) String sql,
            @McpToolParam(description = "LIMIT a imponer si la consulta no trae uno; por omisión 200, tope 500.",
                    required = false) Integer limite) {

        final int tope = Math.min(limite == null || limite <= 0 ? 200 : limite, TOPE_FILAS);
        final String consulta = sqlValidado(sql, tope);

        return acceso.en(tenant, () -> {
            @SuppressWarnings("unchecked")
            final List<Object[]> filas = entityManager
                    .createNativeQuery(consulta, Object[].class)
                    .setMaxResults(tope)
                    .getResultList();
            final Map<String, Object> salida = new LinkedHashMap<>();
            salida.put("tenant", tenant);
            salida.put("sql_ejecutado", consulta);
            salida.put("filas", filas.size());
            salida.put("resultados", filas.stream().map(Arrays::asList).toList());
            return salida;
        });
    }

    // ------------------------------------------------------------------
    // CABYS
    // ------------------------------------------------------------------

    private static final int CABYS_FILAS_POR_DEFECTO = 30;
    private static final int CABYS_TOPE_FILAS = 200;

    @McpTool(name = "consultar_cabys",
            description = "Busca en el Catálogo de Bienes y Servicios de Hacienda/BCCR, por código "
                    + "(completo o un prefijo, para ver toda una rama de la jerarquía) o por un fragmento "
                    + "de la descripción. No pertenece a ninguna contabilidad: es el mismo catálogo para "
                    + "todos los tenants.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public Map<String, Object> consultarCabys(
            @McpToolParam(description = "Código (13 dígitos o un prefijo) o texto de la descripción.",
                    required = true) String q,
            @McpToolParam(description = "Versión del catálogo; por omisión la más reciente cargada.",
                    required = false) String version,
            @McpToolParam(description = "Máximo de filas; por omisión 30, tope 200.",
                    required = false) Integer limite) {
        if (q == null || q.isBlank()) {
            throw new IllegalArgumentException("Hace falta `q`.");
        }
        final String versionEfectiva = version != null && !version.isBlank()
                ? version
                : cabysVersiones.masReciente()
                        .map(CabysVersion::getVersion)
                        .orElseThrow(() -> new IllegalStateException(
                                "No hay ninguna versión del catálogo CABYS cargada."));
        final int tope = Math.clamp(
                limite == null || limite <= 0 ? CABYS_FILAS_POR_DEFECTO : limite, 1, CABYS_TOPE_FILAS);
        final List<CabysItem> encontrados = cabysItems.buscar(
                versionEfectiva, q.trim(),
                org.springframework.data.domain.PageRequest.of(0, tope,
                        org.springframework.data.domain.Sort.by("id.codigo")));

        final Map<String, Object> salida = new LinkedHashMap<>();
        salida.put("version", versionEfectiva);
        salida.put("filas", encontrados.size());
        salida.put("items", encontrados.stream().map(item -> Map.of(
                "codigo", item.getCodigo(),
                "descripcion", item.getDescripcion(),
                "categoria1", item.getCategoria1() == null ? "" : item.getCategoria1(),
                "tarifa", item.isExento() ? "exento"
                        : item.getTarifa() == null ? "n/a" : item.getTarifa().toString()
        )).toList());
        return salida;
    }

    // ------------------------------------------------------------------
    // Actividades económicas (ATV Hacienda ↔ CIIU4 TRIBU-CR)
    // ------------------------------------------------------------------

    private static final int ACTIVIDAD_FILAS_POR_DEFECTO = 30;
    private static final int ACTIVIDAD_TOPE_FILAS = 200;

    @McpTool(name = "consultar_actividad",
            description = "Busca en la correspondencia de actividades económicas de Hacienda: el código "
                    + "ATV de 6 dígitos que trae cada comprobante (codigoActividadEmisor/Receptor) contra "
                    + "su subclase TRIBU-CR (CIIU4). Por código (prefijo) o por un fragmento del nombre de "
                    + "la actividad, en cualquiera de los dos nombres. No pertenece a ninguna contabilidad: "
                    + "es el mismo catálogo para todos los tenants.",
            annotations = @McpTool.McpAnnotations(
                    readOnlyHint = true, destructiveHint = false, idempotentHint = true, openWorldHint = false))
    public Map<String, Object> consultarActividad(
            @McpToolParam(description = "Código ATV (6 dígitos o un prefijo) o texto del nombre de la actividad.",
                    required = true) String q,
            @McpToolParam(description = "Máximo de filas; por omisión 30, tope 200.",
                    required = false) Integer limite) {
        if (q == null || q.isBlank()) {
            throw new IllegalArgumentException("Hace falta `q`.");
        }
        final int tope = Math.clamp(
                limite == null || limite <= 0 ? ACTIVIDAD_FILAS_POR_DEFECTO : limite, 1, ACTIVIDAD_TOPE_FILAS);
        final List<ActividadEconomica> encontradas = actividades.buscar(
                q.trim(),
                org.springframework.data.domain.PageRequest.of(0, tope,
                        org.springframework.data.domain.Sort.by("atv", "ciiu4")));

        final Map<String, Object> salida = new LinkedHashMap<>();
        salida.put("filas", encontradas.size());
        salida.put("actividades", encontradas.stream().map(actividad -> Map.of(
                "atv", actividad.getAtv(),
                "atvNombre", actividad.getAtvNombre(),
                "ciiu4", actividad.getCiiu4(),
                "ciiu4Nombre", actividad.getCiiu4Nombre(),
                "especialidad", actividad.getEspecialidad() == null ? "N/A" : actividad.getEspecialidad()
        )).toList());
        return salida;
    }
}
