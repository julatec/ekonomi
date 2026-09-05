import React from 'react'
import { tipo } from '../dominio/tiposDeComprobante.js'

export default function EtiquetaTipo({ valor }) {
  const definicion = tipo(valor)
  return (
    <span
      className="etiqueta"
      style={{ color: definicion.color, background: definicion.fondo }}
      title={valor}
    >
      {definicion.etiqueta}
    </span>
  )
}
