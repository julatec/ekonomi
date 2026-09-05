import React from 'react'
import { formatearMonto, conSigno } from '../dominio/formato.js'
import { tipo } from '../dominio/tiposDeComprobante.js'

/**
 * Una línea por moneda, nunca una cifra consolidada.
 *
 * El tipo de cambio está guardado por documento, así que sumar montos de monedas distintas
 * —o convertirlos con la tasa de hoy— daría un número que no cuadra con ningún asiento. Por
 * la misma razón las notas de crédito se muestran aparte en vez de netearse: netear esconde
 * el criterio detrás de un solo número.
 */
export default function BandaTotales({ lineas }) {
  if (!lineas?.length) return null

  const monedas = [...new Set(lineas.map((linea) => linea.moneda || 'CRC'))]

  return (
    <div className="panel">
      <h2>Totales del filtro</h2>
      <div className="desplazable">
        <table>
          <thead>
            <tr>
              <th>Moneda</th>
              <th>Tipo</th>
              <th style={{ textAlign: 'right' }}>Comprobantes</th>
              <th style={{ textAlign: 'right' }}>Impuesto</th>
              <th style={{ textAlign: 'right' }}>Total</th>
            </tr>
          </thead>
          <tbody>
            {monedas.map((moneda) => {
              const deLaMoneda = lineas.filter((l) => (l.moneda || 'CRC') === moneda)
              return deLaMoneda.map((linea, indice) => {
                const signo = tipo(linea.tipo).signo
                return (
                  <tr key={`${moneda}-${linea.tipo}`}>
                    {indice === 0 ? (
                      <td rowSpan={deLaMoneda.length} className="mono">{moneda}</td>
                    ) : null}
                    <td>
                      {tipo(linea.tipo).etiqueta}
                      {signo < 0 && <span className="tenue pequeno"> (resta)</span>}
                    </td>
                    <td className="monto">{linea.comprobantes}</td>
                    <td className={`monto ${signo < 0 ? 'negativo' : ''}`}>
                      {formatearMonto(conSigno(linea.totalImpuesto, linea.tipo), moneda)}
                    </td>
                    <td className={`monto ${signo < 0 ? 'negativo' : ''}`}>
                      {formatearMonto(conSigno(linea.totalComprobante, linea.tipo), moneda)}
                    </td>
                  </tr>
                )
              })
            })}
          </tbody>
        </table>
      </div>
    </div>
  )
}
