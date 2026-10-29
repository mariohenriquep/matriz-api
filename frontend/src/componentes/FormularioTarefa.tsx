import { useState, type FormEvent } from 'react'
import { ErroApi } from '../api/cliente'
import type { Membro } from '../api/projetos'
import {
  ESTADOS,
  LIMITE_DESCRICAO_TAREFA,
  LIMITE_TITULO,
  PRIORIDADES,
  type EstadoTarefa,
  type Prioridade,
  type Tarefa,
} from '../api/tarefas'
import { ErroCampo, MensagemErro } from './Estados'

export interface DadosTarefa {
  titulo: string
  descricao: string | null
  prioridade: Prioridade
  dataLimite: string | null
  estado: EstadoTarefa
  responsavelIds: string[]
}

interface Props {
  /** Sem tarefa inicial é uma criação: escolhem-se os responsáveis e o estado começa em "Por fazer". */
  inicial?: Tarefa
  membros: Membro[]
  aoSubmeter: (dados: DadosTarefa) => Promise<void>
  aoCancelar: () => void
}

/** Criar e editar tarefa. Valida os limites do contrato antes de enviar; os dados ficam se der erro. */
export function FormularioTarefa({ inicial, membros, aoSubmeter, aoCancelar }: Props) {
  const criacao = !inicial
  const [titulo, setTitulo] = useState(inicial?.titulo ?? '')
  const [descricao, setDescricao] = useState(inicial?.descricao ?? '')
  const [prioridade, setPrioridade] = useState<Prioridade>(inicial?.prioridade ?? 'MEDIA')
  const [estado, setEstado] = useState<EstadoTarefa>(inicial?.estado ?? 'POR_FAZER')
  const [dataLimite, setDataLimite] = useState(inicial?.dataLimite ?? '')
  const [responsavelIds, setResponsavelIds] = useState<string[]>([])
  const [aEnviar, setAEnviar] = useState(false)
  const [erros, setErros] = useState<Record<string, string>>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)
  const prefixo = inicial ? `tarefa-${inicial.id}` : 'nova-tarefa'

  function validar() {
    const novos: Record<string, string> = {}
    if (!titulo.trim()) novos.titulo = 'O título é obrigatório'
    else if (titulo.trim().length > LIMITE_TITULO) novos.titulo = `O título tem no máximo ${LIMITE_TITULO} caracteres`
    if (descricao.trim().length > LIMITE_DESCRICAO_TAREFA)
      novos.descricao = `A descrição tem no máximo ${LIMITE_DESCRICAO_TAREFA} caracteres`
    return novos
  }

  function alternarResponsavel(id: string) {
    setResponsavelIds((atuais) => (atuais.includes(id) ? atuais.filter((r) => r !== id) : [...atuais, id]))
  }

  async function submeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (aEnviar) return
    const locais = validar()
    setErros(locais)
    setErroGeral(null)
    if (Object.keys(locais).length > 0) return

    setAEnviar(true)
    try {
      await aoSubmeter({
        titulo: titulo.trim(),
        descricao: descricao.trim() || null,
        prioridade,
        dataLimite: dataLimite || null,
        estado,
        responsavelIds,
      })
    } catch (e) {
      if (e instanceof ErroApi && e.erros.length > 0) {
        setErros(Object.fromEntries(e.erros.map((erro) => [erro.campo, erro.mensagem])))
      } else {
        setErroGeral(e instanceof Error ? e.message : 'Ocorreu um erro inesperado. Tenta novamente.')
      }
      setAEnviar(false)
    }
  }

  return (
    <form onSubmit={submeter} noValidate className="formulario">
      {erroGeral && <MensagemErro texto={erroGeral} />}
      <label>
        Título
        <input
          value={titulo}
          onChange={(e) => setTitulo(e.target.value)}
          aria-invalid={!!erros.titulo}
          aria-describedby={`${prefixo}-erro-titulo`}
          autoFocus
        />
        <ErroCampo id={`${prefixo}-erro-titulo`} texto={erros.titulo} />
      </label>
      <label>
        Descrição <span className="suave">(opcional)</span>
        <textarea
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
          rows={3}
          aria-invalid={!!erros.descricao}
          aria-describedby={`${prefixo}-erro-descricao`}
        />
        <span className="suave contador">
          {descricao.trim().length}/{LIMITE_DESCRICAO_TAREFA}
        </span>
        <ErroCampo id={`${prefixo}-erro-descricao`} texto={erros.descricao} />
      </label>
      <div className="linha">
        {!criacao && (
          <label>
            Estado
            <select value={estado} onChange={(e) => setEstado(e.target.value as EstadoTarefa)}>
              {ESTADOS.map((e) => (
                <option key={e.valor} value={e.valor}>
                  {e.nome}
                </option>
              ))}
            </select>
          </label>
        )}
        <label>
          Prioridade
          <select value={prioridade} onChange={(e) => setPrioridade(e.target.value as Prioridade)}>
            {PRIORIDADES.map((p) => (
              <option key={p.valor} value={p.valor}>
                {p.nome}
              </option>
            ))}
          </select>
          <ErroCampo id={`${prefixo}-erro-prioridade`} texto={erros.prioridade} />
        </label>
        <label>
          Data limite <span className="suave">(opcional)</span>
          <input
            type="date"
            value={dataLimite}
            onChange={(e) => setDataLimite(e.target.value)}
            aria-invalid={!!erros.dataLimite}
            aria-describedby={`${prefixo}-erro-data`}
          />
          <ErroCampo id={`${prefixo}-erro-data`} texto={erros.dataLimite} />
        </label>
      </div>
      {criacao && (
        <fieldset className="responsaveis">
          <legend>
            Responsáveis <span className="suave">(opcional)</span>
          </legend>
          {membros.map((membro) => (
            <label key={membro.utilizador.id} className="opcao">
              <input
                type="checkbox"
                checked={responsavelIds.includes(membro.utilizador.id)}
                onChange={() => alternarResponsavel(membro.utilizador.id)}
              />
              {membro.utilizador.nome}
            </label>
          ))}
          <ErroCampo id={`${prefixo}-erro-responsaveis`} texto={erros.responsavelIds} />
        </fieldset>
      )}
      <div className="acoes">
        <button type="submit" disabled={aEnviar}>
          {aEnviar ? 'A guardar…' : criacao ? 'Criar tarefa' : 'Guardar'}
        </button>
        <button type="button" className="secundario" onClick={aoCancelar} disabled={aEnviar}>
          Cancelar
        </button>
      </div>
    </form>
  )
}
