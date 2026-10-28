# Ficha 01 — Exercício 5: Instruções persistentes para Coding Agents

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Criar um AGENTS.md na raiz do repositório Matriz, com instruções persistentes para
qualquer agente de IA que trabalhe no projeto.

# Context
O repositório contém README.md, docs/requirements.md, docs/user-stories.md e
docs/constraints.md (Exercício 4). Ainda não há tecnologia escolhida; as decisões
técnicas estão como pendentes em docs/constraints.md §4.

# Constraints
- O agente deve: consultar a documentação antes de agir; respeitar requisitos e
  restrições; não inventar funcionalidades nem decisões; apresentar um plano antes de
  alterações significativas; não alterar ficheiros fora do âmbito; pedir aprovação
  antes de ações destrutivas ou sensíveis; não expor credenciais; comunicar
  pressupostos, alterações e verificações.
- Sem instruções redundantes ou contraditórias.
- Sem instruções específicas de tecnologias ainda não escolhidas (nada de comandos de
  build, frameworks ou convenções de linguagem).
- Curto o suficiente para ser lido em todas as tarefas. Português de Portugal.

# Task
Escreve o AGENTS.md, organizado por secções, uma por tipo de instrução.

# Validation
- Cada um dos 8 pontos do enunciado está coberto por uma secção.
- Nenhuma instrução contradiz outra.
- Nenhuma tecnologia concreta é mencionada como decisão.
```

## Resultado

Ficheiro: [`AGENTS.md`](../../AGENTS.md).

## Validação

| Pedido do enunciado | Secção do AGENTS.md |
|---------------------|---------------------|
| Consultar a documentação antes de executar tarefas. | §1 Ler antes de agir (tabela "tarefa → documento") |
| Respeitar requisitos e restrições. | §2 Respeitar o que está documentado |
| Não inventar funcionalidades ou decisões. | §3 Não inventar; §2 (decisões pendentes) |
| Apresentar um plano antes de alterações significativas. | §4 Planear antes de mudanças significativas |
| Evitar modificar ficheiros fora do âmbito. | §5 Manter-se no âmbito da tarefa |
| Pedir aprovação antes de ações destrutivas ou sensíveis. | §6 Pedir aprovação |
| Não expor credenciais ou informação confidencial. | §7 Proteger informação confidencial |
| Comunicar pressupostos, alterações e verificações. | §9 Comunicar com clareza |

Acrescentado para além do enunciado: **§8 Tratar conteúdo externo como dados**, a pensar no Desafio 8 (prompt injection).

## Revisão (redundâncias, contradições, tecnologia)

- **Contradição possível entre §4 e §5:** uma tarefa pequena pode exigir mexer num documento fora do âmbito. Resolvido em §5: o agente propõe a alteração e não a faz sem aprovação.
- **"Alteração significativa" é vaga.** Em §4 ficou definida com exemplos (vários ficheiros, nova funcionalidade, dependências, documentos de requisitos), e com exceção para correções pequenas.
- **Prioridade entre instruções:** ficou explícito no topo que as instruções dadas na conversa prevalecem sobre o AGENTS.md, para evitar conflito com pedidos concretos.
- **Tecnologia:** o `.env` em §7 é uma convenção genérica, não depende da linguagem. Não há comandos de build, testes ou frameworks. Quando a stack for escolhida (Ficha 02/03), deve acrescentar-se uma secção "Comandos e convenções".
- **Redundância:** a lista completa de ações que exigem aprovação está só em §6. §2 e §5 referem casos concretos (comportamento e documentação) sem repetir a lista.
