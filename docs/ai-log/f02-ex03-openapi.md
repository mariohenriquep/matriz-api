# Ficha 02 — Exercício 3: API Design com OpenAPI

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Documentar operações REST relevantes do MVP da Matriz num contrato OpenAPI 3.1.

# Context
Em anexo: docs/user-stories.md, docs/database.md (entidades e regras RN-01 a RN-09)
e docs/architecture.md (REST + JSON, erros em Problem Details, autenticação por decidir).

# Constraints
- Pelo menos duas operações, escolhidas pelo valor para o MVP.
- Nomes de campos coerentes com docs/database.md.
- Para cada operação: método, endpoint, parâmetros, schemas de pedido e resposta,
  principais status codes, ligando cada erro a um critério de aceitação ou caso-limite.
- Não fixar tecnologia; a autenticação é provisória (D-05).
- YAML válido em OpenAPI 3.1. Descrições em português de Portugal.

# Task
1. Escolhe e justifica as operações.
2. Escreve openapi.yaml com schemas reutilizáveis em components.
3. Inclui exemplos de pedido e de erro.

# Validation
- O ficheiro passa num validador OpenAPI 3.1.
- Cada status code de erro tem origem num critério (US-xx) ou caso-limite (CE-xx).
```

## Operações escolhidas

| Operação | Porquê | US / RF |
|----------|--------|---------|
| `GET /projetos/{projetoId}/tarefas` | É a operação mais usada: alimenta a lista, o quadro Kanban e os filtros. | US-10, US-13, RF-12 |
| `POST /projetos/{projetoId}/tarefas` | Núcleo de "gerir tarefas", com as regras mais ricas (validação, responsável membro, projeto arquivado, notificação). | US-06, RF-08 |
| `PATCH /tarefas/{tarefaId}/estado` | É o que faz avançar o progresso e o que o Kanban usa ao mover um cartão. | US-09, US-13, RF-11 |

## Status codes e origem

| Código | Quando | Origem |
|--------|--------|--------|
| 200 / 201 | Sucesso | — |
| 400 | Título vazio ou > 200, estado inexistente, campo desconhecido | US-06 crit. 3, US-09 crit. 3, P-24 |
| 401 | Sem sessão | RNF-02, US-01 crit. 5 |
| 404 | Projeto/tarefa não existe **ou** sem acesso (mesma resposta) | CE-07, US-06 crit. 4 |
| 409 | Projeto arquivado | RN-04, US-16 crit. 2 |
| 422 | Responsável não é membro do projeto | RN-02, US-08 crit. 2 |

## Validação

| Critério | Resultado |
|----------|-----------|
| ≥ 2 operações | ✅ 3 operações |
| OpenAPI 3.1 válido | ✅ `openapi-spec-validator` sem erros |
| Erros com origem | ✅ tabela acima |
| Nomes iguais a `docs/database.md` | ✅ (em camelCase na API: `responsavelId`, `dataLimite`…) |

## Decisões e alternativas rejeitadas

- **`PATCH /tarefas/{id}/estado` em vez de `PATCH /tarefas/{id}` genérico.** Uma operação só para o estado é mais simples de validar e corresponde exatamente ao que o Kanban faz.
- **404 em vez de 403 para quem não tem acesso**, para não revelar que o projeto existe (CE-07).
- **409 para projeto arquivado e 422 para regra de negócio**, para o frontend distinguir "o projeto está bloqueado" de "os dados violam uma regra".
- **Sem paginação no MVP:** o volume previsto (≤ 500 tarefas por projeto, RNF-08) não a exige. A resposta já vem num objeto `{ itens, total }` para se poder acrescentar paginação mais tarde sem quebrar o contrato.
- **`atrasada` vem calculado na resposta** (P-27), para o frontend não repetir a regra.
