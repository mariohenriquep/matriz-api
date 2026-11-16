import { useState, type Dispatch, type SetStateAction } from 'react'
import { editarTarefa, nomeEstado, type EstadoTarefa, type Tarefa } from '../api/tarefas'

/** Estado de uma lista de tarefas a carregar da API (quadro de um projeto ou quadro geral). */
export type EstadoListaTarefas =
  | { tipo: 'a-carregar' }
  | { tipo: 'erro'; mensagem: string }
  | { tipo: 'pronto'; tarefas: Tarefa[] }

function comTarefa(estado: EstadoListaTarefas, tarefa: Tarefa): EstadoListaTarefas {
  return estado.tipo === 'pronto'
    ? { ...estado, tarefas: estado.tarefas.map((t) => (t.id === tarefa.id ? tarefa : t)) }
    : estado
}

/**
 * Mensagens e tarefa ocupada de uma lista de tarefas, mais a mudança de estado no quadro: o cartão muda logo de
 * coluna e volta para trás se a API recusar. Depois de guardar chama {@code aoMudar} (contagens ou resumo).
 */
export function useMoverTarefa(setEstado: Dispatch<SetStateAction<EstadoListaTarefas>>, aoMudar: () => void) {
  const [ocupada, setOcupada] = useState<string | null>(null)
  const [erroAcao, setErroAcao] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)

  async function mover(tarefa: Tarefa, novo: EstadoTarefa) {
    if (ocupada || tarefa.estado === novo) return
    setOcupada(tarefa.id)
    setErroAcao(null)
    setSucesso(null)
    setEstado((anterior) => comTarefa(anterior, { ...tarefa, estado: novo }))
    try {
      const atualizada = await editarTarefa(tarefa.id, { estado: novo })
      setEstado((anterior) => comTarefa(anterior, atualizada))
      aoMudar()
      setSucesso(`"${tarefa.titulo}" passou para ${nomeEstado(novo)}.`)
    } catch (e) {
      setEstado((anterior) => comTarefa(anterior, tarefa))
      setErroAcao(e instanceof Error ? e.message : 'Não foi possível mudar o estado.')
    } finally {
      setOcupada(null)
    }
  }

  return { ocupada, setOcupada, erroAcao, setErroAcao, sucesso, setSucesso, mover }
}
