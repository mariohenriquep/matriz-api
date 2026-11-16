-- Arquivo de projetos (extra do MVP, decisão de 2026-10-28): arquivar em vez de apagar.
-- NULL = projeto ativo. Um projeto arquivado fica só de leitura; eliminar continua possível (Ficha 03).
ALTER TABLE projeto ADD COLUMN arquivado_em TIMESTAMPTZ;
