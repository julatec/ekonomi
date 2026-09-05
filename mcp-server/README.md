# `mcp-server` — herramientas MCP de Ekonomi

Módulo Maven con las herramientas MCP que exponen la contabilidad de Ekonomi a un
cliente conversacional. Reemplaza al servidor en Python que vivía en `ekonomi/mcp/`.

## Qué cambió respecto al de Python

Aquel corría fuera de la aplicación y entraba directo a MySQL, así que tenía que
reconstruir los nombres de columna leyendo `information_schema`: no tenía el modelo
a mano. Este corre **dentro** de la aplicación y consulta las mismas entidades JPA
que el webapp. Eso trae tres cosas que antes no se podían:

- Los **reportes auxiliares** de compras y ventas, generados con el mismo código que
  sirve la pantalla de descargas — no una reimplementación que se desincroniza.
- **Autenticación por certificado**, la misma del resto de la aplicación.
- Búsquedas por **nombre, monto, consecutivo y clave parcial**, que el de Python no
  tenía porque los repositorios solo buscaban por cédula y rango de fechas.

## Dónde está cada cosa

| | |
|---|---|
| `EkonomiMcpTools` | Las diez herramientas |
| `AccesoTenant` | Valida el tenant contra el certificado. La pieza de seguridad |
| `BusquedaComprobantes` | Consulta los cinco tipos de comprobante y mezcla |
| `ResumenComprobantes` | Conteos y sumas por tipo y moneda |
| `FiltroComprobantes` | Convierte los parámetros de texto en criterios |
| `ComprobanteResumen` | La fila de resultado, sin el XML |

El **transporte no está acá**: lo pone `webapp`, que ya tiene la cadena de Spring
Security. Este módulo solo declara herramientas. Así el módulo no arrastra un
contenedor de servlets y la superficie expuesta queda en un solo lugar.

## Las herramientas

| | |
|---|---|
| `listar_tenants` | Las contabilidades que el certificado puede abrir. Empezar acá |
| `estado_ekonomi` | Diagnóstico cuando algo falla |
| `esquema` | Tipos de comprobante, cuántos hay y qué campos trae cada fila |
| `buscar_comprobantes` | Por cédula, nombre, clave, consecutivo, fechas, montos, moneda |
| `detalle_comprobante` | Uno por clave exacta, con el XML de Hacienda si se pide |
| `resumen_periodo` | Conteos y sumas por tipo y moneda |
| `clientes_frecuentes` | Emisores y receptores, para hallar la cédula desde un nombre |
| `reporte_compras` | El auxiliar de compras: filas, o el `.xlsx` con `formato="xlsx"` |
| `reporte_ventas` | Igual, por el lado del emisor |
| `consulta_sql` | Escotilla: un SELECT con barandas |

Todas son de solo lectura y todas exigen `tenant`.

## Seguridad

El endpoint cuelga de `/mcp` sobre el mismo Tomcat, así que queda detrás de
`anyRequest().authenticated()` con X.509: **no hay ruta abierta**. Para llegar hay
que presentar un certificado de firma digital reconocido, igual que para la pantalla
de reportes.

El `tenant` va como parámetro de cada herramienta —lo propone quien consulta—, así
que se valida contra `User.getDatasources()` antes de tocar nada. Es la misma regla
que la cookie `tenant` del webapp, con una diferencia: acá un tenant ajeno **falla**
en vez de caer al propio. Una herramienta que respondiera en silencio sobre otra
contabilidad daría una respuesta creíble y equivocada, que es peor que un error.
Lo fija `AccesoTenantTest`.

### `consulta_sql`

Es la herramienta con más filo: un SELECT libre. En el servidor de Python corría en
un proceso local contra un túnel; acá cuelga de un endpoint del servidor de
producción. Las barandas —un solo statement, solo SELECT, sin escrituras, `LIMIT`
forzado y veto sobre `ekonomi_primary` / `inbox` / `users` / `issuer`— están una por
una en `ConsultaSqlGuardasTest`.

Si algún día parece de más, quitarla es borrar un método: las otras nueve no dependen
de ella.

## Pruebas

```bash
JAVA_HOME=/opt/homebrew/opt/openjdk@25 mvn -pl mcp-server -am test
```

`HerramientasExpuestasTest` (en `webapp`) levanta el escáner real de Spring AI y
cuenta lo que publica. Sin él, un módulo que quedara fuera del *component scan*
seguiría construyendo un WAR válido y arrancando **sin una sola herramienta**.
