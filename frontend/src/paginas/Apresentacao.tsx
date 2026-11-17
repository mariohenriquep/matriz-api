import { Link } from 'react-router'
import { TopoPublico } from '../componentes/TopoPublico'

const FUNCIONALIDADES = [
  {
    icone: '▦',
    titulo: 'Quadro Kanban',
    texto: 'Arrasta as tarefas entre Por fazer, Em curso e Concluída. Também funciona com o teclado e no telemóvel.',
  },
  {
    icone: '◧',
    titulo: 'Quadro geral',
    texto: 'Todas as tuas tarefas, de todos os projetos, num só ecrã. Filtra por projeto, por prioridade ou só as tuas.',
  },
  {
    icone: '◎',
    titulo: 'Trabalho em equipa',
    texto: 'Vários responsáveis por tarefa, comentários, prioridades e prazos. As tarefas atrasadas ficam à vista.',
  },
  {
    icone: '▤',
    titulo: 'Arquivo de projetos',
    texto: 'Projetos terminados saem do caminho sem se perder nada: ficam só de leitura e podem ser reativados.',
  },
]

const PASSOS = [
  { titulo: 'Cria a tua conta', texto: 'Só precisas de nome, email e password. É grátis.' },
  { titulo: 'Junta-te a uma equipa', texto: 'O gestor de um projeto adiciona-te pelo teu email.' },
  { titulo: 'Trabalhem juntos', texto: 'Cria e acompanha tarefas, comenta e move-as no quadro.' },
]

/** Exemplo do quadro, só decorativo (os leitores de ecrã leem a descrição em texto). */
function QuadroExemplo() {
  const colunas = [
    { estado: 'por-fazer', nome: 'Por fazer', cartoes: [['Alta', 'Rever o orçamento'], ['Média', 'Preparar a reunião']] },
    { estado: 'em-curso', nome: 'Em curso', cartoes: [['Alta', 'Maquetas da página inicial']] },
    { estado: 'concluida', nome: 'Concluída', cartoes: [['Baixa', 'Mapa do site']] },
  ]
  return (
    <div className="quadro-exemplo" aria-hidden="true">
      {colunas.map((coluna) => (
        <div key={coluna.estado} className={`exemplo-coluna ${coluna.estado}`}>
          <p className="exemplo-titulo">{coluna.nome}</p>
          {coluna.cartoes.map(([prioridade, titulo]) => (
            <div key={titulo} className="exemplo-cartao">
              <span className={`exemplo-prioridade ${prioridade === 'Alta' ? 'alta' : 'media'}`}>{prioridade}</span>
              <span>{titulo}</span>
            </div>
          ))}
        </div>
      ))}
    </div>
  )
}

/** Página inicial pública: apresentação da Matriz e convite para criar conta ou entrar (versão 0.8.0). */
export function Apresentacao() {
  return (
    <div className="publico">
      <TopoPublico />
      <main>
        <section className="destaque" aria-labelledby="titulo-destaque">
          <div className="destaque-texto">
            <p className="sobretitulo">Gestão de projetos e tarefas para equipas</p>
            <h1 id="titulo-destaque">Os projetos da tua equipa, organizados num só quadro.</h1>
            <p className="destaque-descricao">
              A Matriz junta projetos, tarefas, responsáveis e prazos num sítio simples de usar. Cada pessoa vê o
              que tem de fazer e toda a equipa sabe em que ponto está o trabalho.
            </p>
            <div className="destaque-acoes">
              <Link to="/registo" className="botao grande">
                Criar conta grátis
              </Link>
              <Link to="/login" className="botao secundario grande">
                Já tenho conta
              </Link>
            </div>
          </div>
          <QuadroExemplo />
        </section>

        <section className="seccao-publica" aria-labelledby="titulo-funcionalidades">
          <h2 id="titulo-funcionalidades">O que podes fazer</h2>
          <ul className="funcionalidades">
            {FUNCIONALIDADES.map((f) => (
              <li key={f.titulo} className="cartao">
                <span className="funcionalidade-icone" aria-hidden="true">
                  {f.icone}
                </span>
                <h3>{f.titulo}</h3>
                <p className="suave">{f.texto}</p>
              </li>
            ))}
          </ul>
        </section>

        <section className="seccao-publica" aria-labelledby="titulo-passos">
          <h2 id="titulo-passos">Como começar</h2>
          <ol className="passos">
            {PASSOS.map((passo, i) => (
              <li key={passo.titulo}>
                <span className="passo-numero" aria-hidden="true">
                  {i + 1}
                </span>
                <div>
                  <h3>{passo.titulo}</h3>
                  <p className="suave">{passo.texto}</p>
                </div>
              </li>
            ))}
          </ol>
          <p className="nota-plano">
            Com a conta gratuita participas nos projetos para que te convidam. Criar os teus próprios projetos faz
            parte do plano Profissional, disponível em breve.
          </p>
        </section>
      </main>
      <footer className="rodape-publico">
        <p className="suave">Matriz</p>
      </footer>
    </div>
  )
}
