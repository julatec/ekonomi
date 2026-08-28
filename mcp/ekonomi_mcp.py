#!/usr/bin/env python3
"""
Servidor MCP (stdio) para consultar los datos de Ekonomi.

Expone las facturas electrónicas, notas y resúmenes del MySQL multi-tenant de
Ekonomi como herramientas consultables en lenguaje natural, sin pasar por el
webapp. Solo lectura: cada conexión se abre con la transacción en READ ONLY y
ninguna herramienta acepta DML.

    listar_tenants        qué tenants hay configurados
    estado_ekonomi        diagnóstico: config, credencial, servidor, bases
    esquema               tablas de un tenant, o columnas de una tabla
    buscar_comprobantes   facturas/notas por cédula, rol y rango de fechas
    detalle_comprobante   un comprobante por clave (opcionalmente su XML)
    resumen_periodo       conteos y totales por tabla y moneda en un período
    clientes_frecuentes   emisores/receptores más frecuentes
    consulta_sql          SELECT libre con barandas (solo lectura, con LIMIT)

Decisiones de diseño:

- **Los nombres físicos de columna se descubren en runtime** (information_schema)
  en vez de asumirse. El módulo storage mezcla naming: las tablas tribunet usan
  snake_case (emisor_numero) y las de accounting quedan con el default de
  Hibernate puro, que puede conservar camelCase. Adivinar mal rompería todas
  las consultas; descubrir no cuesta nada y se cachea por conexión.
- **La base `ekonomi_primary` está vetada.** Contiene usuarios y la tabla
  inbox con credenciales de correo. Ninguna herramienta la toca, y
  consulta_sql rechaza cualquier referencia a ella.
- La credencial de MySQL se hereda del entorno del proceso (misma filosofía
  que el MCP de adjuntos de julatec-ops): nunca viaja por el protocolo ni
  aparece en la configuración del cliente.

Variables de entorno:
    EKONOMI_DB_HOST      default 127.0.0.1
    EKONOMI_DB_PORT      default 3306
    EKONOMI_DB_USER      default ekonomi
    EKONOMI_DB_PASS_ENV  nombre de la variable con la clave (default EKONOMI_DB_PASS)
    EKONOMI_DB_PREFIX    default ekonomi_
    EKONOMI_TENANTS      default julatec,tribuconta,agropag
"""

from __future__ import annotations

import os
import re
from datetime import date, datetime
from decimal import Decimal
from typing import Annotated, Any

from mcp.server.mcpserver import MCPServer
from mcp.server.mcpserver.exceptions import ToolError
from mcp.types import ToolAnnotations
from pydantic import Field

try:
    import pymysql
    import pymysql.cursors
except ImportError as e:  # pragma: no cover
    raise SystemExit(
        "Falta PyMySQL. Instalar con: .venv/bin/pip install pymysql"
    ) from e

# ---------------------------------------------------------------------------
# Configuración
# ---------------------------------------------------------------------------

HOST = os.environ.get("EKONOMI_DB_HOST", "127.0.0.1")
PORT = int(os.environ.get("EKONOMI_DB_PORT", "3306"))
USER = os.environ.get("EKONOMI_DB_USER", "ekonomi")
PASS_ENV = os.environ.get("EKONOMI_DB_PASS_ENV", "EKONOMI_DB_PASS")
PREFIX = os.environ.get("EKONOMI_DB_PREFIX", "ekonomi_")
TENANTS = [t.strip() for t in
           os.environ.get("EKONOMI_TENANTS", "julatec,tribuconta,agropag").split(",")
           if t.strip()]

# Tablas de comprobantes electrónicos (las que tienen clave de 50 dígitos)
TABLAS_DOC = ["factura", "factura_compra", "factura_exportacion",
              "nota_credito", "nota_debito"]

# La base de seguridad y sus tablas sensibles: vetadas por diseño.
VETADOS = re.compile(r"ekonomi_primary|\binbox\b|\busers?\b|\bissuer\b", re.I)

