import { afterEach, describe, expect, it, vi } from 'vitest'
import { EVENTO_SESSAO_TERMINADA, guardarSessao, lerSessao } from '../auth/sessao'
import { ana } from '../testes/dados'
import { ErroApi, pedido } from './cliente'

function responder(estado: number, corpo?: unknown) {
  vi.stubGlobal(
    'fetch',
    vi.fn().mockResolvedValue(
      new Response(corpo === undefined ? null : JSON.stringify(corpo), {
        status: estado,
        headers: { 'Content-Type': 'application/problem+json' },
      }),
    ),
  )
}

function iniciarSessaoFicticia() {
  guardarSessao({ token: 'token-ficticio', expiraEm: new Date(Date.now() + 3_600_000).toISOString(), utilizador: ana })
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('cliente da API', () => {
  it('envia o token da sessão no cabeçalho Authorization', async () => {
    iniciarSessaoFicticia()
    responder(200, { itens: [] })

    await pedido('/projetos')

    const [, opcoes] = vi.mocked(fetch).mock.calls[0]
    expect(new Headers(opcoes!.headers).get('Authorization')).toBe('Bearer token-ficticio')
  })

  it('um 401 num pedido autenticado termina a sessão e avisa a aplicação', async () => {
    iniciarSessaoFicticia()
    responder(401, { detail: 'Sessão expirada' })
    const aoTerminar = vi.fn()
    window.addEventListener(EVENTO_SESSAO_TERMINADA, aoTerminar)

    await expect(pedido('/projetos')).rejects.toBeInstanceOf(ErroApi)

    expect(lerSessao()).toBeNull()
    expect(aoTerminar).toHaveBeenCalledTimes(1)
    window.removeEventListener(EVENTO_SESSAO_TERMINADA, aoTerminar)
  })

  it('um 401 no login não termina a sessão (credenciais erradas)', async () => {
    responder(401, { detail: 'Email ou palavra-passe incorretos' })
    const aoTerminar = vi.fn()
    window.addEventListener(EVENTO_SESSAO_TERMINADA, aoTerminar)

    await expect(pedido('/auth/login', { metodo: 'POST', corpo: {}, autenticado: false })).rejects.toThrow(
      'Email ou palavra-passe incorretos',
    )

    expect(aoTerminar).not.toHaveBeenCalled()
    window.removeEventListener(EVENTO_SESSAO_TERMINADA, aoTerminar)
  })

  it('converte Problem Details em ErroApi com os erros de campo', async () => {
    responder(400, { detail: 'Dados inválidos', erros: [{ campo: 'titulo', mensagem: 'O título é obrigatório' }] })

    const erro = await pedido('/tarefas/t-1', { metodo: 'PATCH', corpo: {} }).catch((e: unknown) => e)

    expect(erro).toBeInstanceOf(ErroApi)
    expect((erro as ErroApi).estado).toBe(400)
    expect((erro as ErroApi).erros).toEqual([{ campo: 'titulo', mensagem: 'O título é obrigatório' }])
  })

  it('sem ligação ao servidor dá uma mensagem compreensível', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    await expect(pedido('/projetos')).rejects.toThrow('Não foi possível contactar o servidor')
  })
})
