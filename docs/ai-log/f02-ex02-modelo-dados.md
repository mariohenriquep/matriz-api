# Ficha 02 — Exercício 2: Modelação de Dados e Diagrama ER

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Representar os dados essenciais do MVP da Matriz num modelo e num diagrama ER.

# Context
Em anexo: docs/requirements.md (RF-01 a RF-19), docs/user-stories.md (critérios e
casos-limite), docs/constraints.md (pressupostos) e docs/architecture.md (base de
dados relacional, módulos).

# Constraints
- Só as entidades necessárias ao MVP; nada do "fora do âmbito".
- Para cada atributo: tipo lógico (sem tipos de um motor concreto) e regras.
- Assinala regras de negócio que o modelo não garante sozinho e pressupostos novos.
- Diagrama em Mermaid (erDiagram), coerente com as tabelas.
- Português de Portugal.

# Task
1. Identifica entidades, atributos, identificadores e relacionamentos.
2. Define cardinalidades.
3. Lista regras de negócio e pressupostos por confirmar.
4. Gera o erDiagram.

# Validation
- Cada RF que guarda dados tem uma entidade/atributo correspondente.
- Cada entidade do diagrama aparece nas tabelas, com os mesmos atributos.
- Valores derivados (progresso, "Atrasada") não são guardados.
```

## Resultado

[`docs/database.md`](../database.md): 6 entidades (UTILIZADOR, PROJETO, MEMBRO_PROJETO, TAREFA, NOTIFICACAO, REGISTO_ADMIN), cardinalidades, 9 regras de negócio (RN-01 a RN-09) e o diagrama ER.

## Validação

| Critério | Resultado |
|----------|-----------|
| RF com dados cobertos | ✅ Contas e bloqueio (RF-01, RNF-11) → UTILIZADOR; projetos e arquivo (RF-03 a RF-05, RF-18) → PROJETO; membros (RF-06, RF-07) → MEMBRO_PROJETO; tarefas (RF-08 a RF-12) → TAREFA; notificações (RF-16) → NOTIFICACAO; administradores (RF-02, RF-13, RF-19) → UTILIZADOR.is_admin_global + REGISTO_ADMIN. |
| Diagrama = tabelas | ✅ Mesmas 6 entidades e atributos. |
| Diagrama válido | ✅ Renderizado com Mermaid 10.9.1 sem erros. |
| Derivados não guardados | ✅ Progresso e "Atrasada" são calculados. |

## Alternativas consideradas e rejeitadas

- **Tabela de papéis (PAPEL / UTILIZADOR_PAPEL).** Rejeitada: só há três papéis fixos; Gestor = `PROJETO.gestor_id`, Administrador = `is_admin_global`, Membro = MEMBRO_PROJETO. Uma tabela genérica de papéis seria uma funcionalidade não pedida.
- **Guardar o texto das notificações.** Rejeitado: gera-se a partir do tipo e das ligações, e não fica desatualizado se o nome da tarefa mudar.
- **Apagado lógico de tarefas.** Rejeitado no MVP: o histórico está fora do âmbito (P-32, por confirmar).
