# Ficha 05 — Exercício 6: Validação final e troubleshooting

> Ferramenta de IA usada: Claude (Claude Code), com o prompt sugerido da ficha adaptado ao prompt estruturado (B).

## Prompt

```text
# Goal
Validar a preparação da Matriz para entrega e fechar a versão 0.5.0.

# Context
main depois do PR #20: README, compose.yaml, Dockerfiles, ci.yml, sonar-project.properties, execuções da CI
e o SonarQube Cloud (quality gate a 0% de cobertura no código novo na falha controlada).

# Constraints
- Seguir o README como um utilizador novo: .env novo a partir do .env.example e volume vazio.
- Só registar resultados que foram realmente obtidos; o que não se pôde testar fica como limitação.
- Não versionar credenciais nem os ajustes do ambiente do agente (certificado do proxy).

# Task
Executar a aplicação pelo README, confirmar os containers, testar as funcionalidades essenciais, confirmar
a CI, ler os logs, procurar credenciais, reproduzir os problemas comuns do README, fazer a revisão final
(documentação, Docker, pipeline, testes) e escrever o relatório. Acrescentar a cobertura dos testes à
análise do SonarQube e alinhar as versões em 0.5.0.
```

## Alterações

| Ficheiro | O que mudou |
|----------|-------------|
| `backend/pom.xml` | Plugin JaCoCo 0.8.16 (relatório no `verify`); versão 0.5.0. |
| `frontend/package.json`, `vite.config.ts` | `@vitest/coverage-v8` 5.0.5 (mesma versão do Vitest), script `test:cobertura`, configuração da cobertura (lcov); versão 0.5.0. |
| `sonar-project.properties` | Caminhos dos relatórios de cobertura e exclusões da cobertura (testes, E2E, scripts, configuração). |
| `.github/workflows/ci.yml` | O job `qualidade` corre `./mvnw verify` e `npm run test:cobertura` antes da análise. |
| `openapi.yaml`, `e2e/package.json` | Versão 0.5.0. |
| `README.md` | Problemas comuns ajustados às mensagens reais, Playwright sem `npx`, comando da cobertura, link para o relatório, Fase 5 concluída, versão atual 0.5.0. |
| `CHANGELOG.md`, `docs/release-notes/v0.5.0.md` | 0.5.0 com data 2026-11-16, deixa de ser candidata. |
| `docs/delivery-validation.md` | Relatório de validação e relatório da entrega. |

## Verificações feitas pela IA

- Aplicação completa em Compose a partir de um `.env` novo: 3 serviços `healthy`; login, criar uma tarefa e recarregar no browser (Playwright); persistência depois de `down`/`up`; script de contrato com 76 combinações sem divergências; `openapi.yaml` validado.
- Logs sem `ERROR`/`WARN`.
- Credenciais: Trivy `secret`, histórico do Git, ambiente das imagens e ficheiros `.env` versionados.
- Os três problemas comuns do README foram provocados de propósito (porta ocupada, segredo curto, CORS sem a origem). Duas mensagens do README não correspondiam às reais e foram corrigidas.
- Testes locais: backend 107 (JaCoCo 97,4% das linhas), frontend 32 (Vitest 50,9%), lint e build.

## Revisão humana

- O Mário configurou o `SONAR_TOKEN`, o `DOCKERHUB_TOKEN` e a variável `DOCKERHUB_USERNAME`, e revê e faz merge do PR.
- A tag `v0.5.0` é criada no cloud e enviada pelo Mário a partir do Mac. É esse envio que testa a publicação no Docker Hub.

## O que a IA não pôde confirmar

- A publicação no Docker Hub (só acontece no envio da tag).
- A execução no Mac do Mário: a validação local foi no container Linux do agente, com o certificado do proxy acrescentado às builds; a CI usa os Dockerfiles do repositório sem alterações.
