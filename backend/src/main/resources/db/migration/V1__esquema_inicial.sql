-- Matriz: esquema inicial (Ficha 03, Exercício 2).
-- Segue docs/database.md. As tabelas e colunas dos extras do MVP (notificações, administração global,
-- arquivo, bloqueio de login) entram em migrations próprias quando forem implementadas.

CREATE TABLE utilizador (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome           VARCHAR(100)  NOT NULL CHECK (btrim(nome) <> ''),
    email          VARCHAR(254)  NOT NULL CHECK (btrim(email) <> ''),
    password_hash  VARCHAR(100)  NOT NULL,
    versao_token   INTEGER       NOT NULL DEFAULT 0,
    criado_em      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Email único sem distinguir maiúsculas (CE-08)
CREATE UNIQUE INDEX uk_utilizador_email ON utilizador (lower(email));

CREATE TABLE projeto (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome           VARCHAR(100)  NOT NULL CHECK (btrim(nome) <> ''),
    descricao      VARCHAR(1000),
    gestor_id      BIGINT        NOT NULL REFERENCES utilizador (id),
    criado_em      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX ix_projeto_gestor ON projeto (gestor_id);

-- O Gestor também tem linha aqui: os membros de um projeto incluem sempre o Gestor.
CREATE TABLE membro_projeto (
    projeto_id     BIGINT        NOT NULL REFERENCES projeto (id) ON DELETE CASCADE,
    utilizador_id  BIGINT        NOT NULL REFERENCES utilizador (id),
    adicionado_em  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    PRIMARY KEY (projeto_id, utilizador_id)
);

CREATE INDEX ix_membro_utilizador ON membro_projeto (utilizador_id);

CREATE TABLE tarefa (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    projeto_id     BIGINT        NOT NULL REFERENCES projeto (id) ON DELETE CASCADE,
    titulo         VARCHAR(200)  NOT NULL CHECK (btrim(titulo) <> ''),
    descricao      VARCHAR(2000),
    estado         VARCHAR(20)   NOT NULL DEFAULT 'POR_FAZER'
                   CHECK (estado IN ('POR_FAZER', 'EM_CURSO', 'CONCLUIDA')),
    prioridade     VARCHAR(10)   NOT NULL DEFAULT 'MEDIA'
                   CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA')),
    criador_id     BIGINT        NOT NULL REFERENCES utilizador (id),
    data_limite    DATE,
    criado_em      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    atualizado_em  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX ix_tarefa_projeto ON tarefa (projeto_id);

CREATE TABLE tarefa_responsavel (
    tarefa_id      BIGINT        NOT NULL REFERENCES tarefa (id) ON DELETE CASCADE,
    utilizador_id  BIGINT        NOT NULL REFERENCES utilizador (id),
    atribuido_em   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    PRIMARY KEY (tarefa_id, utilizador_id)
);

CREATE INDEX ix_responsavel_utilizador ON tarefa_responsavel (utilizador_id);

CREATE TABLE comentario (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tarefa_id      BIGINT        NOT NULL REFERENCES tarefa (id) ON DELETE CASCADE,
    autor_id       BIGINT        NOT NULL REFERENCES utilizador (id),
    conteudo       VARCHAR(2000) NOT NULL CHECK (btrim(conteudo) <> ''),
    criado_em      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX ix_comentario_tarefa ON comentario (tarefa_id, criado_em);
