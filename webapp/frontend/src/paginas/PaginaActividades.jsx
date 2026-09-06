import React, { useState } from 'react'
import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { api } from '../api/client.js'
import { useDebounce } from '../hooks/useDebounce.js'

/**
 * Buscador de la correspondencia de actividades económicas: el código ATV que Hacienda pone en
 * cada comprobante (`codigoActividadEmisor`/`codigoActividadReceptor`) contra su subclase
 * TRIBU-CR (CIIU 4).
 * <p>
 * No filtra la contabilidad de nadie: es un catálogo público de Hacienda, el mismo para
 * cualquier tenant — igual que CABYS. Sirve para la pregunta que hoy la pantalla de
 * comprobantes no puede contestar: «¿qué actividad es el código 722003 que puso el emisor?».
 */
export default function PaginaActividades() {
  const [texto, setTexto] = useState('')
  const textoDiferido = useDebounce(texto, 300)

  const consulta = useQuery({
    queryKey: ['actividades', textoDiferido],
    queryFn: () => api.actividades({ q: textoDiferido }),
    enabled: textoDiferido.trim().length >= 2,
    placeholderData: keepPreviousData,
  })

  const datos = consulta.data

  return (
    <>
      <div className="buscador">
        <input
          type="search"
          placeholder="Código ATV (6 dígitos) o parte del nombre de la actividad…"
          autoFocus
          value={texto}
          onChange={(evento) => setTexto(evento.target.value)}
        />
        {consulta.isFetching && <span className="tenue pequeno">buscando…</span>}
      </div>

      {textoDiferido.trim().length > 0 && textoDiferido.trim().length < 2 && (
        <div className="vacio">Escribí al menos dos caracteres.</div>
      )}

      {consulta.error && <div className="aviso error">{consulta.error.message}</div>}

      {datos && (
        <div className="panel">
          <h2>
            Actividades económicas
            {datos.devueltos > 0 && <span className="tenue pequeno"> · {datos.devueltos}</span>}
          </h2>
          <div className="desplazable">
            <table>
              <thead>
                <tr>
                  <th>ATV (Hacienda)</th>
                  <th>Nombre ATV</th>
                  <th>CIIU4 (TRIBU-CR)</th>
                  <th>Nombre CIIU4</th>
                  <th>Especialidad</th>
                </tr>
              </thead>
              <tbody>
                {datos.actividades.map((actividad, indice) => (
                  // El par (atv, ciiu4) es la llave real -un mismo atv puede repetirse con
                  // varias subclases-, pero ninguno de los dos solo alcanza como key de React.
                  <tr key={`${actividad.atv}-${actividad.ciiu4}-${indice}`}>
                    <td className="mono">{actividad.atv}</td>
                    <td>{actividad.atvNombre}</td>
                    <td className="mono">{actividad.ciiu4}</td>
                    <td>{actividad.ciiu4Nombre}</td>
                    <td className="tenue pequeno">
                      {actividad.especialidad === 'N/A' ? '—' : actividad.especialidad}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {datos.actividades.length === 0 && (
            <div className="vacio">Ninguna actividad coincide con esa búsqueda.</div>
          )}
          {/* Igual que CABYS: tope de 200 filas sin paginación, es un buscador, no un listado
              completo del catálogo. */}
          {datos.devueltos === 200 && (
            <div className="aviso">Hay más de 200 resultados; agregá más texto para acotar.</div>
          )}
        </div>
      )}
    </>
  )
}
