import { useState, type DragEvent } from 'react'
import { Link } from 'react-router'
import { ESTADOS, formatarData, nomePrioridade, type EstadoTarefa, type Tarefa } from '../api/tarefas'
import { iniciais } from '../api/texto'

interface Props {
  tarefas: Tarefa[]
  /** Projeto arquivado: sem arrastar nem mudar de estado. */
  soLeitura: boolean
  /** Tarefa com um pedido em curso (o cartão fica desativado). */
  ocupada: string | null
  aoMover: (tarefa: Tarefa, estado: EstadoTarefa) => void
}

const TIPO_ARRASTO = 'application/x-matriz-tarefa'

/**
 * Quadro Kanban (extra do MVP): uma coluna por estado. Arrastar um cartão para outra coluna muda o estado.
 * Para quem usa teclado ou ecrã tátil, cada cartão tem também um seletor "Mover para".
 */
export function Quadro({ tarefas, soLeitura, ocupada, aoMover }: Props) {
  const [aArrastar, setAArrastar] = useState<string | null>(null)
  const [sobre, setSobre] = useState<EstadoTarefa | null>(null)

  function largar(evento: DragEvent<HTMLElement>, estado: EstadoTarefa) {
    evento.preventDefault()
    setSobre(null)
    setAArrastar(null)
    if (soLeitura) return
    const id = evento.dataTransfer.getData(TIPO_ARRASTO)
    const tarefa = tarefas.find((t) => t.id === id)
    if (tarefa && tarefa.estado !== estado) aoMover(tarefa, estado)
  }

  return (
    <div className="quadro">
      {ESTADOS.map(({ valor, nome }) => {
        const daColuna = tarefas.filter((t) => t.estado === valor)
        const origem = tarefas.find((t) => t.id === aArrastar)?.estado
        const destino = sobre === valor && origem !== valor
        return (
          <section
            key={valor}
            className={`coluna coluna-${valor.toLowerCase()}${destino ? ' coluna-destino' : ''}`}
            aria-labelledby={`coluna-${valor}`}
            onDragOver={(e) => {
              if (soLeitura || !aArrastar) return
              e.preventDefault()
              e.dataTransfer.dropEffect = 'move'
              if (sobre !== valor) setSobre(valor)
            }}
            onDragLeave={(e) => {
              // Só limpa ao sair da coluna, não ao passar por cima de um cartão dentro dela
              if (!e.currentTarget.contains(e.relatedTarget as Node | null)) setSobre(null)
            }}
            onDrop={(e) => largar(e, valor)}
          >
            <header className="coluna-topo">
              <span className="ponto" aria-hidden="true" />
              <h3 id={`coluna-${valor}`}>{nome}</h3>
              <span className="contador-coluna" aria-label={`${daColuna.length} tarefa(s)`}>
                {daColuna.length}
              </span>
            </header>
            <ul className="cartoes">
              {daColuna.map((tarefa) => (
                <li
                  key={tarefa.id}
                  className={`cartao-tarefa${aArrastar === tarefa.id ? ' a-arrastar' : ''}${ocupada === tarefa.id ? ' ocupada' : ''}`}
                  draggable={!soLeitura && ocupada !== tarefa.id}
                  onDragStart={(e) => {
                    e.dataTransfer.setData(TIPO_ARRASTO, tarefa.id)
                    e.dataTransfer.effectAllowed = 'move'
                    setAArrastar(tarefa.id)
                  }}
                  onDragEnd={() => {
                    setAArrastar(null)
                    setSobre(null)
                  }}
                >
                  <div className="tarefa-topo">
                    <span className={`etiqueta prioridade-${tarefa.prioridade.toLowerCase()}`}>
                      {nomePrioridade(tarefa.prioridade)}
                    </span>
                    {tarefa.atrasada && <span className="etiqueta atrasada">Atrasada</span>}
                  </div>
                  <Link to={`/tarefas/${tarefa.id}`} className="cartao-titulo" draggable={false}>
                    {tarefa.titulo}
                  </Link>
                  <div className="cartao-rodape">
                    <span className="suave">
                      {tarefa.dataLimite ? `Até ${formatarData(tarefa.dataLimite)}` : 'Sem data'}
                      {tarefa.totalComentarios > 0 &&
                        ` · ${tarefa.totalComentarios} comentário${tarefa.totalComentarios === 1 ? '' : 's'}`}
                    </span>
                    <span className="avatares">
                      {tarefa.responsaveis.slice(0, 3).map((r) => (
                        <span key={r.id} className="avatar" title={r.nome}>
                          {iniciais(r.nome)}
                        </span>
                      ))}
                      {tarefa.responsaveis.length > 3 && (
                        <span className="avatar mais">+{tarefa.responsaveis.length - 3}</span>
                      )}
                    </span>
                  </div>
                  {!soLeitura && (
                    <label className="mover">
                      <span className="visualmente-escondido">Mover "{tarefa.titulo}" para</span>
                      <select
                        value={tarefa.estado}
                        disabled={ocupada === tarefa.id}
                        onChange={(e) => aoMover(tarefa, e.target.value as EstadoTarefa)}
                      >
                        {ESTADOS.map((e) => (
                          <option key={e.valor} value={e.valor}>
                            {e.valor === tarefa.estado ? e.nome : `Mover para ${e.nome}`}
                          </option>
                        ))}
                      </select>
                    </label>
                  )}
                </li>
              ))}
            </ul>
            {daColuna.length === 0 && (
              <p className="coluna-vazia">{soLeitura ? 'Sem tarefas' : 'Arrasta tarefas para aqui'}</p>
            )}
          </section>
        )
      })}
    </div>
  )
}
