# Ficha 02 — Exercício 1: Definição da Arquitetura

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Escolher e justificar uma arquitetura adequada ao MVP da Matriz.

# Context
Em anexo: docs/requirements.md, docs/user-stories.md, docs/constraints.md e AGENTS.md.
MVP com RF-01 a RF-19; até ~50 utilizadores e ~100 projetos (P-01); equipa de uma pessoa;
notificações in-app sem exigência de tempo real.

# Constraints
- Atua como arquiteto de software.
- Compara brevemente monólito, monólito modular e microservices, tendo em conta só o MVP.
- Não escolhas linguagem nem frameworks (D-01, D-02 pendentes).
- Distingue decisões propostas de pressupostos novos.
- Português de Portugal.

# Task
1. Lista os fatores dos requisitos que pesam na decisão.
2. Compara as três alternativas numa tabela.
3. Escolhe uma e justifica, com as consequências aceites.
4. Identifica os componentes, as suas responsabilidades e os requisitos que cobrem.
5. Indica as dependências entre componentes.

# Validation
- Todos os RF do MVP estão cobertos por pelo menos um componente.
- Cada fator citado vem dos documentos (com ID).
- Nenhuma tecnologia concreta é apresentada como decidida.
```

## Resultado

[`docs/architecture.md`](../architecture.md): **monólito modular** + SPA, com 6 módulos (Identidade e acesso, Projetos e membros, Tarefas, Progresso, Notificações, Administração).

## Validação

| Critério | Resultado |
|----------|-----------|
| Todos os RF cobertos | ✅ RF-01 a RF-19 aparecem na coluna "Requisitos" dos componentes. |
| Fatores com origem | ✅ Tabela §1 com IDs. |
| Sem tecnologia decidida | ✅ Linguagem, frameworks e motor de BD ficam pendentes. |

## Alternativas consideradas e rejeitadas

- **WebSockets para as notificações.** Rejeitado: não há requisito de tempo real e acrescenta complexidade. Fica a consulta periódica (P-30).
- **Guardar as contagens de progresso numa tabela própria.** Rejeitado: o progresso é calculado a partir das tarefas, para nunca ficar desatualizado. O volume de dados é pequeno (P-01).

Pressupostos novos: P-30 (consulta a cada 30 s) e P-31 (mesmo domínio).
