import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { api } from '../api/client.js'
import { useSesion } from '../estado/SesionContexto.jsx'
import { useDebounce } from '../hooks/useDebounce.js'
import { formatearEntero } from '../dominio/formato.js'

/**
 * La factura electrónica arrancó en Costa Rica en 2018, así que nada puede ser anterior. Es un
 * piso, no una fecha real: sirve para que «ver comprobantes» abarque todo el histórico.
 */
const DESDE_SIEMPRE = '2015-01-01'

function hoy() {
  return new Date().toISOString().slice(0, 10)
}

export default function PaginaClientes() {
  const { tenant } = useSesion()
  const navegar = useNavigate()
  const [texto, setTexto] = useState('')
  const [pagina, setPagina] = useState(0)
  const nombre = useDebounce(texto, 300)

  // Cambiar el término y quedarse en la página 4 mostraría "sin resultados" sobre una
  // búsqueda que sí los tiene.
  React.useEffect(() => setPagina(0), [nombre, tenant])

  const consulta = useQuery({
    queryKey: ['clientes', tenant, nombre, pagina],
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
          Contrapartes
          {datos ? <span className="tenue pequeno"> · {formatearEntero(datos.total)}</span> : null}
        </h2>
        <div className="desplazable">
          <table>
            <thead>
              <tr>
                <th>Cédula</th>
                <th>Nombre</th>
                <th style={{ textAlign: 'right' }}>Comprobantes</th>
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
                    <a
                      className="boton"
                      href={`/report/purchases?id=${encodeURIComponent(cliente.numero)}`}
                      title="Descargar el .xlsx de compras del rango seleccionado"
                    >
                      ↓ compras
                    </a>
                    <a
                      className="boton"
                      href={`/report/sales?id=${encodeURIComponent(cliente.numero)}`}
                      title="Descargar el .xlsx de ventas del rango seleccionado"
                    >
                      ↓ ventas
                    </a>
                    {/* El conteo de la columna es de TODO el histórico —la consulta de
                        contrapartes no filtra por fecha—, así que el enlace tiene que llevar
                        un rango que lo cubra. Sin esto prometía «6.310» y al hacer clic
                        mostraba cero, porque se aplicaba el rango de la barra superior. */}
                    <button
                      className="chip"
                      onClick={() =>
                        navegar(
                          `/comprobantes?q=${encodeURIComponent(cliente.numero)}&campo=cedula`
                            + `&desde=${DESDE_SIEMPRE}&hasta=${hoy()}`,
                        )
                      }
                      title="Todos los comprobantes de esta contraparte, en cualquier fecha"
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
