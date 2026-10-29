import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { criarProjeto, listarProjetos, type Projeto } from '../api/projetos'
import { Barra } from '../componentes/Barra'
import { Contagens } from '../componentes/Contagens'
import { ACarregar, MensagemErro, MensagemSucesso } from '../componentes/Estados'
import { FormularioProjeto } from '../componentes/FormularioProjeto'

type Estado =
  | { tipo: 'a-carregar' }
  | { tipo: 'erro'; mensagem: string }
  | { tipo: 'pronto'; projetos: Projeto[] }

/** Painel inicial: projetos acessíveis, contagens por estado, pesquisa e criação (Ficha 03, Ex. 4). */
export function Inicio() {
  const [pesquisa, setPesquisa] = useState('')
  const [pesquisaAplicada, setPesquisaAplicada] = useState('')
  const [estado, setEstado] = useState<Estado>({ tipo: 'a-carregar' })
  const [aCriar, setACriar] = useState(false)
  const [sucesso, setSucesso] = useState<string | null>(null)

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
      .then((lista) => ativo && setEstado({ tipo: 'pronto', projetos: lista.itens }))
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
        <div className="cabecalho">
          <h1>Os meus projetos</h1>
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
                <h2>
                  <Link to={`/projetos/${projeto.id}`}>{projeto.nome}</Link>
                </h2>
                <p className="suave">
                  {projeto.meuPapel === 'GESTOR' ? 'És o gestor' : `Gestor: ${projeto.gestor.nome}`}
                </p>
                {projeto.descricao && <p className="descricao">{projeto.descricao}</p>}
                <Contagens contagens={projeto.contagens} />
              </li>
            ))}
          </ul>
        )}
      </main>
    </>
  )
}
