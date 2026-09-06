import React from 'react'
import { formatearEntero, formatearMonto } from '../dominio/formato.js'
import { analizarGrafico } from '../dominio/grafico.js'

/**
 * Un gráfico de barras horizontal dentro de una respuesta del asistente.
 *
 * A diferencia de `Diagrama.jsx` (Mermaid), esto no inyecta HTML ni carga ninguna librería:
 * el SVG lo arma React con elementos comunes (`<rect>`, `<text>`), así que el texto del
 * modelo —etiquetas de serie— pasa por el mismo escape automático que el resto de la
 * respuesta. No hace falta un saneador propio porque nunca hay HTML de por medio.
 *
 * El formato que espera, un bloque ```grafico``` con JSON:
 *   {"titulo": "...", "moneda": "CRC", "series": [{"etiqueta": "...", "valor": 123}]}
 * `moneda` es opcional: sin ella los valores se muestran como enteros (conteos), no como
 * plata. Ver el prompt de sistema en ChatController.java para el contrato exacto que se le
 * enseña al modelo.
 *
 * Solo barras horizontales, a propósito: es el único tipo que el modelo tiene instrucción de
 * producir. Un `tipo` desconocido, o un JSON que no calza con esta forma, cae al mismo
 * tratamiento que un Mermaid inválido —se muestra el código, no un error críptico—, porque un
 * modelo chico se equivoca de formato con frecuencia y el código roto sigue siendo
 * información real.
 */

const ANCHO = 320
const ALTO_FILA = 26
const ANCHO_ETIQUETA = 96
const ANCHO_VALOR = 70
const ANCHO_BARRA_MAX = ANCHO - ANCHO_ETIQUETA - ANCHO_VALOR - 12

function truncar(texto, limite = 16) {
  const cadena = String(texto ?? '')
  return cadena.length > limite ? `${cadena.slice(0, limite - 1)}…` : cadena
}

export default function Grafico({ fuente }) {
  let datos
  try {
    datos = analizarGrafico(fuente)
  } catch {
    return (
      <div className="grafico fallido">
        <div className="tenue pequeno">No se pudo dibujar el gráfico; queda el código.</div>
        <pre className="bloque-codigo"><code>{fuente}</code></pre>
      </div>
    )
  }

  const { titulo, moneda, series } = datos
  const formatear = (valor) => (moneda ? formatearMonto(valor, moneda) : formatearEntero(valor))
  // El máximo define la escala. Valor absoluto y sin barra hacia la izquierda para negativos
  // —no hace falta en esta primera versión: lo que el asistente grafica son montos o
  // conteos, siempre positivos—.
  const maximo = Math.max(1, ...series.map((s) => Math.abs(s.valor)))
  const inicioFilas = titulo ? 22 : 4
  const alto = inicioFilas + series.length * ALTO_FILA

  return (
    <div className="grafico">
      <div className="desplazable">
        <svg viewBox={`0 0 ${ANCHO} ${alto}`} width="100%" role="img" aria-label={titulo || 'Gráfico de barras'}>
          {titulo && <text x="0" y="14" className="grafico-titulo">{titulo}</text>}
          {series.map((s, i) => {
            const y = inicioFilas + i * ALTO_FILA
            const anchoBarra = Math.max(1, (Math.abs(s.valor) / maximo) * ANCHO_BARRA_MAX)
            return (
              <g key={i}>
                <title>{`${s.etiqueta}: ${formatear(s.valor)}`}</title>
                <text x="0" y={y + ALTO_FILA / 2 + 4} className="grafico-etiqueta">
                  {truncar(s.etiqueta)}
                </text>
                <rect
                  x={ANCHO_ETIQUETA}
                  y={y + 4}
                  width={anchoBarra}
                  height={ALTO_FILA - 10}
                  rx="3"
                  className="grafico-barra"
                />
                <text x={ANCHO_ETIQUETA + anchoBarra + 6} y={y + ALTO_FILA / 2 + 4} className="grafico-valor">
                  {formatear(s.valor)}
                </text>
              </g>
            )
          })}
        </svg>
      </div>
    </div>
  )
}
