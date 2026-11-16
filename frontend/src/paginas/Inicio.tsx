import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router'
import { criarProjeto, listarProjetos, somarContagens, type Contagens as ContagensProjeto, type Projeto } from '../api/projetos'
import { useSessao } from '../auth/useSessao'
import { Barra } from '../componentes/Barra'
import { Contagens, Progresso, ResumoEstados } from '../componentes/Contagens'
import { ACarregar, MensagemErro, MensagemSucesso } from '../componentes/Estados'
import { FormularioProjeto } from '../componentes/FormularioProjeto'
import { QuadroGeral } from '../componentes/QuadroGeral'
import { formatarDataHora } from '../api/tarefas'

type Vista = 'ativos' | 'quadro' | 'arquivados'

const SEPARADORES: { vista: Vista; nome: string }[] = [
  { vista: 'ativos', nome: 'Ativos' },
  { vista: 'quadro', nome: 'Quadro geral' },
  { vista: 'arquivados', nome: 'Arquivados' },
]

type Estado =
  | { tipo: 'a-carregar' }
  | { tipo: 'erro'; mensagem: string }
  | { tipo: 'pronto'; projetos: Projeto[] }

/**
 * Painel inicial: saudação, resumo das tarefas, projetos acessíveis, pesquisa e criação (Ficha 03, Ex. 4 e 7).
 * Separadores Ativos, Quadro geral (Kanban com as tarefas de todos os projetos ativos) e Arquivados;
 * o separador fica no URL (`?vista=quadro`, `?vista=arquivados`).
 */
