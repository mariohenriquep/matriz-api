import { useEffect, useState, type FormEvent } from 'react'
import { Link, useParams } from 'react-router'
import { ErroApi } from '../api/cliente'
import { listarMembros, obterProjeto, type Membro, type Projeto } from '../api/projetos'
import {
  LIMITE_COMENTARIO,
  atribuirResponsavel,
  comentar,
  formatarData,
  formatarDataHora,
  listarComentarios,
  nomeEstado,
  nomePrioridade,
  obterTarefa,
  removerResponsavel,
  type Comentario,
  type Tarefa,
} from '../api/tarefas'
import { Barra } from '../componentes/Barra'
import { ACarregar, ErroCampo, MensagemErro, MensagemSucesso } from '../componentes/Estados'

type Estado =
  | { tipo: 'a-carregar' }
  | { tipo: 'nao-encontrada' }
  | { tipo: 'erro'; mensagem: string }
  | { tipo: 'pronto'; tarefa: Tarefa; projeto: Projeto; membros: Membro[]; comentarios: Comentario[] }

/** Detalhe da tarefa: dados, responsáveis (atribuir e remover) e comentários. */
export function TarefaDetalhe() {
  const { tarefaId = '' } = useParams()
  const [estado, setEstado] = useState<Estado>({ tipo: 'a-carregar' })
  const [versao, setVersao] = useState(0)

  useEffect(() => {
    let ativo = true
    obterTarefa(tarefaId)
      .then((tarefa) =>
        Promise.all([obterProjeto(tarefa.projetoId), listarMembros(tarefa.projetoId), listarComentarios(tarefa.id)]).then(
          ([projeto, membros, comentarios]) =>
            ativo &&
            setEstado({ tipo: 'pronto', tarefa, projeto, membros: membros.itens, comentarios: comentarios.itens }),
        ),
      )
      .catch((e) => {
        if (!ativo) return
        if (e instanceof ErroApi && e.estado === 404) setEstado({ tipo: 'nao-encontrada' })
        else setEstado({ tipo: 'erro', mensagem: e instanceof Error ? e.message : 'Não foi possível carregar a tarefa.' })
      })
    return () => {
      ativo = false
    }
  }, [tarefaId, versao])

  if (estado.tipo !== 'pronto') {
    return (
      <>
        <Barra />
        <main className="conteudo">
          <p>
            <Link to="/">← Os meus projetos</Link>
          </p>
          {estado.tipo === 'a-carregar' && <ACarregar texto="A carregar a tarefa…" />}
          {estado.tipo === 'erro' && (
            <MensagemErro
              texto={estado.mensagem}
              tentarNovamente={() => {
                setEstado({ tipo: 'a-carregar' })
                setVersao((v) => v + 1)
              }}
            />
          )}
          {estado.tipo === 'nao-encontrada' && (
            <div className="vazio">
              <h1>Tarefa não encontrada</h1>
              <p>A tarefa não existe ou não tens acesso a ela.</p>
            </div>
          )}
        </main>
      </>
    )
  }

  const { tarefa, projeto, membros, comentarios } = estado

  return (
    <>
      <Barra />
      <main className="conteudo">
        <p>
          <Link to={`/projetos/${projeto.id}`}>← {projeto.nome}</Link>
        </p>
        <section>
          <div className="tarefa-topo">
            <h1>{tarefa.titulo}</h1>
          </div>
          <p>
            <span className="etiqueta">{nomeEstado(tarefa.estado)}</span>
            <span className={`etiqueta prioridade-${tarefa.prioridade.toLowerCase()}`}>
              Prioridade {nomePrioridade(tarefa.prioridade).toLowerCase()}
            </span>
            {tarefa.atrasada && <span className="etiqueta atrasada">Atrasada</span>}
          </p>
          {tarefa.descricao ? <p className="descricao">{tarefa.descricao}</p> : <p className="suave">Sem descrição.</p>}
          <p className="suave">
            {tarefa.dataLimite ? `Data limite: ${formatarData(tarefa.dataLimite)} · ` : 'Sem data limite · '}
            Criada por {tarefa.criador.nome} em {formatarDataHora(tarefa.criadoEm)}
          </p>
        </section>

        <Responsaveis
          tarefa={tarefa}
          membros={membros}
          aoMudar={(atual) => setEstado({ ...estado, tarefa: atual })}
        />

        <Comentarios
          tarefa={tarefa}
          comentarios={comentarios}
          aoComentar={(novo) =>
            setEstado({
              ...estado,
              comentarios: [...comentarios, novo],
              tarefa: { ...tarefa, totalComentarios: tarefa.totalComentarios + 1 },
            })
          }
        />
      </main>
    </>
  )
}