server = MCPServer(
    name="ekonomi",
    title="Ekonomi — facturas electrónicas",
    version="0.1.0",
    instructions=(
        "Consulta de solo lectura sobre el MySQL multi-tenant de Ekonomi "
        "(facturas electrónicas de Costa Rica, notas de crédito/débito, "
        "comprobantes manuales). Tenants: " + ", ".join(TENANTS) + ".\n\n"
        "Empezar con `estado_ekonomi` si algo falla, y con `esquema` antes de "
        "usar `consulta_sql`. Las fechas van AAAA-MM-DD. Los montos de los "
        "comprobantes están en la moneda del documento; multiplicar por "
        "tipo_cambio da colones.\n\n"
        "Nota contable: las notas de crédito restan. `resumen_periodo` reporta "
        "cada tabla por separado justamente para poder netear con criterio."
    ),
)


# ---------------------------------------------------------------------------
# Infraestructura de conexión e introspección
# ---------------------------------------------------------------------------

def _clave_db() -> str:
    clave = os.environ.get(PASS_ENV)
    if not clave:
        raise ToolError(
            f"La variable {PASS_ENV} no está definida en el entorno de este proceso. "
            f"Es la contraseña MySQL del usuario {USER!r}. Definirla en ~/.zshrc "
            f"(export {PASS_ENV}=\"...\") y relanzar el cliente MCP. "
            "Si el MySQL local corre con Docker, la clave está en el .env del repo ekonomi."
        )
    return clave


def _db_de(tenant: str) -> str:
    if tenant not in TENANTS:
        raise ToolError(
            f"Tenant desconocido: {tenant!r}. Configurados: {', '.join(TENANTS)}. "
            "Se puede ampliar con la variable EKONOMI_TENANTS."
        )
    return PREFIX + tenant


def _conectar(db: str | None = None):
    try:
        return pymysql.connect(
            host=HOST, port=PORT, user=USER, password=_clave_db(),
            database=db, connect_timeout=5, read_timeout=30,
            cursorclass=pymysql.cursors.DictCursor, autocommit=True,
            charset="utf8mb4",
        )
    except pymysql.err.OperationalError as e:
        codigo = e.args[0] if e.args else None
        if codigo in (2003, 2002):
            raise ToolError(
                f"No hay MySQL escuchando en {HOST}:{PORT}. Si es el entorno local de "
                "Ekonomi: el docker-compose vive en el branch `updates` del repo "
                "(el daemon de Docker también tiene que estar corriendo). "
                "Si es la BD del hosting, falta habilitar el acceso remoto a MySQL."
            ) from e
        if codigo == 1045:
            raise ToolError(
                f"MySQL rechazó las credenciales del usuario {USER!r}. Verificar "
                f"{PASS_ENV} en el entorno y el usuario en EKONOMI_DB_USER."
            ) from e
        if codigo == 1049:
            raise ToolError(
                f"La base {db!r} no existe en este servidor. `estado_ekonomi` lista "
                "las que sí están; puede que ese tenant nunca se haya inicializado "
                "(agropag es un tenant declarado pero sin base en el init local)."
            ) from e
        raise ToolError(f"Error de MySQL: {e}") from e


def _sesion_solo_lectura(conn):
    with conn.cursor() as c:
        c.execute("SET SESSION TRANSACTION READ ONLY")


def _columnas(conn, db: str, tabla: str) -> list[str]:
    with conn.cursor() as c:
        c.execute(
            "SELECT column_name AS c FROM information_schema.columns "
            "WHERE table_schema=%s AND table_name=%s ORDER BY ordinal_position",
            (db, tabla))
        return [r["c"] for r in c.fetchall()]


def _tablas(conn, db: str) -> dict[str, int]:
    with conn.cursor() as c:
        c.execute(
            "SELECT table_name AS t, table_rows AS n FROM information_schema.tables "
            "WHERE table_schema=%s ORDER BY table_name", (db,))
        return {r["t"]: r["n"] for r in c.fetchall()}


