import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import { registar } from '../api/autenticacao'
import { ErroApi } from '../api/cliente'
import { caracteres } from '../api/texto'
import { useSessao } from '../auth/useSessao'
import { TopoPublico } from '../componentes/TopoPublico'

type Campo = 'nome' | 'email' | 'password' | 'confirmacao'

export const MIN_PASSWORD = 8
const MAX_PASSWORD = 64

/** Verificações rápidas antes de enviar; as regras completas (ex.: passwords comuns) são do backend. */
function validar(nome: string, email: string, password: string, confirmacao: string) {
  const erros: Partial<Record<Campo, string>> = {}
  if (!nome.trim()) erros.nome = 'O nome é obrigatório'
  if (!email.trim()) erros.email = 'O email é obrigatório'
  else if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email.trim())) erros.email = 'O email não é válido'
  const tamanho = caracteres(password)
  if (tamanho < MIN_PASSWORD) erros.password = `A password tem de ter pelo menos ${MIN_PASSWORD} caracteres`
  else if (tamanho > MAX_PASSWORD) erros.password = `A password tem no máximo ${MAX_PASSWORD} caracteres`
  if (confirmacao !== password) erros.confirmacao = 'As passwords não coincidem'
  return erros
}

/**
 * Criar conta (versão 0.8.0). A conta fica no plano gratuito. A resposta do servidor é sempre a mesma, exista
 * ou não uma conta com o email, por isso segue-se sempre para o login com a mensagem do servidor.
 */
export function Registo() {
  const { utilizador } = useSessao()
  const navegar = useNavigate()
  const [nome, setNome] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [site, setSite] = useState('')
  const [erros, setErros] = useState<Partial<Record<Campo, string>>>({})
  const [erro, setErro] = useState<string | null>(null)
  const [aEnviar, setAEnviar] = useState(false)

  if (utilizador) return <Navigate to="/" replace />

  async function submeter(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault()
    if (aEnviar) return
    const encontrados = validar(nome, email, password, confirmacao)
    setErros(encontrados)
    setErro(null)
    if (Object.keys(encontrados).length > 0) return
    setAEnviar(true)
    try {
      const resposta = await registar({ nome: nome.trim(), email: email.trim(), password, site })
      navegar('/login', { state: { mensagem: resposta.mensagem, email: email.trim() } })
    } catch (e) {
      if (e instanceof ErroApi && e.erros.length > 0) {
        const doServidor: Partial<Record<Campo, string>> = {}
        for (const { campo, mensagem } of e.erros) {
          if (campo === 'nome' || campo === 'email' || campo === 'password') doServidor[campo] = mensagem
        }
        setErros(doServidor)
      }
      setErro(e instanceof ErroApi ? e.message : 'Ocorreu um erro inesperado. Tenta novamente.')
      setAEnviar(false)
    }
  }

  function campo(nomeCampo: Campo) {
    const mensagem = erros[nomeCampo]
    return {
      'aria-invalid': mensagem ? true : undefined,
      'aria-describedby': mensagem ? `erro-${nomeCampo}` : nomeCampo === 'password' ? 'ajuda-password' : undefined,
    }
  }

  function erroDe(nomeCampo: Campo) {
    const mensagem = erros[nomeCampo]
    return mensagem ? (
      <span className="erro-campo" id={`erro-${nomeCampo}`}>
        {mensagem}
      </span>
    ) : null
  }

  return (
    <div className="publico">
      <TopoPublico mostrar="entrar" />
      <main className="cartao-login">
        <h1>Criar conta</h1>
        <p className="suave">
          É grátis. Depois de criares a conta, o gestor de um projeto pode adicionar-te à equipa pelo teu email.
        </p>

        {erro && (
          <p className="erro" role="alert">
            {erro}
          </p>
        )}

        <form onSubmit={submeter} noValidate>
          <div className="campo">
            <label htmlFor="registo-nome">Nome</label>
            <input
              id="registo-nome"
              autoComplete="name"
              maxLength={100}
              required
              value={nome}
              onChange={(e) => setNome(e.target.value)}
              {...campo('nome')}
            />
            {erroDe('nome')}
          </div>
          <div className="campo">
            <label htmlFor="registo-email">Email</label>
            <input
              id="registo-email"
              type="email"
              autoComplete="email"
              maxLength={254}
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              {...campo('email')}
            />
            {erroDe('email')}
          </div>
          <div className="campo">
            <label htmlFor="registo-password">Password</label>
            <input
              id="registo-password"
              type="password"
              autoComplete="new-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              {...campo('password')}
            />
            {erroDe('password') ?? (
              <span className="ajuda" id="ajuda-password">
                Pelo menos {MIN_PASSWORD} caracteres. Evita passwords comuns e o teu email.
              </span>
            )}
          </div>
          <div className="campo">
            <label htmlFor="registo-confirmacao">Confirmar password</label>
            <input
              id="registo-confirmacao"
              type="password"
              autoComplete="new-password"
              required
              value={confirmacao}
              onChange={(e) => setConfirmacao(e.target.value)}
              {...campo('confirmacao')}
            />
            {erroDe('confirmacao')}
          </div>
          {/* Armadilha para robôs: escondida das pessoas e dos leitores de ecrã, fora da ordem do teclado */}
          <div className="armadilha" aria-hidden="true">
            <label>
              Site
              <input tabIndex={-1} autoComplete="off" value={site} onChange={(e) => setSite(e.target.value)} />
            </label>
          </div>
          <button type="submit" disabled={aEnviar}>
            {aEnviar ? 'A criar conta…' : 'Criar conta'}
          </button>
        </form>
        <p className="troca-conta suave">
          Já tens conta? <Link to="/login">Inicia sessão</Link>
        </p>
      </main>
    </div>
  )
}
