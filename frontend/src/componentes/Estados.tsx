// Estados partilhados pelos ecrãs: a carregar, erro (com nova tentativa) e mensagens de sucesso.
import type { ReactNode } from 'react'

export function ACarregar({ texto }: { texto: string }) {
  return (
    <p className="suave" role="status" aria-live="polite">
      {texto}
    </p>
  )
}

export function MensagemErro({ texto, tentarNovamente }: { texto: string; tentarNovamente?: () => void }) {
  return (
    <div className="erro" role="alert">
      {texto}
      {tentarNovamente && (
        <button type="button" className="secundario pequeno" onClick={tentarNovamente}>
          Tentar novamente
        </button>
      )}
    </div>
  )
}

export function MensagemSucesso({ children }: { children: ReactNode }) {
  return (
    <p className="sucesso" role="status" aria-live="polite">
      {children}
    </p>
  )
}

/** Mensagem de erro de um campo, vinda da validação do backend ou do formulário. */
export function ErroCampo({ id, texto }: { id: string; texto?: string }) {
  if (!texto) return null
  return (
    <span id={id} className="erro-campo">
      {texto}
    </span>
  )
}
