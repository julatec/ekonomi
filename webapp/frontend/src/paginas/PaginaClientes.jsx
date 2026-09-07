import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { api } from '../api/client.js'
import { useSesion } from '../estado/SesionContexto.jsx'
import { useDebounce } from '../hooks/useDebounce.js'
import { formatearEntero } from '../dominio/formato.js'

export default function PaginaClientes() {
  const { tenant, rango } = useSesion()
  const navegar = useNavigate()
  const [texto, setTexto] = useState('')
  const [pagina, setPagina] = useState(0)
  const nombre = useDebounce(texto, 300)

  // Los botones de reporte son <a href> directos a propósito (ver el comentario más abajo):
  // el navegador arma la descarga solo, sin fetch ni blob, así que no hay forma de saber
  // desde acá cuándo termina. Este estado es una aproximación visual nada más —"generando…"
  // por unos segundos tras el clic—, no una medición real del progreso.
  const [descargando, setDescargando] = useState(null)

  function marcarDescarga(clave) {
    setDescargando(clave)
    setTimeout(() => setDescargando((actual) => (actual === clave ? null : actual)), 8000)
  }

  // Cambiar el término y quedarse en la página 4 mostraría "sin resultados" sobre una
  // búsqueda que sí los tiene.
  React.useEffect(() => setPagina(0), [nombre, tenant])

  // El conteo de "Comprobantes" ahora es del rango de la barra superior, no de todo el
  // histórico -ver ClienteController.clientes()-, así que cambiar el rango tiene que traer
  // números nuevos. cambiarRango() ya invalida todas las queries al cambiar, pero el rango
  // igual va en la queryKey: sin eso, volver a esta pantalla después de cambiar el rango en
  // otra podría mostrar un resultado en caché de un rango que ya no es el actual.
  const consulta = useQuery({
    queryKey: ['clientes', tenant, nombre, pagina, rango.desde, rango.hasta],
    queryFn: () => api.clientes({ nombre, page: pagina, size: 20 }),
    // Sin esto la tabla parpadea a vacío entre páginas.
    placeholderData: keepPreviousData,
  })

  const datos = consulta.data

  return (
    <>
      <div className="buscador">
        <input
          type="search"
          placeholder="Buscar por nombre o cédula…"
          value={texto}
          onChange={(evento) => setTexto(evento.target.value)}
        />
        {consulta.isFetching && <span className="tenue pequeno">buscando…</span>}
      </div>

      {consulta.error && <div className="aviso error">{consulta.error.message}</div>}

      <div className="panel">
        <h2>
          Ventas y compras
          {datos ? <span className="tenue pequeno"> · {formatearEntero(datos.total)}</span> : null}
        </h2>
        <div className="desplazable">
          <table>
            <thead>
              <tr>
                <th>Cédula</th>
                <th>Nombre</th>
                <th style={{ textAlign: 'right' }} title="En el rango de la barra superior">
                  Comprobantes
                </th>
                <th>Reportes</th>
              </tr>
            </thead>
            <tbody>
              {datos?.contenido?.map((cliente) => (
                <tr key={cliente.numero}>
                  <td className="mono">{cliente.numero}</td>
                  <td>{cliente.nombre}</td>
                  <td className="monto">{formatearEntero(cliente.comprobantes)}</td>
                  <td className="pequeno">
                    {/* Botones y no enlaces de texto: bajan un `.xlsx` —el servidor manda
                        `Content-Disposition: attachment`— y una descarga no se parece a
                        navegar. Siguen siendo <a> por debajo para que el navegador haga la
                        descarga solo, sin pasar por fetch ni blobs.

                        Rutas absolutas: la aplicación se despliega como ROOT.war y estas
                        cuelgan de la raíz, no de la pantalla que las dibuja. El rango que
                        lleva el reporte es el de la barra superior. */}
                    {/* «SUS» ventas y «SUS» compras, y el posesivo no es un adorno.
                        `/report/sales?id=X` devuelve los comprobantes donde X es el EMISOR,
                        o sea lo que X vendió — que desde esta contabilidad suelen ser
                        compras. Rotularlo «ventas» a secas sobre la fila de un proveedor
                        hacía que el botón dijera lo contrario de lo que baja.
                        Es la misma convención que los chips de la búsqueda: relativa a la
                        cédula, no al dueño de la contabilidad —que la aplicación no conoce—.
                        Orden emisor-primero, igual que los chips. */}
                    <a
                      className="boton"
                      href={`/report/sales?id=${encodeURIComponent(cliente.numero)}`}
                      title={`Comprobantes donde ${cliente.nombre} es el EMISOR — lo que vendió. `
                        + `En el rango de la barra superior.`}
                      onClick={() => marcarDescarga(`${cliente.numero}-ventas`)}
                    >
                      {descargando === `${cliente.numero}-ventas` ? 'generando…' : '↓ sus ventas'}
                    </a>
                    <a
                      className="boton"
                      href={`/report/purchases?id=${encodeURIComponent(cliente.numero)}`}
                      title={`Comprobantes donde ${cliente.nombre} es el RECEPTOR — lo que compró. `
                        + `En el rango de la barra superior.`}
                      onClick={() => marcarDescarga(`${cliente.numero}-compras`)}
                    >
                      {descargando === `${cliente.numero}-compras` ? 'generando…' : '↓ sus compras'}
                    </a>
                    {/* El conteo de la columna y este enlace usan el mismo rango —el de la
                        barra superior—, así que el número de acá sí coincide con lo que
                        aparece al hacer clic. */}
                    <button
                      className="chip"
                      onClick={() =>
                        navegar(`/comprobantes?q=${encodeURIComponent(cliente.numero)}&campo=cedula`)
                      }
                      title="Comprobantes de esta contraparte, en el rango de la barra superior"
                    >
                      ver comprobantes
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {datos && datos.contenido.length === 0 && (
          <div className="vacio">No hay contrapartes que coincidan.</div>
        )}
      </div>

      {datos && datos.totalPaginas > 1 && (
        <div className="buscador" style={{ marginTop: 14 }}>
          <button disabled={pagina === 0} onClick={() => setPagina((p) => p - 1)}>
            ← Anterior
          </button>
          <span className="tenue pequeno">
            Página {datos.pagina + 1} de {datos.totalPaginas}
          </span>
          <button
            disabled={datos.pagina + 1 >= datos.totalPaginas}
            onClick={() => setPagina((p) => p + 1)}
          >
            Siguiente →
          </button>
        </div>
      )}
    </>
  )
}
