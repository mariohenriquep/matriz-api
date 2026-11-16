# Extras do MVP: quadro Kanban e arquivo de projetos

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Acrescentar à Matriz os extras do MVP: quadro Kanban (RF-15, US-13) e arquivo de projetos (RF-18, US-16).

# Context
main na versão 0.5.0 (fichas 01 a 05 concluídas). Backend em clean architecture (ArquiteturaTest), contrato em
openapi.yaml, regras RN-04 e CE-05 em docs/database.md e docs/user-stories.md. A Ficha 03 aceitou eliminar projetos.

# Constraints
- O arquivo convive com a eliminação: arquivar não apaga nada e o Gestor continua a poder eliminar.
- Arquivado = só leitura (409), sem mudar as regras de acesso já existentes (404 para quem não é membro, 403 por papel).
- Kanban sem dependências novas (arrastar nativo do HTML5) e com alternativa para teclado e ecrãs táteis.
- Testes para cada regra nova (backend, frontend e um E2E).

# Task
Migração, endpoints, regras no ControloAcesso, contrato, separadores Ativos/Arquivados, aviso no projeto e
na tarefa, quadro com três colunas e mudança de estado otimista (volta atrás se a API recusar).
```

## Decisões

| Decisão | Porquê |
|---------|--------|
| `POST /arquivar` e `/reativar`, idempotentes | Ações explícitas, fáceis de autorizar só ao Gestor; repetir não é erro (arquivar de novo mantém a data). |
| 409 `/erros/projeto-arquivado` nas escritas | É um conflito com o estado atual do recurso, não falta de permissão; o 404/403 continuam a ter prioridade. |
| `GET /projetos` só com ativos por omissão | O painel e o resumo mostram o trabalho em curso; o arquivo tem o seu separador. |
| Quadro como vista por omissão, lista mantida | A lista tem a edição rápida e os filtros por estado; no quadro o filtro de estado não faz sentido. |
| Mudança otimista no quadro | O cartão muda logo de coluna; se a API recusar volta atrás e mostra o erro. |
| Administrador global fora | Ainda não existe no código (RF-02/RF-13 adiados); US-16 fica só com o Gestor. |

## Verificação

- Backend: `./mvnw verify` com todos os testes verdes, incluindo os 9 de `ArquivoProjetosTest`.
- Frontend: `npm test` (48 testes), `npm run build` e `oxlint` sem avisos.
- E2E: `npm test` em `e2e/` com os 2 testes verdes; o novo arrasta um cartão no Chromium real.

## Quadro geral na página inicial (0.7.0)

Pedido do Mário depois da 0.6.0: "todas as tarefas de todos os projetos num kanban geral na primeira página, sem ser necessário abrir um projeto".

| Decisão | Porquê |
|---------|--------|
| Endpoint novo `GET /tarefas` em vez de um pedido por projeto | Um só pedido, com os filtros feitos na base de dados; escala com o número de projetos. |
| Só projetos ativos | Os arquivados são só de leitura; no quadro geral não se poderiam arrastar. |
| `projetoId` de outro projeto dá lista vazia, não 404 | É um filtro sobre "as minhas tarefas", não o acesso a um recurso; não revela se o projeto existe. |
| Separador "Quadro geral" ao lado de Ativos e Arquivados | A página inicial continua a abrir nos projetos (nada muda para quem já a usa) e o quadro fica a um clique, com URL próprio. |
| Nome do projeto vem da lista de projetos já pedida pelo painel | O contrato da `Tarefa` não muda. |
| Filtro "Só as minhas" | Cobre o RF-17 ("As minhas tarefas") sem uma página nova. |

Verificação: `./mvnw verify`, `npm test` e `oxlint` no frontend, e os 3 testes E2E verdes (o novo arrasta um cartão no quadro geral).