function Responsaveis({
  tarefa,
  membros,
  aoMudar,
}: {
  tarefa: Tarefa
  membros: Membro[]
  aoMudar: (tarefa: Tarefa) => void
}) {
  const [escolhido, setEscolhido] = useState('')
  const [aEnviar, setAEnviar] = useState(false)
  const [aRemover, setARemover] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  // Só os membros do projeto que ainda não são responsáveis podem ser escolhidos
  const elegiveis = membros.filter((m) => !tarefa.responsaveis.some((r) => r.id === m.utilizador.id))

  async function atribuir(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (aEnviar || !escolhido) return
    setAEnviar(true)
    setErro(null)
    try {
      aoMudar(await atribuirResponsavel(tarefa.id, escolhido))
      setEscolhido('')
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Não foi possível atribuir o responsável.')
    } finally {
      setAEnviar(false)
    }
  }

  async function remover(utilizadorId: string) {
    if (aEnviar) return
    setAEnviar(true)
    setErro(null)
    try {
      await removerResponsavel(tarefa.id, utilizadorId)
      aoMudar({ ...tarefa, responsaveis: tarefa.responsaveis.filter((r) => r.id !== utilizadorId) })
      setARemover(null)
    } catch (e) {
      setErro(e instanceof Error ? e.message : 'Não foi possível remover a atribuição.')
    } finally {
      setAEnviar(false)
    }
  }

  return (
    <section className="cartao" aria-labelledby="titulo-responsaveis">
      <h2 id="titulo-responsaveis">Responsáveis ({tarefa.responsaveis.length})</h2>
      {erro && <MensagemErro texto={erro} />}
      {tarefa.responsaveis.length === 0 ? (
        <p className="suave">Ainda ninguém é responsável por esta tarefa.</p>
      ) : (
        <ul className="membros">
          {tarefa.responsaveis.map((r) => (
            <li key={r.id}>
              <span>{r.nome}</span>
              {aRemover === r.id ? (
                <span className="acoes">
                  <span>Remover a atribuição?</span>
                  <button type="button" className="perigo pequeno" onClick={() => void remover(r.id)} disabled={aEnviar}>
                    Remover
                  </button>
                  <button type="button" className="secundario pequeno" onClick={() => setARemover(null)} disabled={aEnviar}>
                    Cancelar
                  </button>
                </span>
              ) : (
                <button type="button" className="secundario pequeno" onClick={() => setARemover(r.id)}>
                  Remover
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
      {elegiveis.length > 0 ? (
        <form onSubmit={atribuir} className="linha" noValidate>
          <label>
            Atribuir a
            <select value={escolhido} onChange={(e) => setEscolhido(e.target.value)}>
              <option value="">Escolhe um membro do projeto</option>
              {elegiveis.map((m) => (
                <option key={m.utilizador.id} value={m.utilizador.id}>
                  {m.utilizador.nome}
                </option>
              ))}
            </select>
          </label>
          <button type="submit" disabled={aEnviar || !escolhido}>
            {aEnviar ? 'A atribuir…' : 'Atribuir'}
          </button>
        </form>
      ) : (
        <p className="suave">Todos os membros do projeto já são responsáveis.</p>
      )}
    </section>
  )
}

function Comentarios({
  tarefa,
  comentarios,
  aoComentar,
}: {
  tarefa: Tarefa
  comentarios: Comentario[]
  aoComentar: (comentario: Comentario) => void
}) {
  const [conteudo, setConteudo] = useState('')
  const [aEnviar, setAEnviar] = useState(false)
  const [erroCampo, setErroCampo] = useState<string | undefined>()
  const [erro, setErro] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState(false)

  async function enviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (aEnviar) return
    setErro(null)
    setSucesso(false)
    if (!conteudo.trim()) return setErroCampo('O comentário não pode estar vazio')
    if (conteudo.trim().length > LIMITE_COMENTARIO)
      return setErroCampo(`O comentário tem no máximo ${LIMITE_COMENTARIO} caracteres`)
    setErroCampo(undefined)
    setAEnviar(true)
    try {
      aoComentar(await comentar(tarefa.id, conteudo.trim()))
      setConteudo('')
      setSucesso(true)
    } catch (e) {
      if (e instanceof ErroApi && e.erros.length > 0) setErroCampo(e.erros[0].mensagem)
      else setErro(e instanceof Error ? e.message : 'Não foi possível publicar o comentário.')
    } finally {
      setAEnviar(false)
    }
  }

  return (
    <section className="cartao" aria-labelledby="titulo-comentarios">
      <h2 id="titulo-comentarios">Comentários ({comentarios.length})</h2>
      {comentarios.length === 0 ? (
        <p className="suave">Ainda não há comentários.</p>
      ) : (
        <ol className="comentarios">
          {comentarios.map((c) => (
            <li key={c.id}>
              <p className="suave">
                <strong>{c.autor.nome}</strong> · <time dateTime={c.criadoEm}>{formatarDataHora(c.criadoEm)}</time>
              </p>
              <p className="descricao">{c.conteudo}</p>
            </li>
          ))}
        </ol>
      )}
      {sucesso && <MensagemSucesso>Comentário publicado.</MensagemSucesso>}
      {erro && <MensagemErro texto={erro} />}
      <form onSubmit={enviar} noValidate className="formulario">
        <label>
          Novo comentário
          <textarea
            value={conteudo}
            onChange={(e) => setConteudo(e.target.value)}
            rows={3}
            aria-invalid={!!erroCampo}
            aria-describedby="erro-comentario"
          />
          <span className="suave contador">
            {conteudo.trim().length}/{LIMITE_COMENTARIO}
          </span>
          <ErroCampo id="erro-comentario" texto={erroCampo} />
        </label>
        <div className="acoes">
          <button type="submit" disabled={aEnviar}>
            {aEnviar ? 'A publicar…' : 'Comentar'}
          </button>
        </div>
      </form>
    </section>
  )
}
