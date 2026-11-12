# Ficha 05 — Exercício 1: Análise e preparação do projeto

> Ferramenta de IA usada: Claude (Claude Code), com o prompt sugerido da ficha adaptado ao prompt estruturado (B).

## Prompt

```text
# Goal
Analisa integralmente o projeto Matriz e propõe um plano para o preparar para entrega com Docker,
Docker Compose e GitHub Actions.

# Context
Repositório na tag v0.4.0: backend/ (Spring Boot), frontend/ (Vite), e2e/, docker-compose.yml, .env.example,
docs/. Ficha PL 05 e decisões das fichas anteriores (CORS direto, porta 5433, seed desligado por omissão).

# Constraints
- Respeitar a arquitetura e as tecnologias existentes; não introduzir componentes desnecessários.
- Não alterar ficheiros antes de o plano ser revisto.
- Não incluir credenciais em nenhum ficheiro versionado.

# Task
Identificar componentes, dependências, versões, comandos, serviços externos, variáveis de ambiente e testes;
propor a estratégia, os ficheiros a criar ou alterar e as etapas.
```

## O que a IA fez

- Leu o `pom.xml`, o `package.json` do frontend e do `e2e`, o `application.yml`, o `docker-compose.yml`, o `.env.example` e a configuração do Playwright.
- Confirmou que não há Dockerfiles nem CI, e que o frontend fixa o endereço da API na build (`VITE_API_URL`).
- Escreveu a análise com 11 decisões (D1 a D11) para o Mário e, depois, o plano em [`docs/delivery-plan.md`](../delivery-plan.md).

## Revisão humana

- Conflito encontrado: a ficha pede `compose.yaml` e o repositório tem `docker-compose.yml`. Proposto renomear.
- A IA propôs nginx só porque o frontend precisa de um servidor de ficheiros estáticos; não propôs proxy reverso para manter a decisão de CORS da Ficha 03.
- Decisões revistas e aprovadas pelo Mário antes da implementação.
