import React, { useState } from 'react'
import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { api } from '../api/client.js'
import { useDebounce } from '../hooks/useDebounce.js'

/**
 * Buscador de la correspondencia de actividades económicas: el código TRIBU-CR (CIIU4) que usa
 * Hacienda hoy, contra el código ATV (CIIU3) que traían los comprobantes antes de esa
 * migración.
 * <p>
 * No filtra la contabilidad de nadie: es un catálogo público de Hacienda, el mismo para
 * cualquier tenant — igual que CABYS. Sirve para la pregunta que hoy la pantalla de
 * comprobantes no puede contestar: «¿qué actividad es este código?». El backend prioriza un
 * código CIIU4 exacto sobre una coincidencia de texto —ver
 * {@code ActividadEconomicaRepository.buscar}—, así que buscar por código es lo más directo.
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
          placeholder="Código Tribu-CR (CIIU4), código ATV, o parte del nombre de la actividad…"
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
                  <th>ATV</th>
                  <th>Nombre ATV</th>
                  <th>Tribu-CR</th>
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
