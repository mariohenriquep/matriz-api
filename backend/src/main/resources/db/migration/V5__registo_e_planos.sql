-- Registo público de contas (versão 0.8.0) e plano de cada conta.
-- O plano decide o que a conta pode fazer além de participar nos projetos para que é convidada:
--   GRATUITO      conta criada pelo registo público: só entra em projetos quando um Gestor a adiciona.
--   PROFISSIONAL  pode também criar projetos (e ficar Gestor). No futuro será um plano pago.
-- As contas que já existem (seed e contas criadas antes desta versão) ficam PROFISSIONAL, para não
-- perderem o que já podiam fazer. As novas ficam GRATUITO por omissão.
ALTER TABLE utilizador
    ADD COLUMN plano VARCHAR(20) NOT NULL DEFAULT 'PROFISSIONAL'
        CHECK (plano IN ('GRATUITO', 'PROFISSIONAL'));

ALTER TABLE utilizador ALTER COLUMN plano SET DEFAULT 'GRATUITO';
