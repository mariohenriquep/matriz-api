# Plano de implementação — Matriz (Ficha 03)

> Ficha 03, Exercício 1. Atualizado ao longo da implementação (ver a coluna "Estado" em §5).
> Legenda: ✅ decisão confirmada pelo Mário · *Proposta* = ainda por validar.

## 1. Conciliação do âmbito

A Ficha 03 define um âmbito funcional mínimo. Foi comparado com o MVP aprovado nas Fichas 01 e 02 e o Mário decidiu (2026-10-29) implementar primeiro **todo o âmbito da Ficha 03** e só depois os extras do nosso MVP.

| # | Diferença | Decisão | Documentos atualizados |
|---|-----------|---------|------------------------|
| C-01 | Vários responsáveis por tarefa (antes: no máximo um, P-07) | ✅ Aceite | requirements RF-10, US-08, database (TAREFA_RESPONSAVEL), P-07 |
| C-02 | Comentários com autor e data (antes: fora do âmbito) | ✅ Aceite | requirements RF-20, US-17, database (COMENTARIO) |
| C-03 | Eliminar projetos sem dados órfãos (antes: só arquivar, P-10) | ✅ Aceite; o arquivo fica como extra | requirements RF-21, US-18, database RN-10, P-10 |
| C-04 | Pesquisar projetos pelo nome | ✅ Aceite | requirements RF-05, US-03 |
| C-05 | Filtrar tarefas por prioridade | ✅ Aceite | requirements RF-12, US-10 |
| C-06 | Registo público opcional; contas de demonstração por seed | ✅ Seed; registo público fica como extra | requirements RF-01 |
| C-07 | Valores do exemplo da ficha (`TODO`, `LOW`…, título 3–150) | ✅ Mantemos os nossos (`POR_FAZER`, `BAIXA`…, título 1–200) | — |

**Extras do nosso MVP adiados** (ficam para depois da Ficha 03, por esta ordem proposta): arquivo e reativação (RF-18), quadro Kanban (RF-15), "As minhas tarefas" (RF-17), marcação "Atrasada" na interface (o campo já vem da API), registo público (RF-01), administrador global e registo de promoções (RF-02, RF-13, RF-19), notificações (RF-16), bloqueio após tentativas falhadas (RNF-11).

## 2. Stack ✅

| Camada | Escolha | Notas |
|--------|---------|-------|
| Backend | **Java 21 + Spring Boot 3** | Spring Web, Spring Security, Spring Data JPA, Bean Validation. Build com Maven. |
| Autenticação | **JWT** em `Authorization: Bearer`, validade 8 h | Assinatura HS256 com segredo em variável de ambiente. Logout invalida tokens através de `UTILIZADOR.versao_token`. Hash de passwords com **BCrypt** (Spring Security). |
| Base de dados | **PostgreSQL 16** | Em desenvolvimento, por Docker Compose (ou instalação local). |
| Migrations | **Flyway** | Scripts SQL versionados em `backend/src/main/resources/db/migration`. |
| Seed | Componente Java que corre no arranque **só se não houver utilizadores** | Não apaga dados em cada arranque. |
| Frontend | **React + Vite + TypeScript** | React Router para navegação; `fetch` com um cliente central (`VITE_API_URL`). |
| Testes backend | JUnit 5 + Spring Boot Test + MockMvc + **Testcontainers** (PostgreSQL real) | ✅ O Mário tem Docker no Mac. |
| Testes frontend | Vitest + Testing Library | Poucos testes, focados em formulários e estados. |

### Estrutura do repositório ✅ (criada no Ex. 2)

```
matriz-api/
├── backend/                       # Spring Boot (Maven)
│   └── src/main/java/pt/matriz/
│       ├── identidade/            # login, JWT, utilizadores, segurança
│       ├── projetos/              # projetos e membros
│       ├── tarefas/               # tarefas e responsáveis
│       ├── comentarios/           # comentários
│       └── comum/                 # erros (Problem Details), configuração
├── frontend/                      # React + Vite + TS
│   └── src/{api,componentes,paginas,auth}
├── docker-compose.yml             # PostgreSQL de desenvolvimento
├── .env.example                   # valores fictícios
├── openapi.yaml
└── docs/
```

O módulo **Progresso** de `docs/architecture.md` é implementado como contagens calculadas dentro de `projetos` (consulta agregada às tarefas); não precisa de pacote próprio nesta fase.

## 3. Regras de acesso consolidadas ✅

Identidade: o utilizador é **sempre** obtido do token. O backend nunca aceita do cliente o dono do projeto, o criador da tarefa nem o autor do comentário.

| Operação | Quem pode | Se não pode |
|----------|-----------|-------------|
| Ver projeto, membros, tarefas, comentários | Membros do projeto (inclui o Gestor) | 404 |
| Criar projeto | Qualquer utilizador autenticado (fica Gestor) | — |
| Editar ou eliminar projeto | Só o Gestor | 403 se for membro; 404 se não for |
| Adicionar ou remover membros | Só o Gestor; o Gestor não pode ser removido | 403 / 409 |
| Criar e editar tarefas (incluindo estado e prioridade) | Membros | 404 |
| Eliminar tarefa | Criador da tarefa ou Gestor * | 403 |
| Atribuir ou remover responsáveis | Membros; o responsável tem de ser membro (elegível) | 422 se não for membro; 409 se repetido |
| Comentar | Membros | 404 |
| Editar ou apagar comentários | Ninguém no MVP * | — (não há operação) |

