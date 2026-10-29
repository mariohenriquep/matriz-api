# Ficha 03 — Exercício 1: Consolidar o plano de implementação

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Transformar a documentação da Matriz num plano de implementação executável e coerente.

# Context
Repositório com AGENTS.md, README.md, docs/requirements.md, docs/user-stories.md,
docs/constraints.md, docs/architecture.md, docs/database.md, openapi.yaml e docs/ui/.
Âmbito da Ficha 03: autenticação, projetos (CRUD + pesquisa), tarefas (CRUD, estado,
prioridade, filtros), vários responsáveis, comentários e painel inicial.
Decisões do Mário: Java 21 + Spring Boot 3 + PostgreSQL + React/Vite/TS; todo o âmbito
da Ficha 03 primeiro, extras depois; aceitar os conflitos da ficha; seed em vez de
registo; manter os nossos valores de campos; JWT 8 h.

# Constraints
- Compara o MVP com o âmbito da ficha e regista cada diferença com a decisão tomada.
- Distingue decisões confirmadas de propostas.
- Respeita a arquitetura (monólito modular) e o modelo de dados aprovados; atualiza-os
  só onde a ficha obriga.
- Não implementes ainda.

# Task
1. Lista lacunas, contradições e decisões pendentes.
2. Consolida as regras de acesso.
3. Completa o contrato OpenAPI para todas as operações necessárias.
4. Propõe incrementos com componentes, dependências, critérios de aceitação e verificações.

# Validation
- O contrato passa no validador OpenAPI 3.1.
- Cada área do âmbito da Ficha 03 tem pelo menos uma operação e um incremento.
- Os documentos anteriores ficam coerentes com as alterações.
```

## Resultado

- [`docs/implementation-plan.md`](../implementation-plan.md): conciliação (C-01 a C-07), stack, estrutura de pastas, regras de acesso, alterações ao modelo e 9 incrementos (I-0 a I-8).
- [`openapi.yaml`](../../openapi.yaml) v0.2.0: **20 operações** (autenticação, projetos, membros, tarefas, responsáveis, comentários).
- Documentos atualizados: `requirements.md` (RF-01, RF-05, RF-10, RF-12, novos RF-20 e RF-21), `user-stories.md` (US-03, US-08, US-10, novas US-17 e US-18), `database.md` (TAREFA_RESPONSAVEL, COMENTARIO, `versao_token`, RN-10), `constraints.md` (P-07, P-10, P-33, D-01 a D-05), `architecture.md` (stack).

## Validação

| Área da Ficha 03 | Operações | Incremento |
|------------------|-----------|-----------|
| Autenticação | `POST /auth/login`, `GET /auth/eu`, `POST /auth/logout` | I-2 |
| Projetos | `GET/POST /projetos`, `GET/PATCH/DELETE /projetos/{id}` (+ pesquisa) | I-3 |
| Tarefas | `GET/POST /projetos/{id}/tarefas`, `GET/PATCH/DELETE /tarefas/{id}` | I-4 |
| Responsáveis | `GET /projetos/{id}/membros` (elegíveis), `POST/DELETE /tarefas/{id}/responsaveis…` | I-5 |
| Comentários | `GET/POST /tarefas/{id}/comentarios` | I-5 |
| Pesquisa e filtros | `?pesquisa=`, `?estado=&prioridade=&responsavelId=` | I-3, I-4 |
| Painel inicial | `GET /projetos` com `contagens` por estado | I-3, I-6 |

- `openapi-spec-validator`: ✅ válido (20 operações).
- Diagrama ER atualizado: ✅ renderizado com Mermaid sem erros.

## Correção encontrada durante a revisão

Na Ficha 01 (Exercício 6), a inserção do RF-19 partiu a linha do RF-18 em `requirements.md` (a prioridade e a justificação do RF-18 ficaram no fim da linha do RF-19). Corrigido agora.

## Decisões de desenho (propostas)

- **Painel inicial sem endpoint próprio:** `GET /projetos` já devolve as contagens por estado de cada projeto.
- **Logout real com JWT:** em vez de apenas apagar o token no browser, o servidor incrementa `versao_token`, e os tokens antigos deixam de ser aceites.
- **`PATCH /tarefas/{id}` genérico** substitui `PATCH /tarefas/{id}/estado`, porque a ficha pede edição de estado e prioridade juntas.
- **Gestão de membros incluída**, embora a ficha não a liste, porque sem ela não há "utilizadores elegíveis" nem projetos partilhados.
