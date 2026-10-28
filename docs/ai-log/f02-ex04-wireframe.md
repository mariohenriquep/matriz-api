# Ficha 02 — Exercício 4: Prototipagem de Interface

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B). O wireframe foi gerado como HTML de baixa fidelidade e exportado para PNG.

## Prompt

```text
# Goal
Criar um wireframe de baixa fidelidade do ecrã principal da Matriz: o painel de um projeto.

# Context
Em anexo: docs/user-stories.md (US-01 a US-16), docs/requirements.md (RF-01 a RF-19)
e openapi.yaml (listar tarefas, criar tarefa, alterar estado).

# Constraints
- Baixa fidelidade: tons de cinzento, sem cores de marca nem imagens.
- Só elementos que correspondem a funcionalidades do MVP; nada do "fora do âmbito"
  (sem comentários, anexos, etiquetas, sprints).
- Cada elemento relevante tem uma anotação a indicar a User Story.
- Dados de exemplo coerentes com os critérios de aceitação (ex.: 3 / 3 / 4 → 40%).
- Textos em português de Portugal.

# Task
Gera o wireframe em HTML e exporta-o para docs/ui/wireframe.png.

# Validation
- Cada elemento do ecrã liga-se a uma US.
- Os números do painel de progresso batem certo com as tarefas mostradas.
- Nenhum elemento fora do âmbito.
```

## Resultado

- [`docs/ui/wireframe.png`](../ui/wireframe.png): imagem do wireframe (1280 px).
- [`docs/ui/wireframe.html`](../ui/wireframe.html): fonte editável.

Ecrã representado: **painel do projeto "Website 2027"**, visto pela Ana (administradora global), com navegação, notificações, lista de projetos, painel de progresso, filtros e quadro Kanban. As anotações numeradas (1 a 10) ligam cada elemento à sua User Story.

## Validação da navegação e dos elementos

| Elemento | US / RF | Presente |
|----------|---------|----------|
| Navegação "Projetos" e lista lateral "Os meus projetos" | US-03, RF-05 | ✅ |
| "Projetos arquivados" à parte | US-03 crit. 2, RF-05 | ✅ |
| "+ Criar projeto" | US-02 | ✅ |
| "As minhas tarefas" | US-15 | ✅ |
| "Administração" (só admin) | US-11, RF-13, RF-19 | ✅ |
| Contador de notificações | US-14 | ✅ |
| Membros / Editar / Arquivar (só Gestor ou admin) | US-04, US-05, US-16 | ✅ |
| Painel de progresso 3 / 3 / 4 e 40% | US-12 crit. 1 | ✅ números coerentes (10 tarefas) |
| Alternar Quadro / Lista | US-10, US-13 | ✅ |
| Filtros por estado e responsável | US-10 | ✅ |
| Quadro com 3 colunas e cartões | US-13 | ✅ |
| Cartão "Atrasada" | P-27, CE-06 | ✅ |
| Tarefa "Sem responsável" | US-08 crit. 3 | ✅ |
| "+ Nova tarefa" | US-06 | ✅ |
| Elementos fora do âmbito (comentários, anexos, etiquetas, sprints) | — | ✅ nenhum |

## Limitações

- Só um ecrã. Login, formulário de nova tarefa, lista de notificações e administração ficam para quando forem implementados (Aula 03).
- A forma exata de mover cartões (arrastar ou menu) continua por decidir (ver Ex. 3 da Ficha 01).
