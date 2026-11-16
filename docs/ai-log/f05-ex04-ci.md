# Ficha 05 — Exercício 4: Integração contínua com GitHub Actions

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B). O Mário pediu para juntar o
> Trivy e o SonarQube Cloud à pipeline (Desafio A da ficha, antecipado para este exercício).

## Prompt

```text
# Goal
Criar a pipeline de CI da Matriz em .github/workflows/ci.yml.

# Context
Comandos de teste e build do backend (./mvnw verify), do frontend (lint, test, build) e do E2E (Playwright);
compose.yaml e Dockerfiles do Ex. 2 e 3. Pedido do Mário: incluir Trivy e SonarQube Cloud.

# Constraints
- Correr em push e pull_request; falhar quando um teste obrigatório falha.
- A construção Docker só depois das verificações; permissões mínimas; nenhum segredo exposto.

# Task
Escrever o workflow, corrigir o que a primeira execução mostrar e fazer a falha controlada.
```

## Pipeline

| Job | Conteúdo | Depende de |
|-----|----------|-----------|
| `backend` | `./mvnw -B verify` (107 testes, Testcontainers) | — |
| `frontend` | `npm ci --ignore-scripts`, lint, 32 testes, build | — |
| `e2e` | PostgreSQL do Compose + Playwright (versão do `package-lock.json`) | — |
| `seguranca` | Trivy no repositório: dependências, Dockerfiles, compose, segredos (HIGH/CRITICAL com correção) | — |
| `qualidade` | SonarQube Cloud; o `SONAR_TOKEN` só chega aos passos que precisam dele | — |
| `docker` | `compose config`, `compose build`, Trivy nas duas imagens, `up --wait`, health, logs, `down -v` | `backend`, `frontend`, `e2e`, `seguranca` |

`permissions: contents: read`; actions fixadas por SHA; passwords e segredo JWT gerados na hora e mascarados.

## Problemas encontrados e correções

| # | Onde | Problema | Correção |
|---|------|----------|----------|
| 1 | Trivy (local, antes do push) | Tomcat 10.1.55 (5 CRITICAL, 1 HIGH), Jackson 2.21.4 (5 HIGH), driver PostgreSQL 42.7.11 (1 HIGH) no Spring Boot 3.5.16 | Versões fixadas no `pom.xml` (10.1.61, 2.21.8, 42.7.14), aprovadas pelo Mário |
| 2 | Trivy | 2 CRITICAL no `spring-webmvc` só corrigidas no Spring 7 (XsltView, SSE) | `.trivyignore` com justificação: a Matriz não usa estas partes |
| 3 | Trivy | `nginx-unprivileged:1.29` com 57 vulnerabilidades no Alpine | 1.30 + `apk upgrade` nas imagens finais; `USER nginx` explícito |
| 4 | CI, 1.ª execução | `matriz-frontend` "unhealthy": `localhost` resolve para `::1` e o nginx só escuta em IPv4 | Healthchecks em `127.0.0.1` |
| 5 | SonarQube, quality gate | "Security Rating C": `npm ci` sem `--ignore-scripts`, `npx`, versão não fixada | `--ignore-scripts` na CI e no Dockerfile; Playwright de `node_modules/.bin`; `npm test` |
| 6 | SonarQube | `SONAR_TOKEN` visível em todos os passos do job | Token só no passo que verifica se existe e no da análise |
| 7 | SonarQube | Avisos: `sonar.sources`/`sonar.tests` ignorados no novo âmbito de análise | `sonar-project.properties` só com exclusões |

## Evidência

| Execução | Resultado |
|----------|-----------|
| [43855997127](https://github.com/mariohenriquep/matriz-api/actions/runs/43855997127) (c87e4b9) | Todos os jobs verdes; SonarQube: quality gate aprovado, 0 problemas novos |
| [43856714001](https://github.com/mariohenriquep/matriz-api/actions/runs/43856714001) (1801cc3, **falha controlada**) | `frontend` falhou (1 de 32 testes); `docker` **skipped**; os outros jobs verdes |
| Execução do revert (9a9772f) e seguintes | Verde de novo (ver o separador Checks do PR #19) |

O Mário guardou capturas de ecrã das execuções como evidência para a entrega.

## Revisão humana

- O Mário pediu o Trivy e o SonarQube, configurou o projeto no SonarQube Cloud e o secret `SONAR_TOKEN`, e trouxe da interface do SonarQube os textos dos problemas (o sonarcloud.io não é acessível a partir do ambiente da IA).
- A primeira hipótese da IA para o "Security Rating C" (o token no job inteiro) era um problema real mas não era a causa do chumbo; a causa só ficou clara com o texto dos problemas.
- Limitação: o quality gate do SonarQube exige 80% de cobertura no código novo e a pipeline ainda não envia relatórios de cobertura (JaCoCo, Vitest). Na falha controlada o gate chumbou por isso. Fica proposto para o Ex. 6.
