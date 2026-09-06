import React, { useState } from 'react'
import { useQuery, keepPreviousData } from '@tanstack/react-query'
import { api } from '../api/client.js'
import { useDebounce } from '../hooks/useDebounce.js'

/**
 * Buscador del Catálogo de Bienes y Servicios de Hacienda/BCCR.
 * <p>
 * No filtra la contabilidad de nadie: el catálogo es un estándar público, el mismo para
 * cualquier tenant. Sirve para dos preguntas distintas con la misma caja: «¿qué código le
 * corresponde a esto que vendo?» (buscando por descripción) y «¿qué es el código que puso
 * el proveedor en la factura?» (buscando por número, o por un prefijo para ver toda la rama
 * de la jerarquía — son 9 niveles, codificados en el propio número).
 */
export default function PaginaCabys() {
  const [texto, setTexto] = useState('')
  const textoDiferido = useDebounce(texto, 300)

  const consulta = useQuery({
    queryKey: ['cabys', textoDiferido],
    queryFn: () => api.cabys({ q: textoDiferido }),
    enabled: textoDiferido.trim().length >= 2,
    placeholderData: keepPreviousData,
  })

  const version = useQuery({ queryKey: ['cabys-version'], queryFn: api.cabysVersiones })
  const vigente = version.data?.[0]

  const datos = consulta.data

  return (
    <>
      <div className="buscador">
        <input
          type="search"
          placeholder="Código (13 dígitos o un prefijo) o parte de la descripción…"
          autoFocus
          value={texto}
          onChange={(evento) => setTexto(evento.target.value)}
        />
        {consulta.isFetching && <span className="tenue pequeno">buscando…</span>}
      </div>

      {vigente && (
        <p className="tenue pequeno">
          Catálogo versión <strong className="mono">{vigente.version}</strong>, vigente desde{' '}
          {vigente.vigenteDesde}.
        </p>
      )}

      {textoDiferido.trim().length > 0 && textoDiferido.trim().length < 2 && (
        <div className="vacio">Escribí al menos dos caracteres.</div>
      )}

      {consulta.error && <div className="aviso error">{consulta.error.message}</div>}

      {datos && (
        <div className="panel">
          <h2>
            Códigos CABYS
            {datos.devueltos > 0 && <span className="tenue pequeno"> · {datos.devueltos}</span>}
          </h2>
          <div className="desplazable">
            <table>
              <thead>
                <tr>
                  <th>Código</th>
                  <th>Descripción</th>
                  <th>Categoría</th>
                  <th style={{ textAlign: 'right' }}>Tarifa IVA</th>
                </tr>
              </thead>
              <tbody>
                {datos.items.map((item) => (
                  <tr key={item.codigo}>
                    <td className="mono">{item.codigo}</td>
                    <td>{item.descripcion}</td>
                    <td className="tenue pequeno">{item.categoria1}</td>
                    <td className="monto">
                      {item.exento
                        ? 'exento'
                        : item.tarifa === null || item.tarifa === undefined
                          ? '—'
                          : `${(item.tarifa * 100).toFixed(item.tarifa * 100 % 1 === 0 ? 0 : 1)}%`}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          {datos.items.length === 0 && (
            <div className="vacio">Ningún código coincide con esa búsqueda.</div>
          )}
          {/* El tope existe —200 filas— y no hay paginación: un prefijo de un solo dígito
              trae miles de códigos, y esto es un buscador, no un listado completo del
              catálogo. Escribir un poco más acota. */}
          {datos.devueltos === 200 && (
            <div className="aviso">Hay más de 200 resultados; agregá más texto para acotar.</div>
          )}
        </div>
      )}
    </>
  )
}
