import { useEffect, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { ErroApi } from '../api/cliente'
import {
  adicionarMembro,
  editarProjeto,
  eliminarProjeto,
  listarMembros,
  obterProjeto,
  removerMembro,
  type Membro,
  type Projeto,
} from '../api/projetos'
import { Barra } from '../componentes/Barra'
import { Contagens } from '../componentes/Contagens'
import { ACarregar, MensagemErro, MensagemSucesso } from '../componentes/Estados'
import { FormularioProjeto } from '../componentes/FormularioProjeto'
import { Tarefas } from '../componentes/Tarefas'

type Estado =
  | { tipo: 'a-carregar' }
  | { tipo: 'nao-encontrado' }
  | { tipo: 'erro'; mensagem: string }
  | { tipo: 'pronto'; projeto: Projeto; membros: Membro[] }

/** Detalhe do projeto: dados, edição e eliminação (só Gestor), membros e tarefas. */
export function ProjetoDetalhe() {
  const { projetoId = '' } = useParams()
  const navegar = useNavigate()
  const [estado, setEstado] = useState<Estado>({ tipo: 'a-carregar' })
  const [aEditar, setAEditar] = useState(false)
  const [aConfirmarEliminar, setAConfirmarEliminar] = useState(false)
  const [aEliminar, setAEliminar] = useState(false)
  const [erroAcao, setErroAcao] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)

  const [versao, setVersao] = useState(0)

  useEffect(() => {
    let ativo = true
    Promise.all([obterProjeto(projetoId), listarMembros(projetoId)])
      .then(([projeto, membros]) => ativo && setEstado({ tipo: 'pronto', projeto, membros: membros.itens }))
      .catch((e) => {
        if (!ativo) return
        if (e instanceof ErroApi && e.estado === 404) setEstado({ tipo: 'nao-encontrado' })
        else setEstado({ tipo: 'erro', mensagem: e instanceof Error ? e.message : 'Não foi possível carregar o projeto.' })
      })
    return () => {
      ativo = false
    }
  }, [projetoId, versao])

  function recarregar() {
    setEstado({ tipo: 'a-carregar' })
    setVersao((v) => v + 1)
  }

  if (estado.tipo !== 'pronto') {
    return (
      <>
        <Barra />
        <main className="conteudo">
          <p>
            <Link to="/">← Os meus projetos</Link>
          </p>
          {estado.tipo === 'a-carregar' && <ACarregar texto="A carregar o projeto…" />}
          {estado.tipo === 'erro' && <MensagemErro texto={estado.mensagem} tentarNovamente={recarregar} />}
          {estado.tipo === 'nao-encontrado' && (
            <div className="vazio">
              <h1>Projeto não encontrado</h1>
              <p>O projeto não existe ou não tens acesso a ele.</p>
            </div>
          )}
        </main>
      </>
    )
  }

  const { projeto, membros } = estado
  const souGestor = projeto.meuPapel === 'GESTOR'

  async function guardar(dados: { nome: string; descricao: string | null }) {
    const atualizado = await editarProjeto(projeto.id, dados)
    setEstado({ tipo: 'pronto', projeto: atualizado, membros })
    setAEditar(false)
    setSucesso('Alterações guardadas.')
  }

  /** Depois de mudar tarefas: volta a pedir o projeto só para as contagens, sem recarregar o ecrã. */
  function atualizarContagens() {
    obterProjeto(projeto.id)
      .then((atual) =>
        setEstado((anterior) => (anterior.tipo === 'pronto' ? { ...anterior, projeto: atual } : anterior)),
      )
      .catch(() => {
        // As contagens ficam como estavam; a lista de tarefas já mostra o resultado
      })
  }

  async function eliminar() {
    if (aEliminar) return
    setAEliminar(true)
    setErroAcao(null)
    try {
      await eliminarProjeto(projeto.id)
      navegar('/', { replace: true })
    } catch (e) {
      setErroAcao(e instanceof Error ? e.message : 'Não foi possível eliminar o projeto.')
      setAEliminar(false)
    }
  }

  return (
    <>
      <Barra />
      <main className="conteudo">
        <p>
          <Link to="/">← Os meus projetos</Link>
        </p>

        {sucesso && <MensagemSucesso>{sucesso}</MensagemSucesso>}
        {erroAcao && <MensagemErro texto={erroAcao} />}

        {aEditar ? (
          <section className="cartao" aria-label="Editar projeto">
            <h1>Editar projeto</h1>
            <FormularioProjeto
              inicial={projeto}
              textoBotao="Guardar"
              textoAEnviar="A guardar…"
              aoSubmeter={guardar}
              aoCancelar={() => setAEditar(false)}
            />
          </section>
        ) : (
          <section>
            <div className="cabecalho">
              <h1>{projeto.nome}</h1>
              {souGestor && (
                <span className="acoes">
                  <button type="button" className="secundario" onClick={() => { setAEditar(true); setSucesso(null) }}>
                    Editar
                  </button>
                  <button
                    type="button"
                    className="perigo"
                    onClick={() => { setAConfirmarEliminar(true); setSucesso(null) }}
                  >
                    Eliminar
                  </button>
                </span>
              )}
            </div>
            <p className="suave">
              Gestor: {projeto.gestor.nome} · O teu papel: {souGestor ? 'Gestor' : 'Membro'}
            </p>
            {projeto.descricao ? <p className="descricao">{projeto.descricao}</p> : <p className="suave">Sem descrição.</p>}
            <Contagens contagens={projeto.contagens} />
          </section>
        )}

        {aConfirmarEliminar && (
          <div className="confirmacao" role="alertdialog" aria-labelledby="titulo-eliminar">
            <h2 id="titulo-eliminar">Eliminar "{projeto.nome}"?</h2>
            <p>
              Isto apaga o projeto e também as suas {projeto.contagens.total} tarefa(s), as atribuições e os comentários.
              Não pode ser desfeito.
            </p>
            <div className="acoes">
              <button type="button" className="perigo" onClick={eliminar} disabled={aEliminar}>
                {aEliminar ? 'A eliminar…' : 'Eliminar definitivamente'}
              </button>
              <button type="button" className="secundario" onClick={() => setAConfirmarEliminar(false)} disabled={aEliminar}>
                Cancelar
              </button>
            </div>
          </div>
        )}

        <Membros
          projeto={projeto}
          membros={membros}
          aoMudar={(novos) => setEstado({ tipo: 'pronto', projeto, membros: novos })}
        />

        <Tarefas projeto={projeto} membros={membros} aoMudar={atualizarContagens} />
      </main>
    </>
  )
}

