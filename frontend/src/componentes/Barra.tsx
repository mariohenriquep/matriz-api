import { useState } from 'react'
import { Link, NavLink } from 'react-router'
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
      <nav aria-label="Principal">
        <Link to="/" className="marca">
          Matriz
        </Link>
        <NavLink to="/" end>
          Projetos
        </NavLink>
      </nav>
      <span>
        {utilizador?.nome}
        <button type="button" className="secundario" onClick={terminar} disabled={aSair}>
          {aSair ? 'A terminar…' : 'Terminar sessão'}
        </button>
      </span>
    </header>
  )
}
