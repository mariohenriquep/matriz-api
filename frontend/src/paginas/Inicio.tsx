import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { criarProjeto, listarProjetos, somarContagens, type Contagens as ContagensProjeto, type Projeto } from '../api/projetos'
import { useSessao } from '../auth/useSessao'
import { Barra } from '../componentes/Barra'
import { Contagens, Progresso } from '../componentes/Contagens'
import { ACarregar, MensagemErro, MensagemSucesso } from '../componentes/Estados'
import { FormularioProjeto } from '../componentes/FormularioProjeto'

type Estado =
  | { tipo: 'a-carregar' }
  | { tipo: 'erro'; mensagem: string }
  | { tipo: 'pronto'; projetos: Projeto[] }

/** Painel inicial: saudação, resumo das tarefas, projetos acessíveis, pesquisa e criação (Ficha 03, Ex. 4 e 7). */
export function Inicio() {
  const { utilizador } = useSessao()
  const [pesquisa, setPesquisa] = useState('')
  const [pesquisaAplicada, setPesquisaAplicada] = useState('')
  const [estado, setEstado] = useState<Estado>({ tipo: 'a-carregar' })
  const [aCriar, setACriar] = useState(false)
  const [sucesso, setSucesso] = useState<string | null>(null)
  // O resumo conta todos os projetos, por isso só se atualiza com a lista sem pesquisa
  const [resumo, setResumo] = useState<{ projetos: number; contagens: ContagensProjeto } | null>(null)

  const [versao, setVersao] = useState(0)

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
    listarProjetos(pesquisaAplicada)
      .then((lista) => {
        if (!ativo) return
        setEstado({ tipo: 'pronto', projetos: lista.itens })
        if (!pesquisaAplicada.trim()) setResumo({ projetos: lista.itens.length, contagens: somarContagens(lista.itens) })
      })
      .catch(
        (e) =>
          ativo &&
          setEstado({ tipo: 'erro', mensagem: e instanceof Error ? e.message : 'Não foi possível carregar os projetos.' }),
      )
    return () => {
      ativo = false
    }
  }, [pesquisaAplicada, versao])

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
    recarregar()
  }

  return (
    <>
      <Barra />
      <main className="conteudo">
        <section className="resumo" aria-labelledby="saudacao">
          <h1 id="saudacao">Olá, {utilizador?.nome}</h1>
          {resumo && (
            <>
              <p className="suave">
                {resumo.projetos === 0
                  ? 'Ainda não participas em nenhum projeto.'
                  : `Participas em ${resumo.projetos} projeto${resumo.projetos === 1 ? '' : 's'}. Tarefas em todos eles:`}
              </p>
              {resumo.projetos > 0 && <Contagens contagens={resumo.contagens} />}
            </>
          )}
        </section>

        <div className="cabecalho">
          <h2>Os meus projetos</h2>
          {!aCriar && (
            <button type="button" onClick={() => { setACriar(true); setSucesso(null) }}>
              Novo projeto
            </button>
          )}
        </div>

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

        <label className="pesquisa">
          Pesquisar pelo nome
          <input type="search" value={pesquisa} onChange={(e) => setPesquisa(e.target.value)} placeholder="ex.: website" />
        </label>

        {estado.tipo === 'a-carregar' && <ACarregar texto="A carregar projetos…" />}
        {estado.tipo === 'erro' && (
          <MensagemErro texto={estado.mensagem} tentarNovamente={recarregar} />
        )}
        {estado.tipo === 'pronto' && estado.projetos.length === 0 && (
          <div className="vazio">
            {pesquisaAplicada.trim() ? (
              <p>Nenhum projeto corresponde a "{pesquisaAplicada.trim()}".</p>
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
        {estado.tipo === 'pronto' && estado.projetos.length > 0 && (
          <ul className="grelha">
            {estado.projetos.map((projeto) => (
              <li key={projeto.id} className="cartao">
                <h3>
                  <Link to={`/projetos/${projeto.id}`}>{projeto.nome}</Link>
                </h3>
                <p className="suave">
                  {projeto.meuPapel === 'GESTOR' ? 'És o gestor' : `Gestor: ${projeto.gestor.nome}`}
                </p>
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