function Membros({
  projeto,
  membros,
  aoMudar,
}: {
  projeto: Projeto
  membros: Membro[]
  aoMudar: (membros: Membro[]) => void
}) {
  const souGestor = projeto.meuPapel === 'GESTOR'
  const [email, setEmail] = useState('')
  const [aEnviar, setAEnviar] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [aRemover, setARemover] = useState<Membro | null>(null)

  async function adicionar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (aEnviar || !email.trim()) return
    setAEnviar(true)
    setErro(null)
    try {
      const novo = await adicionarMembro(projeto.id, email.trim())
      aoMudar([...membros, novo])
      setEmail('')
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Não foi possível adicionar o membro.')
    } finally {
      setAEnviar(false)
    }
  }

  async function remover(membro: Membro) {
    setAEnviar(true)
    setErro(null)
    try {
      await removerMembro(projeto.id, membro.utilizador.id)
      aoMudar(membros.filter((m) => m.utilizador.id !== membro.utilizador.id))
      setARemover(null)
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Não foi possível remover o membro.')
    } finally {
      setAEnviar(false)
    }
  }

  return (
    <section className="cartao">
      <h2>Membros ({membros.length})</h2>
      {erro && <MensagemErro texto={erro} />}
      <ul className="membros">
        {membros.map((membro) => (
          <li key={membro.utilizador.id}>
            <span>
              {membro.utilizador.nome} <span className="suave">{membro.utilizador.email}</span>
              {membro.papel === 'GESTOR' && <span className="etiqueta">Gestor</span>}
            </span>
            {souGestor && membro.papel !== 'GESTOR' && (
              aRemover?.utilizador.id === membro.utilizador.id ? (
                <span className="acoes">
                  <span>Remover? As atribuições dele neste projeto saem.</span>
                  <button type="button" className="perigo pequeno" onClick={() => void remover(membro)} disabled={aEnviar}>
                    Remover
                  </button>
                  <button type="button" className="secundario pequeno" onClick={() => setARemover(null)} disabled={aEnviar}>
                    Cancelar
                  </button>
                </span>
              ) : (
                <button type="button" className="secundario pequeno" onClick={() => setARemover(membro)}>
                  Remover
                </button>
              )
            )}
          </li>
        ))}
      </ul>
      {souGestor && (
        <form onSubmit={adicionar} className="linha" noValidate>
          <label>
            Adicionar por email
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="nome@exemplo.pt" />
          </label>
          <button type="submit" disabled={aEnviar || !email.trim()}>
            {aEnviar ? 'A adicionar…' : 'Adicionar'}
          </button>
        </form>
      )}
    </section>
  )
}
