# Ficha 02 — Exercício 5: Revisão crítica e Human-in-the-Loop

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).
> Artefactos revistos: `docs/requirements.md`, `docs/user-stories.md`, `docs/constraints.md`, `docs/architecture.md`, `docs/database.md`, `openapi.yaml`, `docs/ui/wireframe.png`, `README.md`, `AGENTS.md`.

## Prompt

```text
# Goal
Verificar se o Architecture Pack da Matriz é consistente com o Specification Pack.

# Context
Em anexo: todos os artefactos listados acima.

# Constraints
- Atua como revisor técnico externo.
- Procura omissões e contradições ENTRE artefactos (requisitos ↔ arquitetura ↔ dados ↔
  API ↔ wireframe), não problemas de estilo.
- Para cada problema: artefactos envolvidos, descrição, sugestão concreta.
- Não proponhas funcionalidades novas.
- Português de Portugal.

# Task
Lista os problemas encontrados, do mais grave para o menos grave.

# Validation
- Cada problema refere pelo menos dois artefactos ou um requisito concreto.
```

## Problemas encontrados e proposta de decisão

Legenda: **Aceitar** / **Alterar** (aplicar outra versão) / **Rejeitar**. A decisão final é do Mário.

| # | Artefactos | Problema | Sugestão da IA | Proposta | Porquê |
|---|-----------|----------|----------------|----------|--------|
| A-01 | database.md (RN-06, REGISTO_ADMIN) ↔ RF-02 | **Contradição:** cada mudança de administrador exige um registo com `autor_id` obrigatório, mas o primeiro administrador é criado na instalação e não tem autor. | Tornar `autor_id` opcional e acrescentar a ação `INSTALACAO`. | **Alterar** | Mais simples: escrever em RN-06 que a criação na instalação não gera registo, porque RF-19 fala só de promoções e despromoções. O modelo fica igual. |
| A-02 | openapi.yaml (`NovaTarefa.titulo`) ↔ P-24, US-06 crit. 3 | `minLength: 1` aceita um título só com espaços, que a US-06 recusa. | Acrescentar `pattern: '\S'` (pelo menos um carácter que não seja espaço). | **Aceitar** | O contrato passa a refletir a regra. |
| A-03 | openapi.yaml (`responsavelId=sem`) ↔ US-10 | O filtro "tarefas sem responsável" não está em nenhuma US nem RF. | Remover. | **Aceitar** | O AGENTS.md diz para não inventar funcionalidades; se for útil, entra no backlog primeiro. |
| A-04 | architecture.md (módulos) ↔ openapi.yaml | **Omissão:** a arquitetura tem 6 módulos, mas a API só documenta 3 operações de Tarefas. Não há visão de conjunto dos restantes endpoints (projetos, membros, progresso, notificações, administração). | Documentar todos os endpoints em OpenAPI. | **Alterar** | O enunciado pede ≥ 2 operações. Proposta: acrescentar à `description` do openapi.yaml uma lista dos endpoints previstos (sem schemas), para a Aula 03. |
| A-05 | README.md ↔ Ficha 02 | O README diz "Fase 1" e não lista `architecture.md`, `database.md`, `openapi.yaml` nem `docs/ui/`. | Atualizar estado e tabela da documentação. | **Aceitar** | É o primeiro ficheiro lido por pessoas e agentes. |
| A-06 | AGENTS.md §1 ↔ Ficha 02 | A tabela "ler antes de agir" não refere os novos artefactos (arquitetura, dados, API, UI). | Acrescentar as linhas correspondentes. | **Aceitar** | Sem isto, um agente na Aula 03 pode ignorar o modelo de dados e o contrato. |
| A-07 | constraints.md §4 (D-03, D-04, D-07) ↔ architecture.md | As decisões estão como "Proposto", mas o Mário já aprovou a arquitetura. | Marcar D-03 (relacional), D-04 (monólito modular) e D-07 (consulta periódica) como decididas. | **Aceitar** | Os documentos devem refletir as decisões tomadas. O motor da BD continua pendente. |
| A-08 | wireframe ↔ US-03 crit. 2 | A Ana é administradora, mas a lista lateral diz "Os meus projetos"; para um administrador mostra todos os projetos. | Mudar o título para "Projetos". | **Aceitar** | Pequeno e evita ambiguidade sobre o que a lista mostra. |
| A-09 | wireframe ↔ openapi.yaml | O painel de progresso do wireframe não tem operação na API. | Acrescentar `GET /projetos/{id}/progresso` ao openapi.yaml. | **Rejeitar** | O progresso pode ser calculado a partir da lista de tarefas ou entrar na lista de endpoints previstos (A-04). Não é preciso para o mínimo de 2 operações. |
| A-10 | database.md ↔ RNF-02, P-28 | As sessões (expiração após 8 h) não têm suporte no modelo. | Criar já a tabela SESSAO. | **Rejeitar** | Depende do mecanismo de autenticação (D-05); o database.md já o assinala em §5. |

## Resumo da proposta

- **Aceitar:** 6 (A-02, A-03, A-05, A-06, A-07, A-08).
- **Alterar:** 2 (A-01, A-04).
- **Rejeitar:** 2 (A-09, A-10).

## Decisão do Mário

O Mário aceitou a proposta na íntegra (2026-10-28).

### Alterações aplicadas

| # | Ficheiro(s) | Alteração |
|---|-------------|-----------|
| A-01 | database.md (RN-06) | A criação do primeiro administrador na instalação não gera registo. |
| A-02 | openapi.yaml (`NovaTarefa.titulo`) | `pattern: '\S'` para recusar títulos só com espaços. |
| A-03 | openapi.yaml (`responsavelId`) | Removido o valor especial `sem`. |
| A-04 | openapi.yaml (`info.description`) | Lista de endpoints previstos por módulo, para a Aula 03. |
| A-05 | README.md | Estado do projeto por fases e novos ficheiros na tabela da documentação. |
| A-06 | AGENTS.md §1 | Linhas para arquitetura, dados, API e UI. |
| A-07 | constraints.md (D-03, D-04, D-07), architecture.md | Decisões marcadas como tomadas. |
| A-08 | docs/ui/wireframe.html e .png | Título da lista lateral: "Projetos". |

A-09 e A-10 foram rejeitadas e não exigiram alterações.
