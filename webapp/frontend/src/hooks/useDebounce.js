import { useEffect, useState } from 'react'

/** Diez líneas de setTimeout no justifican una dependencia. */
export function useDebounce(valor, milisegundos = 300) {
  const [diferido, setDiferido] = useState(valor)
  useEffect(() => {
    const temporizador = setTimeout(() => setDiferido(valor), milisegundos)
    return () => clearTimeout(temporizador)
  }, [valor, milisegundos])
  return diferido
}
