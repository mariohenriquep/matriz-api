import { useState, type FormEvent } from 'react'
import { ErroApi } from '../api/cliente'
import { LIMITE_DESCRICAO, LIMITE_NOME } from '../api/projetos'
import { ErroCampo, MensagemErro } from './Estados'

interface Props {
  inicial?: { nome: string; descricao: string | null }
  textoBotao: string
  textoAEnviar: string
  aoSubmeter: (dados: { nome: string; descricao: string | null }) => Promise<void>
  aoCancelar?: () => void
}

/** Criar e editar projeto. Valida os mesmos limites do contrato antes de enviar; os dados ficam se der erro. */
export function FormularioProjeto({ inicial, textoBotao, textoAEnviar, aoSubmeter, aoCancelar }: Props) {
  const [nome, setNome] = useState(inicial?.nome ?? '')
  const [descricao, setDescricao] = useState(inicial?.descricao ?? '')
  const [aEnviar, setAEnviar] = useState(false)
  const [erros, setErros] = useState<Record<string, string>>({})
  const [erroGeral, setErroGeral] = useState<string | null>(null)

  function validar() {
    const novos: Record<string, string> = {}
    if (!nome.trim()) novos.nome = 'O nome é obrigatório'
    else if (nome.trim().length > LIMITE_NOME) novos.nome = `O nome tem no máximo ${LIMITE_NOME} caracteres`
    if (descricao.trim().length > LIMITE_DESCRICAO)
      novos.descricao = `A descrição tem no máximo ${LIMITE_DESCRICAO} caracteres`
    return novos
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
      await aoSubmeter({ nome: nome.trim(), descricao: descricao.trim() || null })
    } catch (e) {
      if (e instanceof ErroApi && e.erros.length > 0) {
        setErros(Object.fromEntries(e.erros.map((erro) => [erro.campo, erro.mensagem])))
      } else {
        setErroGeral(e instanceof Error ? e.message : 'Ocorreu um erro inesperado. Tenta novamente.')
      }
    } finally {
      setAEnviar(false)
    }
  }

  return (
    <form onSubmit={submeter} noValidate className="formulario">
      {erroGeral && <MensagemErro texto={erroGeral} />}
      <label>
        Nome
        <input
          value={nome}
          onChange={(e) => setNome(e.target.value)}
          aria-invalid={!!erros.nome}
          aria-describedby="erro-nome"
          autoFocus
        />
        <ErroCampo id="erro-nome" texto={erros.nome} />
      </label>
      <label>
        Descrição <span className="suave">(opcional)</span>
        <textarea
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
          rows={3}
          aria-invalid={!!erros.descricao}
          aria-describedby="erro-descricao"
        />
        <span className="suave contador">
          {descricao.trim().length}/{LIMITE_DESCRICAO}
        </span>
        <ErroCampo id="erro-descricao" texto={erros.descricao} />
      </label>
      <div className="acoes">
        <button type="submit" disabled={aEnviar}>
          {aEnviar ? textoAEnviar : textoBotao}
        </button>
        {aoCancelar && (
          <button type="button" className="secundario" onClick={aoCancelar} disabled={aEnviar}>
            Cancelar
          </button>
        )}
      </div>
    </form>
  )
}
