import React, { useEffect, useState } from 'react'

/**
 * Un diagrama de Mermaid dentro de una respuesta.
 *
 * Mermaid se carga con `import()` dinámico y no arriba del archivo: pesa alrededor de un mega
 * comprimido —trae d3, y cada tipo de diagrama su propio analizador— y la enorme mayoría de las
 * respuestas del asistente son números y tablas. Así el costo lo paga solamente quien pide un
 * diagrama, y Vite lo parte en su propio trozo sin que haya que configurarle nada.
 *
 * Es el único lugar de la interfaz que inyecta HTML sin pasar por React. Está acotado a lo que
 * devuelve `mermaid.render`, con `securityLevel: 'strict'`, que es el modo en que Mermaid pasa
 * cada etiqueta por su propio saneador y no permite HTML adentro de los nodos. Lo que entra es
 * el texto del modelo, no de la contabilidad; y aun así se sanea.
 *
 * Un modelo chico escribe Mermaid inválido con frecuencia. Cuando eso pasa no se muestra un
 * error críptico: se muestra el código, que es información de verdad, con una nota.
 */

/**
 * Mermaid le pone al SVG un `max-width` en línea, del ancho que le dio su propio cálculo. Eso
 * es lo que hace que ampliar no amplíe nada: el atributo gana sobre cualquier hoja de estilo.
 * Se quita solo para la copia ampliada; la de la burbuja lo conserva.
 */
function sinTopeDeAncho(svg) {
  return svg.replace(/max-width:\s*[\d.]+px;?/g, '')
}

// Un identificador propio por diagrama. Mermaid lo usa para el elemento temporal donde dibuja,
// y dos diagramas con el mismo id se pisan.
let contador = 0

let iniciado = false

export default function Diagrama({ fuente }) {
  const [svg, setSvg] = useState(null)
  const [falla, setFalla] = useState(null)
  const [ampliado, setAmpliado] = useState(false)

  useEffect(() => {
    let vivo = true
    setSvg(null)
    setFalla(null)
    const id = `diagrama-${++contador}`

    import('mermaid')
      .then(({ default: mermaid }) => {
        if (!iniciado) {
          mermaid.initialize({
            startOnLoad: false,
            securityLevel: 'strict',
            // Sin esto, un diagrama inválido deja un cartel de error de Mermaid pegado al
            // final del documento, fuera del cajón y sin forma de quitarlo.
            suppressErrorRendering: true,
            theme: 'neutral',
            fontFamily: 'inherit',
          })
          iniciado = true
        }
        return mermaid.render(id, fuente)
      })
      .then((resultado) => { if (vivo) setSvg(resultado.svg) })
      .catch((e) => { if (vivo) setFalla(e?.message || String(e)) })

    return () => { vivo = false }
  }, [fuente])

  // Escape cierra la ampliación. Se registra solo mientras está abierta.
  useEffect(() => {
    if (!ampliado) return undefined
    const alTeclear = (evento) => { if (evento.key === 'Escape') setAmpliado(false) }
    window.addEventListener('keydown', alTeclear)
    return () => window.removeEventListener('keydown', alTeclear)
  }, [ampliado])

  if (falla) {
    return (
      <div className="diagrama fallido">
        <div className="tenue pequeno">No se pudo dibujar el diagrama; queda el código.</div>
        <pre className="bloque-codigo"><code>{fuente}</code></pre>
      </div>
    )
  }

  if (!svg) return <div className="diagrama tenue pequeno">dibujando el diagrama…</div>

  // El cajón mide 380 px: adentro el diagrama entra completo pero ilegible, y sirve para ver
  // la forma. Para leerlo se amplía sobre toda la ventana, que es donde el SVG se muestra a su
  // tamaño natural y, si aun así no cabe, se desplaza.
  return (
    <>
      <div
        className="diagrama"
        role="button"
        tabIndex={0}
        title="Ampliar el diagrama"
        onClick={() => setAmpliado(true)}
        onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') setAmpliado(true) }}
        dangerouslySetInnerHTML={{ __html: svg }}
      />
      <div className="tenue pequeno" style={{ marginTop: -4, marginBottom: 8 }}>
        tocá el diagrama para verlo en grande
      </div>
      {ampliado && (
        <div className="lupa" onClick={() => setAmpliado(false)}>
          <div className="lupa-lienzo" dangerouslySetInnerHTML={{ __html: sinTopeDeAncho(svg) }} />
        </div>
      )}
    </>
  )
}
