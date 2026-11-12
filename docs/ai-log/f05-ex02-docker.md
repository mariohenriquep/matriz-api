# Ficha 05 — Exercício 2: Containerização com Docker

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Criar as imagens Docker do backend e do frontend da Matriz.

# Context
docs/delivery-plan.md (Ex. 1), backend/pom.xml (Java 21, Spring Boot 3.5.16, Maven Wrapper),
frontend/package.json (Vite 8, VITE_API_URL), application.yml (configuração por variáveis de ambiente).

# Constraints
- Imagens multi-stage, base com versão fixa, utilizador sem privilégios.
- Nenhuma credencial nos Dockerfiles nem nas imagens; .dockerignore em cada contexto.
- O comportamento da aplicação não muda.

# Task
Dockerfile e .dockerignore para backend/ e frontend/; construir, executar e validar.
```

## Ficheiros

| Ficheiro | O que faz |
|----------|-----------|
| `backend/Dockerfile` | Build com `eclipse-temurin:21-jdk-alpine` e `./mvnw` (camada própria para as dependências); execução em `eclipse-temurin:21-jre-alpine` como utilizador `matriz`; healthcheck em `/actuator/health`. |
| `backend/.dockerignore` | Deixa de fora `target/`, IDE e `.env`. |
| `frontend/Dockerfile` | Build com `node:22-alpine` e `npm ci`; `VITE_API_URL` como build arg; execução em `nginxinc/nginx-unprivileged:1.30-alpine` (utilizador `nginx`, porta 8080). |
| `frontend/nginx.conf` | Fallback para `index.html` (rotas do React Router), cache longa para `/assets/`, cabeçalhos `nosniff`, `DENY` e `Referrer-Policy`. |
| `frontend/.dockerignore` | Deixa de fora `node_modules/`, `dist/` e `.env`. |

## Validação (2026-11-12, no ambiente cloud)

Comandos: `docker build`, `docker images`, `docker run` numa rede própria com um `postgres:16-alpine`, `docker ps`, `docker logs`.

| Verificação | Resultado |
|-------------|-----------|
| `docker images` | `matriz-backend` 130 MB, `matriz-frontend` 23 MB (conteúdo) |
| `docker ps` | backend e frontend `Up (healthy)` |
| Utilizador | backend `uid=100(matriz)`, frontend `uid=101(nginx)` |
| `GET /actuator/health` | `{"status":"UP"}` (inclui a base de dados) |
| `POST /api/v1/auth/login` (conta de demonstração) | 200 |
| `HEAD /projetos/1` no frontend | 200 (fallback para `index.html`) |
| CORS a partir da origem do frontend | `Access-Control-Allow-Origin` correto |
| `docker logs` do backend | Flyway aplicou as migrations; `Started MatrizApplication`; sem `WARN` nem `ERROR` |
| `docker history` | só `ENV MATRIZ_PORTA=8080`; nenhum segredo |

O segredo JWT e a password da base de dados foram gerados na hora (`openssl rand`) e passados com `-e` no `docker run`.

## Revisão humana e problemas encontrados

- No cloud o Docker Hub e o Maven Central passam por um proxy com certificado próprio. Para construir aqui usou-se uma cópia do Dockerfile, fora do repositório, com esse certificado e um espelho do Maven. O Dockerfile versionado não tem nada disto e a CI usa os registos normais.
- Os testes não correm no `docker build` (`-DskipTests`): correm antes, na CI (Ex. 4), e a imagem só se constrói depois deles.
- O `VITE_API_URL` fica fixado na imagem do frontend. Para outro endereço da API, constrói-se a imagem com outro build arg.
