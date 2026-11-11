# Ficha 04 — Exercício 1: Plano de testes com IA

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Definir os testes das tarefas e das permissões a partir dos requisitos, não do comportamento atual do código.

# Context
AGENTS.md, docs/requirements.md (RF-08 a RF-12, RNF-03, RNF-06), docs/user-stories.md (US-06 a US-10,
CE-06, CE-07), openapi.yaml, testes existentes da Ficha 03.

# Constraints
- Matriz concisa: entrada, resultado esperado e nível de teste (unitário, integração, E2E).
- Incluir obrigatoriamente: título inválido, criação e consulta de tarefa, acesso sem autorização.
- Não inventar endpoints, valores nem funcionalidades não implementadas.
- Ligar cada cenário a um teste existente ou ao exercício que o vai criar.

# Task
Gerar a matriz; depois rever cada pressuposto contra os documentos e o código.

# Validation
Cada cenário tem requisito ou critério de aceitação de origem e resultado verificável.
```

## O que a IA sugeriu e o que mudou na revisão humana

A primeira versão da matriz tinha oito pressupostos errados ou fracos, corrigidos antes de gravar o plano
(detalhe em `docs/test-plan.md` §5):

1. `403` para não membros: corrigido para `404` (CE-07).
2. Estados e limites do exemplo da Ficha 03 (`TODO`, 3 a 150): corrigidos para os do contrato aprovado.
3. e 4. Testes do administrador global e das notificações: retirados, porque não estão implementados.
5. "O `TarefasTest` já prova a persistência": falso, corre numa transação desfeita no fim; o Ex. 3 cria um
   teste com commit real e limpeza (decisão do Mário, 2026-11-09).
6. Valor-limite só com `"x".repeat(200)`: acrescentado o caso dos emojis, porque `String.length()` conta
   unidades UTF-16 e o requisito conta caracteres. Fica como hipótese para o Ex. 2.
7. `data-testid` nos seletores E2E: trocado por seletores acessíveis, sem mexer no frontend.
8. Cobertura de código como critério: retirado.

Também se corrigiram três descrições que não batiam certo com os testes existentes (o estado inválido usado
é `FEITA`, a edição testada é parcial e o `422` dos responsáveis é verificado pelo `type`, não pela mensagem).

## Decisões do Mário (2026-11-09)

Aprovou todas as recomendações da análise da Ficha 04: unitários sobre `CampoTexto`, integração com commit
real, E2E com Playwright numa base de dados própria, seletores acessíveis, review do PR #11, IDOR com Ana,
Joana e Rui, Semgrep + oxlint + npm audit, CI para a Aula 05.
