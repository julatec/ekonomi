import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { api } from '../api/client.js'
import { useSesion } from '../estado/SesionContexto.jsx'
import { useDebounce } from '../hooks/useDebounce.js'
import { formatearEntero } from '../dominio/formato.js'

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
                    {/* Absolutas y no relativas: la aplicación se despliega como ROOT.war y
                        estas rutas cuelgan de la raíz, no de la pantalla que las dibuja. El
                        rango que lleva el reporte es el de la barra superior. */}
                    <a href={`/report/purchases?id=${encodeURIComponent(cliente.numero)}`}>compras</a>
                    {' · '}
                    <a href={`/report/sales?id=${encodeURIComponent(cliente.numero)}`}>ventas</a>
                    {' · '}
                    <button
                      className="chip"
                      onClick={() =>
                        navegar(
                          `/comprobantes?q=${encodeURIComponent(cliente.numero)}&campo=cedula`,
                        )
                      }
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
