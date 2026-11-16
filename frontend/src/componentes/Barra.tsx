import { useState } from 'react'
import { Link, useLocation } from 'react-router'
import { iniciais } from '../api/texto'
import { useSessao } from '../auth/useSessao'

/** Barra lateral (em ecrãs pequenos passa a barra no topo): marca, navegação e sessão. */
export function Barra() {
  const { utilizador, sair } = useSessao()
  const [aSair, setASair] = useState(false)
  const { pathname, search } = useLocation()
  const vista = pathname === '/' ? new URLSearchParams(search).get('vista') : null
  const noArquivo = vista === 'arquivados'
  const noQuadro = vista === 'quadro'
  const emProjetos = !noArquivo && !noQuadro

  async function terminar() {
    setASair(true)
    await sair()
  }

  return (
    <header className="barra">
      <Link to="/" className="marca">
        <span className="marca-logo" aria-hidden="true">
          M
        </span>
        Matriz
      </Link>
      <nav aria-label="Principal">
        <Link to="/" aria-current={emProjetos ? 'page' : undefined}>
          <span className="icone" aria-hidden="true">
            ◧
          </span>
          Projetos
        </Link>
        <Link to="/?vista=quadro" aria-current={noQuadro ? 'page' : undefined}>
          <span className="icone" aria-hidden="true">
            ▦
          </span>
          Quadro geral
        </Link>
        <Link to="/?vista=arquivados" aria-current={noArquivo ? 'page' : undefined}>
          <span className="icone" aria-hidden="true">
            ▤
          </span>
          Arquivo
        </Link>
      </nav>
      <div className="sessao">
        <span className="avatar grande" aria-hidden="true">
          {iniciais(utilizador?.nome ?? '')}
        </span>
        <span className="sessao-texto">
          <span className="sessao-nome">{utilizador?.nome}</span>
          <button type="button" className="ligacao" onClick={terminar} disabled={aSair}>
            {aSair ? 'A terminar…' : 'Terminar sessão'}
          </button>
        </span>
      </div>
    </header>
  )
}
