-- Pesquisa de projetos pelo nome sem distinguir maiúsculas nem acentos (US-03 crit. 4, openapi `pesquisa`).
-- A extensão unaccent faz parte do PostgreSQL (contrib) e é "trusted" desde a versão 13.
CREATE EXTENSION IF NOT EXISTS unaccent;
