import { useEffect, useMemo, useState } from 'react'
import type { Projeto } from '../api/projetos'
import {
  PRIORIDADES,
  listarTodasAsTarefas,
  type FiltrosQuadroGeral,
  type Prioridade,
} from '../api/tarefas'
import { useSessao } from '../auth/useSessao'
import { ACarregar, MensagemErro, MensagemSucesso } from './Estados'
import { Quadro } from './Quadro'
import { useMoverTarefa, type EstadoListaTarefas } from './useMoverTarefa'

interface Props {
  /** Projetos ativos do utilizador (para o filtro e o nome em cada cartão). */
  projetos: Projeto[]
  /** Chamado depois de uma tarefa mudar de estado, para a página atualizar o resumo. */
  aoMudar: () => void
}

/**
 * Quadro geral da página inicial: as tarefas de todos os projetos ativos num só Kanban, sem abrir cada projeto.
 * Arrastar muda o estado como no quadro de um projeto. Os projetos arquivados ficam de fora (são só de leitura).
 */
export function QuadroGeral({ projetos, aoMudar }: Props) {
  const { utilizador } = useSessao()
  const [projetoId, setProjetoId] = useState('')
  const [prioridade, setPrioridade] = useState<Prioridade | ''>('')
  const [soMinhas, setSoMinhas] = useState(false)
  const [estado, setEstado] = useState<EstadoListaTarefas>({ tipo: 'a-carregar' })
  const [versao, setVersao] = useState(0)
  const { ocupada, erroAcao, setErroAcao, sucesso, setSucesso, mover } = useMoverTarefa(setEstado, aoMudar)

  const nomes = useMemo(() => new Map(projetos.map((p) => [p.id, p.nome])), [projetos])

  useEffect(() => {
    let ativo = true
    const filtros: FiltrosQuadroGeral = {}
    if (projetoId) filtros.projetoId = projetoId
    if (prioridade) filtros.prioridade = prioridade
    if (soMinhas && utilizador) filtros.responsavelId = utilizador.id
    listarTodasAsTarefas(filtros)
      .then((lista) => ativo && setEstado({ tipo: 'pronto', tarefas: lista.itens }))
      .catch(
        (e) =>
          ativo &&
          setEstado({ tipo: 'erro', mensagem: e instanceof Error ? e.message : 'Não foi possível carregar as tarefas.' }),
      )
    return () => {
      ativo = false
    }
  }, [projetoId, prioridade, soMinhas, utilizador, versao])

  function aCarregar() {
    setEstado({ tipo: 'a-carregar' })
    setSucesso(null)
    setErroAcao(null)
  }

  function recarregar() {
    aCarregar()
    setVersao((v) => v + 1)
  }

  const filtrado = Boolean(projetoId || prioridade || soMinhas)

  return (
    <section className="painel-tarefas" aria-labelledby="titulo-quadro-geral">
      <h2 id="titulo-quadro-geral" className="visualmente-escondido">
        Todas as tarefas
      </h2>
      <div className="filtros" role="group" aria-label="Filtrar o quadro">
        <label>
          Projeto
          <select
            value={projetoId}
            onChange={(e) => {
              aCarregar()
              setProjetoId(e.target.value)
            }}
          >
            <option value="">Todos os projetos</option>
            {projetos.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nome}
              </option>
            ))}
          </select>
        </label>
        <label>
          Prioridade
          <select
            value={prioridade}
            onChange={(e) => {
              aCarregar()
              setPrioridade(e.target.value as Prioridade | '')
            }}
          >
            <option value="">Todas</option>
            {PRIORIDADES.map((p) => (
              <option key={p.valor} value={p.valor}>
                {p.nome}
              </option>
            ))}
          </select>
        </label>
        <label className="caixa">
          <input
            type="checkbox"
            checked={soMinhas}
            onChange={(e) => {
              aCarregar()
              setSoMinhas(e.target.checked)
            }}
          />
          Só as minhas
        </label>
      </div>

      {sucesso && <MensagemSucesso>{sucesso}</MensagemSucesso>}
      {erroAcao && <MensagemErro texto={erroAcao} />}

      {estado.tipo === 'a-carregar' && <ACarregar texto="A carregar as tarefas…" />}
      {estado.tipo === 'erro' && <MensagemErro texto={estado.mensagem} tentarNovamente={recarregar} />}
      {estado.tipo === 'pronto' && estado.tarefas.length === 0 && (
        <div className="vazio">
          <p>
            {filtrado
              ? 'Nenhuma tarefa corresponde aos filtros.'
              : 'Ainda não há tarefas nos teus projetos ativos. Abre um projeto para criar a primeira.'}
          </p>
        </div>
      )}
      {estado.tipo === 'pronto' && estado.tarefas.length > 0 && (
        <Quadro
          tarefas={estado.tarefas}
          soLeitura={false}
          ocupada={ocupada}
          aoMover={(t, e) => void mover(t, e)}
          nomesProjetos={nomes}
        />
      )}
    </section>
  )
}