export function Inicio() {
  const { utilizador } = useSessao()
  const [parametros, setParametros] = useSearchParams()
  const vistaUrl = parametros.get('vista')
  const vista: Vista = vistaUrl === 'quadro' || vistaUrl === 'arquivados' ? vistaUrl : 'ativos'
  const arquivados = vista === 'arquivados'
  const noQuadro = vista === 'quadro'
  const [pesquisa, setPesquisa] = useState('')
  const [pesquisaAplicada, setPesquisaAplicada] = useState('')
  const [estado, setEstado] = useState<Estado>({ tipo: 'a-carregar' })
  const [aCriar, setACriar] = useState(false)
  const localizacao = useLocation()
  const navegar = useNavigate()
  // Mensagem deixada por outra página (por exemplo, depois de eliminar um projeto)
  const [sucesso, setSucesso] = useState<string | null>(
    (localizacao.state as { mensagem?: string } | null)?.mensagem ?? null,
  )
  // O resumo conta todos os projetos, por isso só se atualiza com a lista sem pesquisa
  const [resumo, setResumo] = useState<{ projetos: number; contagens: ContagensProjeto } | null>(null)
  // Projetos ativos (sem pesquisa), para o filtro e os nomes do quadro geral
  const [ativos, setAtivos] = useState<Projeto[] | null>(null)

  const [versao, setVersao] = useState(0)

  // Limpa a mensagem do histórico para não reaparecer ao recarregar a página
  useEffect(() => {
    if (localizacao.state) navegar(localizacao.pathname, { replace: true, state: null })
  }, [localizacao, navegar])

  // Pesquisa ao escrever, com uma pequena pausa para não fazer um pedido por tecla
  useEffect(() => {
    if (pesquisa === pesquisaAplicada) return
    const espera = setTimeout(() => {
      setEstado({ tipo: 'a-carregar' })
      setPesquisaAplicada(pesquisa)
    }, 300)
    return () => clearTimeout(espera)
  }, [pesquisa, pesquisaAplicada])

  useEffect(() => {
    let ativo = true // ignora respostas de pesquisas antigas que cheguem depois
    // No quadro geral a pesquisa não se aplica: pede-se sempre a lista completa de projetos ativos
    const texto = noQuadro ? '' : pesquisaAplicada
    listarProjetos(texto, arquivados)
      .then((lista) => {
        if (!ativo) return
        setEstado({ tipo: 'pronto', projetos: lista.itens })
        if (!texto.trim() && !arquivados) {
          setResumo({ projetos: lista.itens.length, contagens: somarContagens(lista.itens) })
          setAtivos(lista.itens)
        }
      })
      .catch(
        (e) =>
          ativo &&
          setEstado({ tipo: 'erro', mensagem: e instanceof Error ? e.message : 'Não foi possível carregar os projetos.' }),
      )
    return () => {
      ativo = false
    }
  }, [pesquisaAplicada, arquivados, noQuadro, versao])

  function mudarVista(nova: Vista) {
    if (nova === vista) return
    setEstado({ tipo: 'a-carregar' })
    setSucesso(null)
    setParametros(nova === 'ativos' ? {} : { vista: nova }, { replace: true })
  }

  function recarregar() {
    setEstado({ tipo: 'a-carregar' })
    setVersao((v) => v + 1)
  }

  async function criar(dados: { nome: string; descricao: string | null }) {
    const projeto = await criarProjeto(dados)
    setACriar(false)
    setSucesso(`Projeto "${projeto.nome}" criado.`)
    setPesquisa('')
    setPesquisaAplicada('')
    setParametros({}, { replace: true })
    recarregar()
  }

  return (
    <>
      <Barra />
      <main className="conteudo">
        <div className="cabecalho">
          <section className="resumo" aria-labelledby="saudacao">
            <h1 id="saudacao">Olá, {utilizador?.nome}</h1>
            {resumo && (
              <p className="suave">
                {resumo.projetos === 0
                  ? 'Ainda não participas em nenhum projeto.'
                  : `Participas em ${resumo.projetos} projeto${resumo.projetos === 1 ? '' : 's'}. Tarefas em todos eles:`}
              </p>
            )}
          </section>
          {!aCriar && (
            <button type="button" onClick={() => { setACriar(true); setSucesso(null) }}>
              + Novo projeto
            </button>
          )}
        </div>
        {resumo && resumo.projetos > 0 && <ResumoEstados contagens={resumo.contagens} />}

        {sucesso && <MensagemSucesso>{sucesso}</MensagemSucesso>}

        {aCriar && (
          <section className="cartao" aria-label="Novo projeto">
            <h2>Novo projeto</h2>
            <FormularioProjeto
              textoBotao="Criar projeto"
              textoAEnviar="A criar…"
              aoSubmeter={criar}
              aoCancelar={() => setACriar(false)}
            />
          </section>
        )}

        <h2 className="visualmente-escondido">Os meus projetos</h2>
        <div className="barra-ferramentas">
          <div className="separadores" role="tablist" aria-label="Vista da página inicial">
            {SEPARADORES.map((separador) => (
              <button
                key={separador.vista}
                type="button"
                role="tab"
                aria-selected={vista === separador.vista}
                className={vista === separador.vista ? 'ativo' : undefined}
                onClick={() => mudarVista(separador.vista)}
              >
                {separador.nome}
              </button>
            ))}
          </div>
          {!noQuadro && (
            <label className="pesquisa">
              <span className="visualmente-escondido">Pesquisar pelo nome</span>
              <input
                type="search"
                value={pesquisa}
                onChange={(e) => setPesquisa(e.target.value)}
                placeholder="Pesquisar projetos…"
              />
            </label>
          )}
        </div>
        {arquivados && (
          <p className="suave">
            Projetos arquivados ficam só de leitura: as tarefas e os comentários mantêm-se. O gestor pode reativá-los
            ou eliminá-los.
          </p>
        )}

        {noQuadro &&
          (ativos ? (
            <QuadroGeral projetos={ativos} aoMudar={() => setVersao((v) => v + 1)} />
          ) : estado.tipo === 'erro' ? (
            <MensagemErro texto={estado.mensagem} tentarNovamente={recarregar} />
          ) : (
            <ACarregar texto="A carregar as tarefas…" />
          ))}

        {!noQuadro && estado.tipo === 'a-carregar' && <ACarregar texto="A carregar projetos…" />}
        {!noQuadro && estado.tipo === 'erro' && <MensagemErro texto={estado.mensagem} tentarNovamente={recarregar} />}
        {!noQuadro && estado.tipo === 'pronto' && estado.projetos.length === 0 && (
          <div className="vazio">
            {pesquisaAplicada.trim() ? (
              <p>Nenhum projeto corresponde a "{pesquisaAplicada.trim()}".</p>
            ) : arquivados ? (
              <p>Não há projetos arquivados. O gestor pode arquivar um projeto na página do projeto.</p>
            ) : (
              <>
                <p>Ainda não participas em nenhum projeto.</p>
                {!aCriar && (
                  <button type="button" onClick={() => setACriar(true)}>
                    Criar projeto
                  </button>
                )}
              </>
            )}
          </div>
        )}
        {!noQuadro && estado.tipo === 'pronto' && estado.projetos.length > 0 && (
          <ul className="grelha">
            {estado.projetos.map((projeto) => (
              <li key={projeto.id} className={projeto.arquivado ? 'cartao cartao-projeto arquivado' : 'cartao cartao-projeto'}>
                <div className="projeto-topo">
                  <span className="projeto-icone" aria-hidden="true">
                    {projeto.nome.trim().charAt(0).toUpperCase()}
                  </span>
                  <div className="projeto-titulo">
                    <h3>
                      <Link to={`/projetos/${projeto.id}`}>{projeto.nome}</Link>
                    </h3>
                    <p className="suave">
                      {projeto.meuPapel === 'GESTOR' ? 'És o gestor' : `Gestor: ${projeto.gestor.nome}`}
                    </p>
                  </div>
                  {projeto.arquivado && <span className="etiqueta">Arquivado</span>}
                </div>
                {projeto.arquivado && projeto.arquivadoEm && (
                  <p className="suave">Arquivado em {formatarDataHora(projeto.arquivadoEm)}</p>
                )}
                {projeto.descricao && <p className="descricao">{projeto.descricao}</p>}
                <Progresso contagens={projeto.contagens} />
                <Contagens contagens={projeto.contagens} />
              </li>
            ))}
          </ul>
        )}
      </main>
    </>
  )
}