\* Regras herdadas do nosso MVP (US-07) ou propostas no Ex. 1; confirmadas pelo Mário a 2026-10-29.

## 4. Alterações ao modelo de dados

- **TAREFA_RESPONSAVEL** (`tarefa_id`, `utilizador_id`, `atribuido_em`), PK composta: impede duplicados (RN-02 passa a aplicar-se a cada linha).
- **COMENTARIO** (`id`, `tarefa_id`, `autor_id`, `conteudo` ≤ 2000, `criado_em`).
- `TAREFA.responsavel_id` é removido.
- `UTILIZADOR.versao_token` (inteiro) para invalidar tokens no logout.
- `PROJETO.descricao` ≤ 1000; `TAREFA.descricao` ≤ 2000 (novos limites, P-33).
- **RN-10:** eliminar um projeto elimina, na mesma transação, membros, tarefas, atribuições e comentários; eliminar uma tarefa elimina as suas atribuições e comentários (`ON DELETE CASCADE`).
- Ficam por criar nesta fase (extras): NOTIFICACAO, REGISTO_ADMIN, campos de bloqueio de login, `arquivado_em`, `is_admin_global`.

## 5. Incrementos

Cada incremento segue o ciclo pedido pela ficha: análise e plano → validação contigo → implementação → verificações → revisão do diff → commit.

| Inc. | Ex. | Conteúdo | Componentes / ficheiros previstos | Critérios de aceitação | Verificação | Estado |
|------|-----|----------|-----------------------------------|------------------------|-------------|--------|
| I-0 | 1 | Plano, regras de acesso, contrato completo | `docs/implementation-plan.md`, `openapi.yaml`, docs atualizados | Contrato cobre todo o âmbito; diferenças registadas | `openapi-spec-validator` | ✅ Feito |
| I-1 | 2 | Estrutura, BD, migrations, seed | `backend/` (pom, app, `V1__esquema.sql`), `docker-compose.yml`, `.env.example`, seed, `frontend/` inicial | App arranca; esquema criado por Flyway; seed com ≥ 2 utilizadores e projetos com acessos distintos; 2.º arranque não duplica dados | `mvn test`; arrancar 2 vezes e contar linhas; `npm run build` | ✅ Feito (à espera de validação); ver `docs/ai-log/f03-ex02-persistencia.md` |
| I-2 | 3 | Autenticação e autorização | `identidade/` (JWT, filtro, `/auth/*`), handler de erros | Login válido → token; inválido → 401 genérico; sem token → 401; logout invalida token; 2 utilizadores não veem os projetos um do outro | Testes MockMvc; `curl` | Por fazer |
| I-3 | 4 | Projetos e membros | `projetos/` (entidades, repositórios, serviço, controlador) + ecrãs de login e projetos | CRUD de projetos com validação 1–100 / ≤ 1000; pesquisa por nome; só Gestor edita/elimina; eliminação sem órfãos. **Percurso:** login → criar projeto → ver na lista → recarregar e reiniciar o backend → continua lá | Testes de integração; percurso manual no browser | Por fazer |
| I-4 | 5 | Tarefas | `tarefas/` + detalhe do projeto (lista, filtros, formulário) | CRUD; estado e prioridade; filtros por estado, prioridade e responsável; títulos só com espaços recusados; contagens atualizadas sem recarregar | Testes de integração; manual | Por fazer |
| I-5 | 6 | Responsáveis e comentários | `tarefas/` (atribuições), `comentarios/` + detalhe da tarefa | Vários responsáveis, sem duplicados, só membros; comentários com autor e data do backend; persistem ao reabrir | Testes de integração; manual | Por fazer |
| I-6 | 7 | Frontend completo | Painel inicial, projetos, detalhe do projeto, detalhe da tarefa; componentes partilhados | Estados de carregamento, vazio, sucesso e erro; sem submissões duplicadas; dados do formulário mantidos em erro; redireciona para login quando a sessão expira | Manual no browser + testes Vitest | Por fazer |
| I-7 | 8 | Verificação | `docs/verification.md`, testes automáticos | 9 cenários registados (esperado / observado / evidência); ≥ 3 testes (validação, autorização, persistência) | `mvn test`, `npm test`, percursos manuais | Por fazer |
| I-8 | 9 | Revisão e entrega | README, AGENTS.md, OpenAPI final, refatoração | README permite arrancar de uma cópia limpa; contrato = implementação | Clone limpo + seguir o README | Por fazer |

## 6. Como trabalhamos e onde corre

- O código fica no teu repositório local (`/Users/mariohenriquepinto/SharedFolder/Matriz/matriz-api`), num branch `feat/ficha03-implementacao`.
- Compilo e testo num ambiente com Java 21, Maven, PostgreSQL e Docker. Para correres no teu Mac precisas de Java 21, Node 22 e Docker (o Maven vem pelo `mvnw`). Docker ✅ confirmado; Java 21 e Node 22 por confirmar.
- Commit no fim de cada incremento; push e PR só com o teu OK.

## 7. Pendentes

- P-33 (novos limites de descrição) por confirmar.
- Ex. 2: limites do utilizador (`nome` ≤ 100, `email` ≤ 254) por confirmar; ver `docs/ai-log/f03-ex02-persistencia.md`.
