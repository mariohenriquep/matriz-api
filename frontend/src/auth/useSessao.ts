import { useContext } from 'react'
import { ContextoSessao } from './contexto'

export function useSessao() {
  const sessao = useContext(ContextoSessao)
  if (!sessao) throw new Error('useSessao tem de ser usado dentro de ProvedorSessao')
  return sessao
}
