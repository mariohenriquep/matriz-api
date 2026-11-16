import { Link } from 'react-router'

/** Topo das páginas públicas (apresentação, registo e login): marca e ligações para entrar ou criar conta. */
export function TopoPublico({ mostrar = 'ambos' }: { mostrar?: 'ambos' | 'entrar' | 'registo' }) {
  return (
    <header className="topo-publico">
      <Link to="/" className="marca">
        <span className="marca-logo" aria-hidden="true">
          M
        </span>
        Matriz
      </Link>
      <nav aria-label="Conta">
        {mostrar !== 'registo' && (
          <Link to="/login" className="botao secundario">
            Entrar
          </Link>
        )}
        {mostrar !== 'entrar' && (
          <Link to="/registo" className="botao">
            Criar conta
          </Link>
        )}
      </nav>
    </header>
  )
}
