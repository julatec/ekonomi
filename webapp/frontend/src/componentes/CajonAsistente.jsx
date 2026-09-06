import React, { useEffect, useRef, useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { api } from '../api/client.js'
import { useSesion } from '../estado/SesionContexto.jsx'
import Marcado from './Marcado.jsx'

/**
 * El asistente: se le pregunta en palabras corrientes y contesta consultando la contabilidad
 * abierta con las mismas herramientas del servidor MCP.
 *
 * El historial vive acá, en el navegador, y viaja completo en cada pregunta. No hay sesión de
 * chat en el servidor: así el backend no guarda conversaciones de nadie y recargar la página
 * empieza limpio, que para una herramienta de consulta es lo que uno espera.
 *
 * El chip de herramientas debajo de cada respuesta no es decoración: dice qué se consultó para
 * llegar a ese número. Sin eso, una cifra del modelo es indistinguible de una inventada.
 */
export default function CajonAsistente({ onCerrar }) {
  const { tenant } = useSesion()
  const [mensajes, setMensajes] = useState([])
  const [texto, setTexto] = useState('')
  const [estado, setEstado] = useState(null)
  const fin = useRef(null)

  useEffect(() => {
    api.estadoDelChat().then(setEstado).catch(() => setEstado(null))
  }, [])

  useEffect(() => {
    fin.current?.scrollIntoView({ behavior: 'smooth' })
  }, [mensajes])

  const preguntar = useMutation({
    // Solo `user` y `assistant` viajan: un `system` a mitad de conversación hace que la
    // plantilla del modelo devuelva 500, y el servidor ya arma el suyo al principio.
    mutationFn: (pregunta) =>
      api.preguntar(
        pregunta,
        mensajes.filter((m) => m.rol === 'user' || m.rol === 'assistant')
          .map((m) => ({ rol: m.rol, texto: m.texto })),
      ),
    onSuccess: (r) =>
      setMensajes((previos) => [...previos, {
        rol: 'assistant',
        texto: r.ok ? r.texto : r.error,
        error: !r.ok,
        herramientas: r.herramientasUsadas || [],
      }]),
    onError: (e) =>
      setMensajes((previos) => [...previos, { rol: 'assistant', texto: e.message, error: true, herramientas: [] }]),
  })

  function enviar(evento) {
    evento.preventDefault()
    const pregunta = texto.trim()
    if (!pregunta || preguntar.isPending) return
    setMensajes((previos) => [...previos, { rol: 'user', texto: pregunta }])
    setTexto('')
    preguntar.mutate(pregunta)
  }

  const sugerencias = [
    `¿Cuántas facturas hubo este mes?`,
    `¿Cuánto le facturé a Auto Mercado este año?`,
    `Resumen del último trimestre por moneda`,
    `Diagrama de mis cinco clientes más frecuentes`,
  ]

  return (
    <aside className="cajon">
      <header>
        <strong>Asistente</strong>
        <span className="tenue pequeno"> · {tenant}</span>
        <button className="cerrar" onClick={onCerrar} aria-label="Cerrar">×</button>
      </header>

      <div className="conversacion">
        {mensajes.length === 0 && (
          <div className="tenue pequeno">
            <p>
              Preguntá sobre la contabilidad <strong>{tenant}</strong>. Consulta los datos de
              verdad: no estima ni recuerda cifras.
            </p>
            {sugerencias.map((s) => (
              <button key={s} className="chip" style={{ margin: '2px 4px 2px 0' }}
                      onClick={() => setTexto(s)}>
                {s}
              </button>
            ))}
            {estado && (
              <p style={{ marginTop: 10 }}>
                {estado.herramientas?.length} herramientas disponibles.
              </p>
            )}
          </div>
        )}

        {mensajes.map((m, i) => (
          <div key={i} className={`burbuja ${m.rol} ${m.error ? 'error' : ''}`}>
            {/* Solo la respuesta se interpreta como Markdown. La pregunta se muestra tal cual
                se escribió —si alguien buscó `**Flores**`, eso es lo que preguntó— y el error
                también, porque un mensaje de error no es un documento. */}
            {m.rol === 'assistant' && !m.error
              ? <Marcado texto={m.texto} />
              : <div className="literal">{m.texto}</div>}
            {m.herramientas?.length > 0 && (
              <div className="tenue pequeno" style={{ marginTop: 6 }}>
                consultó: {m.herramientas.join(' · ')}
              </div>
            )}
          </div>
        ))}

        {/* Puntos animados y no solo el texto "pensando…" quieto: la respuesta puede tardar
            varios segundos —el modelo corre a ~29 tokens/s, y una pregunta con herramientas
            de por medio suma más— y un texto inmóvil no distingue "está trabajando" de
            "se colgó". La animación es puro CSS, ver .puntos-pensando en estilos.css. */}
        {preguntar.isPending && (
          <div className="burbuja assistant tenue literal">
            pensando<span className="puntos-pensando"><span>.</span><span>.</span><span>.</span></span>
          </div>
        )}
        <div ref={fin} />
      </div>

      <form className="redactar" onSubmit={enviar}>
        <input
          type="text"
          placeholder="Preguntá algo…"
          value={texto}
          onChange={(e) => setTexto(e.target.value)}
          disabled={preguntar.isPending}
        />
        <button type="submit" disabled={preguntar.isPending || !texto.trim()}>Enviar</button>
      </form>
    </aside>
  )
}
