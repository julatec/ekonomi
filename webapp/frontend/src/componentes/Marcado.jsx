import React, { useMemo } from 'react'
import { analizar } from '../dominio/marcado.js'
import Diagrama from './Diagrama.jsx'

/**
 * Pinta el Markdown de una respuesta del asistente.
 *
 * El texto llega analizado como estructura —ver `dominio/marcado.js`— y acá se convierte en
 * elementos de React. Eso importa por seguridad y no por estilo: React escapa el texto de un
 * hijo, así que un nombre de emisor que traiga `<script>` se ve como `<script>` y no se
 * ejecuta. En ningún punto de este archivo se arma HTML a partir de la respuesta.
 *
 * (La excepción es el SVG de Mermaid, que sí se inyecta; vive aislada en `Diagrama.jsx` y
 * está explicada ahí.)
 */
export default function Marcado({ texto }) {
  // Memorizado por texto: la conversación entera se vuelve a pintar con cada respuesta nueva, y
  // sin esto cada mensaje anterior se re-analiza en cada vuelta.
  const bloques = useMemo(() => analizar(texto), [texto])
  return bloques.map((bloque, i) => <Bloque key={i} bloque={bloque} />)
}

function Bloque({ bloque }) {
  switch (bloque.tipo) {
    case 'parrafo':
      return <p><Partes partes={bloque.partes} /></p>

    case 'encabezado': {
      // Se empieza en h4 a propósito: esto vive adentro de una burbuja de chat, y un `#` de la
      // respuesta no puede competir con el h1 de la página ni con los h2 de los paneles. Se
      // conserva la jerarquía relativa, que es lo que el modelo quiso decir.
      const Etiqueta = `h${Math.min(3 + bloque.nivel, 6)}`
      return <Etiqueta><Partes partes={bloque.partes} /></Etiqueta>
    }

    case 'codigo':
      if (bloque.lenguaje === 'mermaid') return <Diagrama fuente={bloque.texto} />
      return (
        <pre className="bloque-codigo">
          <code>{bloque.texto}</code>
        </pre>
      )

    case 'lista': {
      const Etiqueta = bloque.ordenada ? 'ol' : 'ul'
      return (
        <Etiqueta>
          {bloque.items.map((contenido, i) => (
            <li key={i}>{contenido.map((b, j) => <Bloque key={j} bloque={b} />)}</li>
          ))}
        </Etiqueta>
      )
    }

    case 'tabla':
      // Envuelta en `desplazable`, la misma clase de las tablas de la aplicación: el cajón mide
      // 380 px y una tabla de cuatro columnas con montos no entra. Antes de desbordar, se
      // desplaza.
      return (
        <div className="desplazable">
          <table>
            <thead>
              <tr>
                {bloque.encabezados.map((celda, i) => (
                  <th key={i} style={{ textAlign: bloque.alineaciones[i] }}>
                    <Partes partes={celda} />
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {bloque.filas.map((fila, i) => (
                <tr key={i}>
                  {fila.map((celda, j) => (
                    <td key={j} style={{ textAlign: bloque.alineaciones[j] }}>
                      <Partes partes={celda} />
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )

    case 'cita':
      return (
        <blockquote>
          {bloque.bloques.map((b, i) => <Bloque key={i} bloque={b} />)}
        </blockquote>
      )

    case 'regla':
      return <hr />

    default:
      return null
  }
}

function Partes({ partes }) {
  return partes.map((parte, i) => {
    switch (parte.tipo) {
      case 'texto':
        return <React.Fragment key={i}>{parte.texto}</React.Fragment>
      case 'codigo':
        return <code key={i}>{parte.texto}</code>
      case 'fuerte':
        return <strong key={i}><Partes partes={parte.partes} /></strong>
      case 'enfasis':
        return <em key={i}><Partes partes={parte.partes} /></em>
      case 'tachado':
        return <s key={i}><Partes partes={parte.partes} /></s>
      case 'enlace': {
        // Un enlace interno navega acá; uno de afuera abre pestaña, y con `noreferrer` para no
        // contarle a un tercero desde qué contabilidad se salió.
        const afuera = /^https?:/i.test(parte.destino)
        return (
          <a
            key={i}
            href={parte.destino}
            {...(afuera ? { target: '_blank', rel: 'noreferrer noopener' } : {})}
          >
            <Partes partes={parte.partes} />
          </a>
        )
      }
      default:
        return null
    }
  })
}
