import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { iniciarSessao, obterUtilizadorAutenticado, terminarSessao, type Utilizador } from '../api/autenticacao'
import { ContextoSessao } from './contexto'
import { EVENTO_SESSAO_TERMINADA, guardarSessao, lerSessao, limparSessao } from './sessao'

const AVISO_SESSAO_TERMINADA = 'A tua sessão terminou. Inicia sessão novamente.'

export function ProvedorSessao({ children }: { children: ReactNode }) {
  const [utilizador, setUtilizador] = useState<Utilizador | null>(() => lerSessao()?.utilizador ?? null)
  const [aviso, setAviso] = useState<string | null>(null)

  useEffect(() => {
    const terminada = () => {
      setUtilizador(null)
      setAviso(AVISO_SESSAO_TERMINADA)
    }
    window.addEventListener(EVENTO_SESSAO_TERMINADA, terminada)
    return () => window.removeEventListener(EVENTO_SESSAO_TERMINADA, terminada)
  }, [])

  // Ao abrir a aplicação com uma sessão guardada, confirma no backend que o token continua válido
  // (pode ter sido feito logout noutro separador). Um 401 dispara o evento acima.
  useEffect(() => {
    if (lerSessao()) obterUtilizadorAutenticado().catch(() => {})
  }, [])

  const entrar = useCallback(async (email: string, password: string) => {
    const sessao = await iniciarSessao(email, password)
    guardarSessao(sessao)
    setAviso(null)
    setUtilizador(sessao.utilizador)
  }, [])

  const sair = useCallback(async () => {
    try {
      await terminarSessao()
    } catch {
      // Mesmo que o servidor não responda, a sessão local termina.
    }
    limparSessao()
    setAviso(null)
    setUtilizador(null)
  }, [])

  const valor = useMemo(() => ({ utilizador, aviso, entrar, sair }), [utilizador, aviso, entrar, sair])
  return <ContextoSessao.Provider value={valor}>{children}</ContextoSessao.Provider>
}
