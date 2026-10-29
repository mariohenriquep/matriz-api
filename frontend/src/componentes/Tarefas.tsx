import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import type { Membro, Projeto } from '../api/projetos'
import {
  ESTADOS,
  PRIORIDADES,
  criarTarefa,
  editarTarefa,
  eliminarTarefa,
  formatarData,
  listarTarefas,
  nomePrioridade,
  type AlteracaoTarefa,
  type EstadoTarefa,
  type FiltrosTarefas,
  type Prioridade,
  type Tarefa,
} from '../api/tarefas'
import { useSessao } from '../auth/useSessao'
import { ACarregar, MensagemErro, MensagemSucesso } from './Estados'
import { FormularioTarefa, type DadosTarefa } from './FormularioTarefa'

type Estado =
  | { tipo: 'a-carregar' }
  | { tipo: 'erro'; mensagem: string }
  | { tipo: 'pronto'; tarefas: Tarefa[] }

interface Props {
  projeto: Projeto
  membros: Membro[]
  /** Chamado depois de criar, editar ou eliminar, para o projeto atualizar as contagens. */
  aoMudar: () => void
}

/** Lista de tarefas do projeto com filtros, criação, edição, mudança rápida de estado e eliminação. */
export function Tarefas({ projeto, membros, aoMudar }: Props) {
  const { utilizador } = useSessao()
  const [filtros, setFiltros] = useState<FiltrosTarefas>({})
  const [estado, setEstado] = useState<Estado>({ tipo: 'a-carregar' })
  const [versao, setVersao] = useState(0)
  const [aCriar, setACriar] = useState(false)
  const [aEditar, setAEditar] = useState<string | null>(null)
  const [aConfirmar, setAConfirmar] = useState<string | null>(null)
  const [ocupada, setOcupada] = useState<string | null>(null)
  const [erroAcao, setErroAcao] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)

  useEffect(() => {
    let ativo = true
    listarTarefas(projeto.id, filtros)
      .then((lista) => ativo && setEstado({ tipo: 'pronto', tarefas: lista.itens }))
      .catch((e) => ativo && setEstado({ tipo: 'erro', mensagem: e instanceof Error ? e.message : 'Não foi possível carregar as tarefas.' }))
    return () => {
      ativo = false
    }
  }, [projeto.id, filtros, versao])

  const filtrosAtivos = Boolean(filtros.estado || filtros.prioridade || filtros.responsavelId)
  const souGestor = projeto.meuPapel === 'GESTOR'

  function mudarFiltro(novos: FiltrosTarefas) {
    setEstado({ tipo: 'a-carregar' })
    setFiltros(novos)
  }

  /**
   * Volta a pedir a lista com os filtros atuais (a tarefa pode deixar de corresponder) e as contagens do projeto.
   * A mensagem só aparece depois de a lista estar atualizada.
   */
  async function depoisDeMudar(mensagem: string) {
    aoMudar()
    try {
      const lista = await listarTarefas(projeto.id, filtros)
      setEstado({ tipo: 'pronto', tarefas: lista.itens })
    } catch {
      setVersao((v) => v + 1)
    }
    setSucesso(mensagem)
  }

  async function criar(dados: DadosTarefa) {
    await criarTarefa(projeto.id, {
      titulo: dados.titulo,
      descricao: dados.descricao,
      prioridade: dados.prioridade,
      dataLimite: dados.dataLimite,
      responsavelIds: dados.responsavelIds,
    })
    await depoisDeMudar(`Tarefa "${dados.titulo}" criada.`)
    setACriar(false)
  }

  async function guardar(tarefa: Tarefa, dados: DadosTarefa) {
    const alteracao: AlteracaoTarefa = {}
    if (dados.titulo !== tarefa.titulo) alteracao.titulo = dados.titulo
    if (dados.descricao !== tarefa.descricao) alteracao.descricao = dados.descricao
    if (dados.estado !== tarefa.estado) alteracao.estado = dados.estado
    if (dados.prioridade !== tarefa.prioridade) alteracao.prioridade = dados.prioridade
    if (dados.dataLimite !== tarefa.dataLimite) alteracao.dataLimite = dados.dataLimite
    if (Object.keys(alteracao).length > 0) await editarTarefa(tarefa.id, alteracao)
    await depoisDeMudar('Alterações guardadas.')
    setAEditar(null)
  }

  async function mudarEstado(tarefa: Tarefa, novo: EstadoTarefa) {
    if (ocupada) return
    setOcupada(tarefa.id)
    setErroAcao(null)
    try {
      await editarTarefa(tarefa.id, { estado: novo })
      await depoisDeMudar(`"${tarefa.titulo}" passou para ${ESTADOS.find((e) => e.valor === novo)?.nome}.`)
    } catch (e) {
      setErroAcao(e instanceof Error ? e.message : 'Não foi possível mudar o estado.')
    } finally {
      setOcupada(null)
    }
  }

  async function eliminar(tarefa: Tarefa) {
    if (ocupada) return
    setOcupada(tarefa.id)
    setErroAcao(null)
    try {
      await eliminarTarefa(tarefa.id)
      setAConfirmar(null)
      await depoisDeMudar(`Tarefa "${tarefa.titulo}" eliminada.`)
    } catch (e) {
      setErroAcao(e instanceof Error ? e.message : 'Não foi possível eliminar a tarefa.')
    } finally {
      setOcupada(null)
    }
  }

  return (
    <section className="cartao" aria-labelledby="titulo-tarefas">
      <div className="cabecalho">
        <h2 id="titulo-tarefas">Tarefas</h2>
        {!aCriar && (
          <button type="button" onClick={() => { setACriar(true); setSucesso(null) }}>
            Nova tarefa
          </button>
        )}
      </div>

      {aCriar && (
        <div className="cartao">
          <h3>Nova tarefa</h3>
          <FormularioTarefa membros={membros} aoSubmeter={criar} aoCancelar={() => setACriar(false)} />
        </div>
      )}

      <div className="filtros" role="group" aria-label="Filtrar tarefas">
        <label>
          Estado
          <select
            value={filtros.estado ?? ''}
            onChange={(e) => mudarFiltro({ ...filtros, estado: (e.target.value || undefined) as EstadoTarefa | undefined })}
          >
            <option value="">Todos</option>
            {ESTADOS.map((e) => (
              <option key={e.valor} value={e.valor}>
                {e.nome}
              </option>
            ))}
          </select>
        </label>
        <label>
          Prioridade
          <select
            value={filtros.prioridade ?? ''}
            onChange={(e) =>
              mudarFiltro({ ...filtros, prioridade: (e.target.value || undefined) as Prioridade | undefined })
            }
          >
            <option value="">Todas</option>
            {PRIORIDADES.map((p) => (
              <option key={p.valor} value={p.valor}>
                {p.nome}
              </option>
            ))}
          </select>
        </label>
        <label>
          Responsável
          <select
            value={filtros.responsavelId ?? ''}
            onChange={(e) => mudarFiltro({ ...filtros, responsavelId: e.target.value || undefined })}
          >
            <option value="">Todos</option>
            {membros.map((m) => (
              <option key={m.utilizador.id} value={m.utilizador.id}>
                {m.utilizador.nome}
              </option>
            ))}
          </select>
        </label>
        {filtrosAtivos && (
          <button type="button" className="secundario pequeno" onClick={() => mudarFiltro({})}>
            Limpar filtros
          </button>
        )}
      </div>

      {sucesso && <MensagemSucesso>{sucesso}</MensagemSucesso>}
      {erroAcao && <MensagemErro texto={erroAcao} />}

      {estado.tipo === 'a-carregar' && <ACarregar texto="A carregar as tarefas…" />}
      {estado.tipo === 'erro' && (
        <MensagemErro texto={estado.mensagem} tentarNovamente={() => { setEstado({ tipo: 'a-carregar' }); setVersao((v) => v + 1) }} />
      )}
      {estado.tipo === 'pronto' && estado.tarefas.length === 0 && (
        <p className="vazio">
          {filtrosAtivos ? 'Nenhuma tarefa corresponde aos filtros.' : 'Ainda não há tarefas neste projeto.'}
        </p>
      )}
      {estado.tipo === 'pronto' && estado.tarefas.length > 0 && (
        <ul className="tarefas">
          {estado.tarefas.map((tarefa) => {
            const podeEliminar = souGestor || tarefa.criador.id === utilizador?.id
            if (aEditar === tarefa.id) {
              return (
                <li key={tarefa.id} className="cartao">
                  <h3>Editar tarefa</h3>
                  <FormularioTarefa
                    inicial={tarefa}
                    membros={membros}
                    aoSubmeter={(dados) => guardar(tarefa, dados)}
                    aoCancelar={() => setAEditar(null)}
                  />
                </li>
              )
            }
            return (
              <li key={tarefa.id}>
                <div className="tarefa-topo">
                  <strong>
                    <Link to={`/tarefas/${tarefa.id}`}>{tarefa.titulo}</Link>
                  </strong>
                  <span className={`etiqueta prioridade-${tarefa.prioridade.toLowerCase()}`}>
                    {nomePrioridade(tarefa.prioridade)}
                  </span>
                  {tarefa.atrasada && <span className="etiqueta atrasada">Atrasada</span>}
                </div>
                {tarefa.descricao && <p className="descricao">{tarefa.descricao}</p>}
                <p className="suave">
                  {tarefa.responsaveis.length > 0
                    ? `Responsáveis: ${tarefa.responsaveis.map((r) => r.nome).join(', ')}`
                    : 'Sem responsáveis'}
                  {tarefa.dataLimite && ` · Data limite: ${formatarData(tarefa.dataLimite)}`}
                  {` · Criada por ${tarefa.criador.nome}`}
                  {tarefa.totalComentarios > 0 && ` · ${tarefa.totalComentarios} comentário(s)`}
                </p>
                <div className="acoes">
                  <label className="em-linha">
                    Estado
                    <select
                      value={tarefa.estado}
                      disabled={ocupada === tarefa.id}
                      onChange={(e) => void mudarEstado(tarefa, e.target.value as EstadoTarefa)}
                    >
                      {ESTADOS.map((e) => (
                        <option key={e.valor} value={e.valor}>
                          {e.nome}
                        </option>
                      ))}
                    </select>
                  </label>
                  <button
                    type="button"
                    className="secundario pequeno"
                    onClick={() => { setAEditar(tarefa.id); setSucesso(null) }}
                  >
                    Editar
                  </button>
                  {podeEliminar &&
                    (aConfirmar === tarefa.id ? (
                      <>
                        <span>Eliminar, com os comentários?</span>
                        <button
                          type="button"
                          className="perigo pequeno"
                          onClick={() => void eliminar(tarefa)}
                          disabled={ocupada === tarefa.id}
                        >
                          {ocupada === tarefa.id ? 'A eliminar…' : 'Eliminar'}
                        </button>
                        <button type="button" className="secundario pequeno" onClick={() => setAConfirmar(null)}>
                          Cancelar
                        </button>
                      </>
                    ) : (
                      <button
                        type="button"
                        className="perigo pequeno"
                        onClick={() => { setAConfirmar(tarefa.id); setSucesso(null) }}
                      >
                        Eliminar
                      </button>
                    ))}
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