def _col(cols: list[str], *candidatas: str) -> str | None:
    """Primera columna existente entre las candidatas (case-insensitive)."""
    por_minuscula = {c.lower(): c for c in cols}
    for cand in candidatas:
        real = por_minuscula.get(cand.lower())
        if real:
            return real
    return None


def _v(x: Any) -> Any:
    if isinstance(x, Decimal):
        return float(x)
    if isinstance(x, (datetime, date)):
        return x.isoformat(sep=" ") if isinstance(x, datetime) else x.isoformat()
    if isinstance(x, (bytes, bytearray)):
        return f"<{len(x)} bytes>"
    return x


def _fila(r: dict) -> dict:
    return {k: _v(v) for k, v in r.items()}


def _validar_fecha(nombre: str, valor: str | None) -> str | None:
    if valor is None:
        return None
    try:
        datetime.strptime(valor, "%Y-%m-%d")
        return valor
    except ValueError:
        raise ToolError(f"{nombre} inválida: {valor!r}. Formato AAAA-MM-DD.")


# Mapa lógico → candidatas físicas para las tablas de comprobantes
CAND = {
    "clave": ("clave",),
    "fecha": ("fecha_emision", "fechaEmision"),
    "consecutivo": ("numero_consecutivo", "numeroConsecutivo"),
    "emisor": ("emisor_numero", "emisorNumero"),
    "emisor_nombre": ("emisor_nombre", "emisorNombre"),
    "receptor": ("receptor_numero", "receptorNumero"),
    "receptor_nombre": ("receptor_nombre", "receptorNombre"),
    "total": ("total_comprobante", "totalComprobante"),
    "moneda": ("codigo_moneda", "codigoMoneda", "currency"),
    "tipo_cambio": ("tipo_cambio", "tipoCambio"),
    "xml": ("document", "documento"),
}


def _mapa(conn, db: str, tabla: str) -> dict[str, str]:
    cols = _columnas(conn, db, tabla)
    return {logico: fisico
            for logico, cands in CAND.items()
            if (fisico := _col(cols, *cands))}


# ---------------------------------------------------------------------------
# Herramientas
# ---------------------------------------------------------------------------

