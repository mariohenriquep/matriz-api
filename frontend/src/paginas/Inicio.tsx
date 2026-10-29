import { useState } from 'react'
import { useSessao } from '../auth/useSessao'

// Painel provisório: a lista de projetos e as contagens chegam no Exercício 4.
export function Inicio() {
  const { utilizador, sair } = useSessao()
  const [aSair, setASair] = useState(false)

  async function terminar() {
    setASair(true)
    await sair()
  }

  return (
    <>
      <header className="barra">
        <strong>Matriz</strong>
        <span>
          {utilizador?.nome}
          <button type="button" className="secundario" onClick={terminar} disabled={aSair}>
            {aSair ? 'A terminar…' : 'Terminar sessão'}
          </button>
        </span>
      </header>
      <main className="conteudo">
        <h1>Olá, {utilizador?.nome}</h1>
        <p className="suave">O painel com os teus projetos está em construção.</p>
      </main>
    </>
  )
}
