// Cliente central da API: junta o token, converte os erros Problem Details e trata a sessão expirada.
import { avisarSessaoTerminada, lerSessao } from '../auth/sessao'

const URL_BASE: string = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1'

export interface ErroCampo {
  campo: string
  mensagem: string
}

export class ErroApi extends Error {
  readonly estado: number
  readonly erros: ErroCampo[]

  constructor(estado: number, mensagem: string, erros: ErroCampo[] = []) {
    super(mensagem)
    this.estado = estado
    this.erros = erros
  }
}

interface Opcoes {
  metodo?: 'GET' | 'POST' | 'PATCH' | 'DELETE'
  corpo?: unknown
  /** false só no login: não envia token e um 401 não termina a sessão. */
  autenticado?: boolean
}

export async function pedido<T>(caminho: string, { metodo = 'GET', corpo, autenticado = true }: Opcoes = {}): Promise<T> {
  const cabecalhos: Record<string, string> = {}
  if (corpo !== undefined) cabecalhos['Content-Type'] = 'application/json'
  if (autenticado) {
    const token = lerSessao()?.token
    if (token) cabecalhos.Authorization = `Bearer ${token}`
  }

  let resposta: Response
  try {
    resposta = await fetch(URL_BASE + caminho, {
      method: metodo,
      headers: cabecalhos,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
    })
  } catch {
    throw new ErroApi(0, 'Não foi possível contactar o servidor. Verifica a ligação e tenta novamente.')
  }

  if (resposta.status === 401 && autenticado) {
    avisarSessaoTerminada()
  }
  if (!resposta.ok) {
    const problema = await lerProblema(resposta)
    throw new ErroApi(resposta.status, problema.detail ?? 'Ocorreu um erro inesperado. Tenta novamente.', problema.erros)
  }
  if (resposta.status === 204) return undefined as T
  return (await resposta.json()) as T
}

async function lerProblema(resposta: Response): Promise<{ detail?: string; erros?: ErroCampo[] }> {
  try {
    return await resposta.json()
  } catch {
    return {}
  }
}