@server.tool(
    title="Listar tenants",
    description="Los tenants configurados y a qué base de datos mapea cada uno. "
                "No toca la base: es solo la configuración del servidor.",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
def listar_tenants() -> dict[str, Any]:
    return {
        "tenants": {t: PREFIX + t for t in TENANTS},
        "servidor": f"{HOST}:{PORT}",
        "usuario": USER,
        "nota": ("ekonomi_primary (seguridad) está vetada por diseño: contiene "
                 "usuarios y credenciales de correo."),
    }


@server.tool(
    title="Estado de Ekonomi",
    description="Diagnóstico: configuración, si la credencial está disponible, si el "
                "servidor MySQL responde, qué bases ekonomi_* existen y cuántas tablas "
                "tiene cada una. Nunca devuelve la contraseña. Correr esto primero "
                "cuando algo falle.",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
def estado_ekonomi() -> dict[str, Any]:
    salida: dict[str, Any] = {
        "servidor": f"{HOST}:{PORT}",
        "usuario": USER,
        "variable_de_clave": PASS_ENV,
        "clave_disponible": bool(os.environ.get(PASS_ENV)),
        "tenants_configurados": TENANTS,
    }
    if not salida["clave_disponible"]:
        salida["listo"] = False
        salida["como_arreglarlo"] = (
            f"Definir {PASS_ENV} en el entorno (por ejemplo en ~/.zshrc) y relanzar "
            "el cliente MCP. La clave local está en el .env del repo ekonomi."
        )
        return salida
    conn = _conectar()
    try:
        _sesion_solo_lectura(conn)
        with conn.cursor() as c:
            c.execute("SELECT VERSION() AS v")
            salida["mysql_version"] = c.fetchone()["v"]
            c.execute("SHOW DATABASES")
            bases = [r[list(r)[0]] for r in c.fetchall()]
        propias = [b for b in bases if b.startswith(PREFIX)]
        detalle = {}
        for t in TENANTS:
            db = PREFIX + t
            detalle[t] = ({"base": db, "existe": True, "tablas": len(_tablas(conn, db))}
                          if db in propias else
                          {"base": db, "existe": False,
                           "nota": "El tenant está declarado pero la base no existe en este servidor."})
        salida["bases_encontradas"] = propias
        salida["por_tenant"] = detalle
        salida["listo"] = True
    finally:
        conn.close()
    return salida


@server.tool(
    title="Esquema de un tenant",
    description="Sin `tabla`: lista las tablas del tenant con su conteo aproximado de "
                "filas. Con `tabla`: sus columnas reales (nombre físico y tipo). "
                "Usarlo antes de consulta_sql: el naming mezcla snake_case y camelCase.",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
def esquema(
    tenant: Annotated[str, Field(description="julatec, tribuconta o agropag")],
    tabla: Annotated[str | None, Field(description="Tabla puntual; vacío = listar todas")] = None,
) -> dict[str, Any]:
    db = _db_de(tenant)
    conn = _conectar(db)
    try:
        _sesion_solo_lectura(conn)
        if tabla is None:
            return {"tenant": tenant, "base": db, "tablas": _tablas(conn, db)}
        with conn.cursor() as c:
            c.execute(
                "SELECT column_name AS columna, column_type AS tipo, is_nullable AS nulo, "
                "column_key AS llave FROM information_schema.columns "
                "WHERE table_schema=%s AND table_name=%s ORDER BY ordinal_position",
                (db, tabla))
            cols = c.fetchall()
        if not cols:
            disponibles = ", ".join(sorted(_tablas(conn, db)))
            raise ToolError(f"La tabla {tabla!r} no existe en {db}. Hay: {disponibles}")
        return {"tenant": tenant, "tabla": tabla, "columnas": [_fila(r) for r in cols]}
    finally:
        conn.close()


@server.tool(
    title="Buscar comprobantes",
    description="Busca facturas, facturas de compra, facturas de exportación y notas "
                "por rango de fechas y cédula (como emisor, receptor o cualquiera de "
                "los dos). Devuelve por cada hit: tipo, clave, fecha, emisor, receptor, "
                "total y moneda. Rol receptor = compras del tenant; emisor = ventas.",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
def buscar_comprobantes(
    tenant: Annotated[str, Field(description="julatec, tribuconta o agropag")],
    desde: Annotated[str | None, Field(description="Fecha mínima AAAA-MM-DD")] = None,
    hasta: Annotated[str | None, Field(description="Fecha máxima AAAA-MM-DD")] = None,
    cedula: Annotated[str | None, Field(description="Cédula a buscar")] = None,
    rol: Annotated[str, Field(description="emisor | receptor | cualquiera")] = "cualquiera",
    tipos: Annotated[list[str] | None, Field(
        description=f"Tablas a incluir, entre {TABLAS_DOC}. Vacío = todas")] = None,
    limite: Annotated[int, Field(ge=1, le=500)] = 50,
) -> dict[str, Any]:
    desde = _validar_fecha("desde", desde)
    hasta = _validar_fecha("hasta", hasta)
    if rol not in ("emisor", "receptor", "cualquiera"):
        raise ToolError(f"rol inválido: {rol!r}. Vale: emisor, receptor, cualquiera.")
    objetivo = tipos or TABLAS_DOC
    invalidas = [t for t in objetivo if t not in TABLAS_DOC]
    if invalidas:
        raise ToolError(f"Tipos inválidos: {invalidas}. Válidos: {TABLAS_DOC}")

    db = _db_de(tenant)
    conn = _conectar(db)
    resultados: list[dict] = []
    saltadas: list[str] = []
    try:
        _sesion_solo_lectura(conn)
        existentes = _tablas(conn, db)
        for tabla in objetivo:
            if tabla not in existentes:
                saltadas.append(f"{tabla} (no existe en {db})")
                continue
            m = _mapa(conn, db, tabla)
            if "clave" not in m or "fecha" not in m:
                saltadas.append(f"{tabla} (sin columnas clave/fecha reconocibles)")
                continue
            sel = [f"`{m[l]}` AS `{l}`" for l in
                   ("clave", "fecha", "consecutivo", "emisor", "emisor_nombre",
                    "receptor", "receptor_nombre", "total", "moneda", "tipo_cambio")
                   if l in m]
            cond, args = ["1=1"], []
            if desde:
                cond.append(f"`{m['fecha']}` >= %s"); args.append(desde)
            if hasta:
                cond.append(f"`{m['fecha']}` <= %s"); args.append(hasta + " 23:59:59")
            if cedula:
                if rol == "emisor" and "emisor" in m:
                    cond.append(f"`{m['emisor']}` = %s"); args.append(cedula)
                elif rol == "receptor" and "receptor" in m:
                    cond.append(f"`{m['receptor']}` = %s"); args.append(cedula)
                elif rol == "cualquiera":
                    partes = [f"`{m[r]}` = %s" for r in ("emisor", "receptor") if r in m]
                    if partes:
                        cond.append("(" + " OR ".join(partes) + ")")
                        args.extend([cedula] * len(partes))
            sql = (f"SELECT {', '.join(sel)} FROM `{tabla}` "
                   f"WHERE {' AND '.join(cond)} ORDER BY `{m['fecha']}` DESC LIMIT %s")
            with conn.cursor() as c:
                c.execute(sql, args + [limite])
                for r in c.fetchall():
                    fila = _fila(r)
                    fila["tipo"] = tabla
                    resultados.append(fila)
    finally:
        conn.close()

    resultados.sort(key=lambda r: str(r.get("fecha", "")), reverse=True)
    salida: dict[str, Any] = {
        "tenant": tenant,
        "total_devueltos": len(resultados[:limite]),
        "comprobantes": resultados[:limite],
    }
    if len(resultados) > limite:
        salida["nota"] = (f"Se juntaron {len(resultados)} filas entre todas las tablas y "
                          f"se devuelven las {limite} más recientes. Afinar filtros o subir limite.")
    if saltadas:
        salida["tablas_saltadas"] = saltadas
    return salida


@server.tool(
    title="Detalle de un comprobante",
    description="Busca la clave (50 dígitos) en todas las tablas de comprobantes y "
                "devuelve la fila completa de donde aparezca. Con incluir_xml=true "
                "adjunta además el XML original almacenado (recortado a max_xml_kb).",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
def detalle_comprobante(
    tenant: Annotated[str, Field(description="julatec, tribuconta o agropag")],
    clave: Annotated[str, Field(min_length=10, max_length=60,
                                description="Clave del comprobante")],
    incluir_xml: Annotated[bool, Field(description="Adjuntar el XML del LOB")] = False,
    max_xml_kb: Annotated[int, Field(ge=1, le=512)] = 64,
) -> dict[str, Any]:
    db = _db_de(tenant)
    conn = _conectar(db)
    try:
        _sesion_solo_lectura(conn)
        existentes = _tablas(conn, db)
        for tabla in TABLAS_DOC:
            if tabla not in existentes:
                continue
            m = _mapa(conn, db, tabla)
            if "clave" not in m:
                continue
            cols = _columnas(conn, db, tabla)
            col_xml = m.get("xml")
            sel = ", ".join(f"`{c}`" for c in cols if c != col_xml)
            with conn.cursor() as c:
                c.execute(f"SELECT {sel} FROM `{tabla}` WHERE `{m['clave']}` = %s", (clave,))
                fila = c.fetchone()
                if not fila:
                    continue
                salida = {"tenant": tenant, "tabla": tabla, "comprobante": _fila(fila)}
                if incluir_xml and col_xml:
                    c.execute(f"SELECT `{col_xml}` AS x FROM `{tabla}` "
                              f"WHERE `{m['clave']}` = %s", (clave,))
                    x = (c.fetchone() or {}).get("x")
                    if x is not None:
                        texto = (x.decode("utf-8", "replace")
                                 if isinstance(x, (bytes, bytearray)) else str(x))
                        tope = max_xml_kb * 1024
                        salida["xml"] = texto[:tope]
                        if len(texto) > tope:
                            salida["xml_truncado_en"] = f"{max_xml_kb} KB de {len(texto)//1024} KB"
                return salida
        raise ToolError(
            f"La clave {clave!r} no aparece en ninguna tabla de comprobantes de {db}. "
            "Verificar el tenant: la misma clave puede vivir en otro."
        )
    finally:
        conn.close()


@server.tool(
    title="Resumen de un período",
    description="Conteos y totales por tabla y moneda para una cédula en un rango de "
                "fechas. Rol receptor = compras; emisor = ventas. Devuelve el total en "
                "moneda del documento y, cuando hay tipo de cambio, el equivalente en "
                "colones. Las notas de crédito se reportan aparte y SIN negar: netear "
                "es decisión contable de quien lee.",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
def resumen_periodo(
    tenant: Annotated[str, Field(description="julatec, tribuconta o agropag")],
    cedula: Annotated[str, Field(description="Cédula del tenant en el rol dado")],
    desde: Annotated[str, Field(description="Fecha mínima AAAA-MM-DD")],
    hasta: Annotated[str, Field(description="Fecha máxima AAAA-MM-DD")],
    rol: Annotated[str, Field(description="receptor (compras) | emisor (ventas)")] = "receptor",
) -> dict[str, Any]:
    desde = _validar_fecha("desde", desde)
    hasta = _validar_fecha("hasta", hasta)
    if rol not in ("emisor", "receptor"):
        raise ToolError(f"rol inválido: {rol!r}. Vale: emisor o receptor.")
    db = _db_de(tenant)
    conn = _conectar(db)
    por_tabla: dict[str, Any] = {}
    try:
        _sesion_solo_lectura(conn)
        existentes = _tablas(conn, db)
        for tabla in TABLAS_DOC:
            if tabla not in existentes:
                continue
            m = _mapa(conn, db, tabla)
            if "fecha" not in m or "total" not in m:
                continue
            # En las notas la cédula puede estar en cualquiera de los dos roles
            # (el repo original usa un solo search para ambos).
            if tabla.startswith("nota_"):
                roles = [r for r in ("emisor", "receptor") if r in m]
            else:
                roles = [rol] if rol in m else []
            if not roles:
                continue
            cond_rol = "(" + " OR ".join(f"`{m[r]}` = %s" for r in roles) + ")"
            campos = [f"COUNT(*) AS n", f"SUM(`{m['total']}`) AS total"]
            grupo = ""
            if "moneda" in m:
                campos.append(f"`{m['moneda']}` AS moneda")
                grupo = f" GROUP BY `{m['moneda']}`"
            if "tipo_cambio" in m:
                campos.append(f"SUM(`{m['total']}` * `{m['tipo_cambio']}`) AS total_crc")
            sql = (f"SELECT {', '.join(campos)} FROM `{tabla}` "
                   f"WHERE {cond_rol} AND `{m['fecha']}` BETWEEN %s AND %s{grupo}")
            with conn.cursor() as c:
                c.execute(sql, [cedula] * len(roles) + [desde, hasta + " 23:59:59"])
                filas = [_fila(r) for r in c.fetchall() if r.get("n")]
            if filas:
                por_tabla[tabla] = filas
    finally:
        conn.close()
    return {
        "tenant": tenant, "cedula": cedula, "rol": rol,
        "periodo": f"{desde} → {hasta}",
        "por_tabla": por_tabla or "Sin comprobantes en el período.",
        "nota": ("Las notas de crédito RESTAN del total de las facturas; acá van sin "
                 "negar y separadas por tabla."),
    }


@server.tool(
    title="Clientes y proveedores frecuentes",
    description="Los emisores y receptores con más comprobantes en factura y "
                "factura_compra, con conteo. Útil como catálogo de contrapartes.",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
def clientes_frecuentes(
    tenant: Annotated[str, Field(description="julatec, tribuconta o agropag")],
    limite: Annotated[int, Field(ge=1, le=200)] = 30,
) -> dict[str, Any]:
    db = _db_de(tenant)
    conn = _conectar(db)
    piezas, args = [], []
    try:
        _sesion_solo_lectura(conn)
        existentes = _tablas(conn, db)
        for tabla in ("factura", "factura_compra"):
            if tabla not in existentes:
                continue
            m = _mapa(conn, db, tabla)
            for r in ("emisor", "receptor"):
                if r in m:
                    nom = m.get(f"{r}_nombre")
                    sel_nom = f"MAX(`{nom}`)" if nom else "NULL"
                    piezas.append(
                        f"SELECT `{m[r]}` AS cedula, {sel_nom} AS nombre, "
                        f"'{r}' AS rol, '{tabla}' AS tabla, COUNT(*) AS n "
                        f"FROM `{tabla}` GROUP BY `{m[r]}`")
        if not piezas:
            raise ToolError(f"Ni factura ni factura_compra existen en {db}.")
        sql = (" UNION ALL ".join(piezas)) + " ORDER BY n DESC LIMIT %s"
        with conn.cursor() as c:
            c.execute(sql, [limite])
            filas = [_fila(r) for r in c.fetchall()]
    finally:
        conn.close()
    return {"tenant": tenant, "contrapartes": filas}


@server.tool(
    title="Consulta SQL de solo lectura",
    description="Ejecuta UN SELECT sobre la base del tenant, con barandas: solo "
                "lectura (la sesión completa va en READ ONLY), un único statement, "
                "sin tocar ekonomi_primary ni tablas de seguridad, y con LIMIT "
                "obligatorio (se agrega si falta). Ver `esquema` primero: los nombres "
                "físicos de columna no siempre son los esperables.",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
def consulta_sql(
    tenant: Annotated[str, Field(description="julatec, tribuconta o agropag")],
    sql: Annotated[str, Field(min_length=8, description="Un SELECT")],
    limite: Annotated[int, Field(ge=1, le=1000,
                                 description="LIMIT a imponer si la consulta no trae uno")] = 200,
) -> dict[str, Any]:
    limpio = sql.strip().rstrip(";").strip()
    if ";" in limpio:
        raise ToolError("Un solo statement por consulta: hay un ';' en el medio.")
    if not re.match(r"(?is)^\s*select\b", limpio):
        raise ToolError("Solo se aceptan SELECT.")
    if re.search(r"(?i)\b(insert|update|delete|replace|drop|alter|create|truncate|"
                 r"grant|revoke|call|handler|load|outfile|dumpfile|lock|unlock|"
                 r"set|use|into)\b", limpio):
        raise ToolError("La consulta contiene palabras que no son de lectura pura. "
                        "Solo SELECT, sin INTO ni SET.")
    if VETADOS.search(limpio):
        raise ToolError("Esa consulta toca la base de seguridad (ekonomi_primary / "
                        "inbox / users): vetada por diseño — contiene credenciales.")
    if not re.search(r"(?i)\blimit\s+\d+", limpio):
        limpio += f" LIMIT {limite}"

    db = _db_de(tenant)
    conn = _conectar(db)
    try:
        _sesion_solo_lectura(conn)
        with conn.cursor() as c:
            try:
                c.execute(limpio)
            except pymysql.err.ProgrammingError as e:
                raise ToolError(
                    f"MySQL rechazó la consulta: {e.args[1] if len(e.args) > 1 else e}. "
                    "Revisar nombres con `esquema` — mezclan snake_case y camelCase."
                ) from e
            filas = [_fila(r) for r in c.fetchall()]
    finally:
        conn.close()
    # Recortar celdas enormes (por si seleccionaron el LOB del XML)
    for f in filas:
        for k, v in f.items():
            if isinstance(v, str) and len(v) > 2000:
                f[k] = v[:2000] + f"… (recortado de {len(v)} caracteres)"
    return {"tenant": tenant, "sql_ejecutado": limpio, "filas": len(filas),
            "resultados": filas}


if __name__ == "__main__":
    server.run("stdio")
