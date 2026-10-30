import type { Contagens as ContagensProjeto } from '../api/projetos'

export function Contagens({ contagens }: { contagens: ContagensProjeto }) {
  return (
    <dl className="contagens">
      <div>
        <dt>Por fazer</dt>
        <dd>{contagens.POR_FAZER}</dd>
      </div>
      <div>
        <dt>Em curso</dt>
        <dd>{contagens.EM_CURSO}</dd>
      </div>
      <div>
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
