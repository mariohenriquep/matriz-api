import type { Contagens as ContagensProjeto } from '../api/projetos'

export function Contagens({ contagens }: { contagens: ContagensProjeto }) {
  return (
    <dl className="contagens">
      <div className="estado-por_fazer">
        <dt>Por fazer</dt>
        <dd>{contagens.POR_FAZER}</dd>
      </div>
      <div className="estado-em_curso">
        <dt>Em curso</dt>
        <dd>{contagens.EM_CURSO}</dd>
      </div>
      <div className="estado-concluida">
        <dt>Concluídas</dt>
        <dd>{contagens.CONCLUIDA}</dd>
      </div>
    </dl>
  )
}

/** Barra de progresso do wireframe: percentagem de tarefas concluídas. */
export function Progresso({ contagens }: { contagens: ContagensProjeto }) {
  const percentagem = contagens.total === 0 ? 0 : Math.round((contagens.CONCLUIDA / contagens.total) * 100)
  const texto =
    contagens.total === 0
      ? 'Ainda sem tarefas'
      : `${percentagem}% concluído · ${contagens.CONCLUIDA} de ${contagens.total} tarefa${contagens.total === 1 ? '' : 's'}`
  return (
    <div className="progresso">
      <div
        className="progresso-barra"
        role="progressbar"
        aria-label="Tarefas concluídas"
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={percentagem}
        aria-valuetext={texto}
      >
        <span style={{ width: `${percentagem}%` }} />
      </div>
      <p className="suave">{texto}</p>
    </div>
  )
}

/** Cartões de resumo do painel: um por estado, com a cor do estado. */
export function ResumoEstados({ contagens }: { contagens: ContagensProjeto }) {
  const estados = [
    { chave: 'por_fazer', nome: 'Por fazer', valor: contagens.POR_FAZER },
    { chave: 'em_curso', nome: 'Em curso', valor: contagens.EM_CURSO },
    { chave: 'concluida', nome: 'Concluídas', valor: contagens.CONCLUIDA },
  ]
  return (
    <dl className="resumo-estados">
      {estados.map((e) => (
        <div key={e.chave} className={`resumo-estado estado-${e.chave}`}>
          <span className="resumo-icone" aria-hidden="true">
            <span className="ponto" />
          </span>
          <dt>{e.nome}</dt>
          <dd>{e.valor}</dd>
        </div>
      ))}
    </dl>
  )
}
