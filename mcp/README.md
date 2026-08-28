# MCP de Ekonomi — consultas de facturas electrónicas

Servidor MCP (stdio) de **solo lectura** sobre el MySQL multi-tenant de Ekonomi.
Issue de tracking: julatec/julatec-ops#15 · Diseño CABYS (v0.2): julatec/julatec-ops#17

## Herramientas

`listar_tenants` · `estado_ekonomi` · `esquema` · `buscar_comprobantes` ·
`detalle_comprobante` · `resumen_periodo` · `clientes_frecuentes` · `consulta_sql`

Decisiones clave:
- **Nombres de columna por introspección** (information_schema), no adivinados: el
  storage mezcla snake_case (tablas tribunet) con el naming default de Hibernate.
- **`ekonomi_primary` vetada**: contiene usuarios y la tabla `inbox` con credenciales
  de correo. `consulta_sql` además rechaza cualquier SELECT que la mencione.
- Toda sesión corre con `SET SESSION TRANSACTION READ ONLY`.

## Instalación

```bash
cd mcp
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

Credencial: `export EKONOMI_DB_PASS="..."` en `~/.zshrc` (la del usuario `ekonomi`
del MySQL local; está en el `.env` del repo). Nunca va en `.mcp.json` ni en git.

## Contra qué base corre

- **Local:** el `docker-compose.yml` vive en el branch `updates` (Docker daemon
  apagado por defecto en esta Mac). Una base recién levantada está vacía: el esquema
  lo crea Hibernate al arrancar el webapp.
- **Producción:** el MySQL del hosting Tomcat (Mochahost, cuenta agropag). El acceso
  externo a MySQL quedó pendiente en el ticket MOCHA-LZN-192-62977 — sin eso, este
  servidor solo puede correr contra la copia local.

## v0.2 (bloqueada por CABYS)

`query_by_cabys` y `validar_tarifas` requieren las tablas `cabys_item` y
`linea_detalle` del diseño CABYS. Ver `julatec-ops/04-proyectos/ekonomi/cabys-diseno.md`.
