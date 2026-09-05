import React from 'react'

/**
 * El hueco del asistente, todavía sin conversación.
 *
 * La lógica —hablarle al modelo local de pitia y dejarlo usar las herramientas MCP que ya
 * existen— es la fase siguiente. Acá solo queda reservado el lugar y la forma de abrirlo y
 * cerrarlo, para que esa fase no tenga que tocar el layout.
 */
export default function CajonAsistente({ onCerrar }) {
  return (
    <aside className="cajon">
      <header>
        <strong>Asistente</strong>
        <button onClick={onCerrar} aria-label="Cerrar">✕</button>
      </header>
      <div className="cuerpo">
        <p className="tenue">
          Todavía no está conectado. Va a permitir preguntar en palabras corrientes, por
          ejemplo <em>«¿cuántas facturas tuvo Leda Flores Quesada este mes?»</em>, y responder
          consultando esta misma contabilidad.
        </p>
      </div>
    </aside>
  )
}
