# Ficha 04 — Exercício 4: Teste End-to-End

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Validar pela interface o percurso: autenticar → abrir projeto → criar tarefa → ver a tarefa na lista.

# Context
docs/test-plan.md (T-17), frontend React (Login, Inicio, ProjetoDetalhe, Tarefas, FormularioTarefa),
decisões D-4 (pasta e2e/, base de dados matriz_e2e) e D-5 (seletores acessíveis, sem mexer no frontend).

# Constraints
- Playwright; não inventar seletores: usar só rótulos, papéis e nomes que existem no código.
- Dados de teste isolados e repetíveis; sem esperas temporizadas fixas.
- Screenshots e traces só para diagnosticar falhas.
- Não alterar código de produção sem autorização.

# Task
Montar o projeto Playwright, automatizar o fluxo e executá-lo duas vezes.

# Validation
cd e2e && npx playwright test (x2); a base de dados de desenvolvimento fica intacta.
```

## Resultado

- `e2e/` com `@playwright/test` 1.63.0, configuração que arranca o backend (8081) e o frontend (5174) e
  `preparar-bd.mjs`, que recria a base de dados `matriz_e2e` antes de cada execução.
- O teste passou nas duas execuções (19 s e 9 s). A base de dados de desenvolvimento ficou com 0 tabelas.
- Nenhuma alteração ao frontend: os rótulos e os papéis que já existiam chegaram para todos os seletores.

## Revisão humana das propostas da IA

- Usar portas próprias (8081/5174) em vez das de desenvolvimento: assim o teste não reaproveita um backend
  já ligado à base de dados de desenvolvimento (`reuseExistingServer: false`) e pode correr ao mesmo tempo.
- A verificação "a tarefa aparece na lista" foi complementada com um `reload`, para provar que a tarefa
  vem do backend e não só do estado da página.
- Limitação: o teste depende do Docker (PostgreSQL do `docker compose`) e, no Mac, de instalar uma vez o
  Chromium do Playwright (`npx playwright install chromium`). Foi executado no ambiente cloud; no Mac do
  Mário ainda não foi executado.
