import { useState, type FormEvent } from 'react'
import { Navigate, useLocation } from 'react-router'
import { ErroApi } from '../api/cliente'
import { useSessao } from '../auth/useSessao'

export function Login() {
  const { utilizador, aviso, entrar } = useSessao()
  const local = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [aEnviar, setAEnviar] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  if (utilizador) {
    const destino = (local.state as { de?: string } | null)?.de ?? '/'
    return <Navigate to={destino} replace />
  }

  async function submeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (aEnviar) return
    setAEnviar(true)
    setErro(null)
    try {
      await entrar(email, password)
    } catch (e) {
      // Os campos mantêm-se preenchidos para o utilizador corrigir.
      setErro(e instanceof ErroApi ? e.message : 'Ocorreu um erro inesperado. Tenta novamente.')
      setAEnviar(false)
    }
  }

  return (
    <main className="cartao-login">
      <h1>Matriz</h1>
      <p className="suave">Inicia sessão para veres os teus projetos.</p>

      {aviso && !erro && (
        <p className="aviso" role="status">
          {aviso}
        </p>
      )}
      {erro && (
        <p className="erro" role="alert">
          {erro}
        </p>
      )}

      <form onSubmit={submeter} noValidate>
        <label>
          Email
          <input
            type="email"
            autoComplete="username"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>
        <label>
          Password
          <input
            type="password"
            autoComplete="current-password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </label>
        <button type="submit" disabled={aEnviar || !email.trim() || !password}>
          {aEnviar ? 'A entrar…' : 'Entrar'}
        </button>
      </form>
    </main>
  )
}
