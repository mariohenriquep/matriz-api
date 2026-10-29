import { useState } from 'react'
import { Link } from 'react-router'
import { useSessao } from '../auth/useSessao'

export function Barra() {
  const { utilizador, sair } = useSessao()
  const [aSair, setASair] = useState(false)

  async function terminar() {
    setASair(true)
    await sair()
  }

  return (
    <header className="barra">
      <Link to="/" className="marca">
        Matriz
      </Link>
      <span>
        {utilizador?.nome}
        <button type="button" className="secundario" onClick={terminar} disabled={aSair}>
          {aSair ? 'A terminar…' : 'Terminar sessão'}
        </button>
      </span>
    </header>
  )
}
