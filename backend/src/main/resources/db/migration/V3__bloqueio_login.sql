-- Bloqueio do login após tentativas falhadas seguidas (RNF-11, P-29; Ficha 04, Ex. 5, finding SEC-01).
-- Colunas previstas em docs/database.md desde a Ficha 02.
ALTER TABLE utilizador
    ADD COLUMN tentativas_falhadas INTEGER     NOT NULL DEFAULT 0 CHECK (tentativas_falhadas >= 0),
    ADD COLUMN bloqueado_ate       TIMESTAMPTZ;
