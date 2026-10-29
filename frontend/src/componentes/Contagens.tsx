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
