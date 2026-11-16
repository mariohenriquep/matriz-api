# Validação final da entrega (Ficha 05, Ex. 6)

Verificação global da Matriz 0.5.0 antes da tag `v0.5.0`, feita a 2026-11-16 sobre o `main` depois do PR #20 (`5105c24`), com as alterações do Ex. 6 (cobertura e versões). Segue as tarefas do Ex. 6 e termina com o relatório pedido na entrega da ficha.

## 1. Resumo

| Requisito da entrega | Estado | Onde |
|----------------------|--------|------|
| Código-fonte atualizado | ✅ | `main`, PRs #17 a #21 |
| Dockerfiles e `.dockerignore` | ✅ | `backend/`, `frontend/` |
| Docker Compose | ✅ | `compose.yaml` |
| `.env.example` sem credenciais reais | ✅ | §6 |
| Workflow GitHub Actions | ✅ | `.github/workflows/ci.yml` |
| README e documentação técnica | ✅ | `README.md`, `docs/architecture.md` §8, `openapi.yaml` |
| Changelog e notas da versão | ✅ | `CHANGELOG.md`, `docs/release-notes/v0.5.0.md` |
| Evidência dos testes e verificações | ✅ | §3 e §4, `docs/ai-log/f05-*` |

Não há problemas críticos por resolver. As limitações estão em §9.

## 2. Ambiente da validação

- **Local:** container Linux do agente de IA (Docker 29.9, Docker Compose 5.6), a partir de um `.env` novo copiado do `.env.example`, com a password da base de dados e o `MATRIZ_JWT_SEGREDO` gerados com `openssl rand`, e um volume vazio.
- **Diferença em relação ao Mac:** neste ambiente a rede passa por um proxy com certificado próprio, por isso as imagens foram construídas com cópias dos Dockerfiles que só acrescentam esse certificado (não estão no repositório). `docker compose build` é substituído por `docker build` com as mesmas tags `:local`, seguido de `docker compose up -d --no-build`. A CI do GitHub usa os Dockerfiles do repositório sem alterações.
- **CI:** GitHub Actions, execuções na §4.

## 3. Execução a partir do README

| # | Tarefa do Ex. 6 | Comando ou ação | Resultado |
|---|-----------------|-----------------|-----------|
| 1 | Configuração | `cp .env.example .env`, preencher `POSTGRES_PASSWORD` e `MATRIZ_JWT_SEGREDO` | `.env` ignorado pelo Git (`git check-ignore`) |
| 1 | Validar o Compose | `docker compose config -q` | Sem erros |
| 1 | Arrancar | `docker compose up -d` (com as imagens já construídas) | Os 3 serviços `healthy` em 28 s, pela ordem postgres → backend → frontend |
| 2 | Estado dos containers | `docker compose ps` | `matriz-postgres` 5433, `matriz-backend` 8080, `matriz-frontend` 8000, todos `healthy` |
| 2 | Utilizadores | `docker exec <container> id -un` | backend `matriz`, frontend `nginx` (o processo do PostgreSQL corre como `postgres`, a imagem oficial só arranca como root) |
| 3 | API | `GET /actuator/health` | `{"status":"UP"}` |
| 3 | Frontend | `GET http://localhost:8000/` | `<title>Matriz</title>`, cabeçalhos `X-Content-Type-Options: nosniff` e `X-Frame-Options: DENY` |
| 3 | Autenticação | `GET /api/v1/projetos` sem token | `401` |
| 3 | CORS | preflight com `Origin: http://localhost:8000` | `200` |
| 3 | Funcionalidades no browser (Playwright) | login com `ana@matriz.local`, abrir "Website institucional", criar uma tarefa, recarregar | Tarefa visível, 0 erros na consola |
| 3 | Persistência | `docker compose down`, `docker compose up -d`, abrir de novo o projeto | A tarefa criada continua lá |
| 3 | Contrato da API | `python3 scripts/verificar-contrato.py` sobre a aplicação em Compose | 76 combinações operação/código, sem divergências |
| 3 | OpenAPI | `openapi-spec-validator` sobre `openapi.yaml` | Válido (OpenAPI 3.1) |
| 5 | Logs | `docker compose logs`, à procura de `ERROR`/`WARN` | Nenhum, mesmo depois do script de contrato (os erros de validação dão 4xx sem registo de erro) |
| — | Limpeza | `docker compose down -v` | Volume apagado |

**Problemas comuns do README, reproduzidos:**

| Situação provocada | O que acontece | Resolução no README |
|--------------------|----------------|---------------------|
| Porta 8000 ocupada por outro processo | `failed to bind host port 0.0.0.0:8000/tcp: address already in use` | Mudar a porta no `.env`. Mensagem acrescentada ao README. |
| `MATRIZ_JWT_SEGREDO=curto` | O backend fica em `Restarting`; o log diz "MATRIZ_JWT_SEGREDO tem de estar definida no .env, com pelo menos 32 caracteres." | Corrigir o segredo. `Restarting` acrescentado ao README. |
| `MATRIZ_CORS_ORIGENS` sem `http://localhost:8000` | Preflight do login a partir de `:8000` responde `403` (no browser aparece como erro de rede) | Acrescentar a origem. |

## 4. Testes e pipeline

| Verificação | Resultado |
|-------------|-----------|
| Backend `./mvnw verify` (unitários, `ArquiteturaTest`, integração com PostgreSQL 16 em Testcontainers) | 107 testes, 0 falhas. Cobertura JaCoCo: 97,4% das linhas, 89,5% dos ramos |
| Frontend `npm run lint`, `npm test`, `npm run build` | Lint sem avisos, 32 testes, build OK. Cobertura Vitest: 50,9% das linhas |
| E2E Playwright | Verde na CI (job E2E) |
| Trivy no repositório (dependências, configuração, segredos) | 0 HIGH/CRITICAL com correção; 2 exceções justificadas em `.trivyignore` |
| Trivy nas imagens | 0 HIGH/CRITICAL com correção |
| SonarQube Cloud | Quality gate aprovado, 0 problemas novos, 0 security hotspots |

Execuções da CI:

| Execução | O quê | Resultado |
|----------|-------|-----------|
| [43855997127](https://github.com/mariohenriquep/matriz-api/actions/runs/43855997127) | PR #19, pipeline completa | ✅ todos os jobs |
| [43856714001](https://github.com/mariohenriquep/matriz-api/actions/runs/43856714001) | Falha controlada (teste do frontend partido) | ❌ Frontend; Imagens Docker não correu |
| [43858652001](https://github.com/mariohenriquep/matriz-api/actions/runs/43858652001) | PR #20 com os passos de publicação | ✅; publicação saltada fora das tags |
| [43859938190](https://github.com/mariohenriquep/matriz-api/actions/runs/43859938190) | `main` depois do PR #20 | ✅ todos os jobs |
| PR #21 | Ex. 6: cobertura no SonarQube, versão 0.5.0 | ver o PR |

## 5. Logs

Os logs do arranque, das operações no browser e do script de contrato não têm erros nem avisos. Na CI, o passo "Logs dos serviços" mostra as últimas 50 linhas de cada serviço em todas as execuções, incluindo as que falham. Nos testes do backend aparece um `ERROR ... Erro inesperado`: é esperado, vem do teste que confirma que um erro interno devolve 500 sem expor detalhes.

## 6. Credenciais nos ficheiros versionados

| Verificação | Resultado |
|-------------|-----------|
| Ficheiros `.env` no Git (`git ls-files`) | Só `.env.example` e `frontend/.env.example` |
| Trivy `secret` no repositório (local e na CI) | Nenhum segredo encontrado |
| Histórico do Git: padrões de tokens (GitHub, Docker Hub, SonarQube, AWS, chaves privadas) | Nenhuma ocorrência |
| Variáveis de ambiente das imagens (`docker image inspect`) | Sem passwords, segredos ou tokens |
| Dockerfiles e `compose.yaml` | Só referências a variáveis (`${POSTGRES_PASSWORD}`); a password é obrigatória (`:?`) e o segredo JWT é validado no arranque |
| CI | Credenciais de teste geradas em cada execução e mascaradas; `SONAR_TOKEN` e `DOCKERHUB_TOKEN` são secrets do GitHub e só chegam aos passos que os usam |

Valores conhecidos que aparecem de propósito: a password das contas de demonstração (`MATRIZ_SEED_PASSWORD` no `.env.example`, só criadas com `MATRIZ_SEED_ATIVO=true`) e os segredos fictícios dos testes (`segredo-so-para-testes-...`). Nenhum dá acesso a uma instalação real.

## 7. Problemas encontrados e correções

| # | Ex. | Problema | Correção |
|---|-----|----------|----------|
| 1 | 4 | Trivy: Tomcat, Jackson e driver PostgreSQL do Spring Boot 3.5.16 com vulnerabilidades HIGH/CRITICAL | Versões corrigidas fixadas no `pom.xml` |
| 2 | 4 | Trivy: 2 CRITICAL no `spring-webmvc` só corrigidas no Spring 7 | `.trivyignore` com justificação (XsltView e SSE não são usados) |
| 3 | 4 | Trivy: `nginx-unprivileged:1.29` com 57 vulnerabilidades no Alpine | nginx 1.30 e `apk upgrade` nas imagens finais |
| 4 | 4 | CI: frontend `unhealthy`, porque `localhost` resolve para `::1` e o nginx só escuta em IPv4 | Healthchecks em `127.0.0.1` |
| 5 | 4 | SonarQube "Security Rating C": `npm ci` sem `--ignore-scripts`, `npx`, versão não fixada | `--ignore-scripts`; Playwright de `node_modules/.bin` |
| 6 | 4 | `SONAR_TOKEN` visível em todo o job | Token só nos passos que o usam |
| 7 | 4 | SonarQube: avisos de propriedades sem efeito | `sonar-project.properties` simplificado |
| 8 | 4 | SonarQube chumbou a falha controlada por 0% de cobertura no código novo: a CI não enviava cobertura | Ex. 6: JaCoCo e Vitest no job `qualidade` |
| 9 | 6 | README: mensagem de porta ocupada diferente da real; backend com segredo inválido fica em `Restarting`, não só "não saudável" | Texto corrigido |
| 10 | 6 | README ainda mandava instalar o Playwright com `npx` | `./node_modules/.bin/playwright` |

Problemas só do ambiente do agente (sem impacto no repositório): certificado do proxy nas builds, limite de pedidos do Docker Hub (espelho `mirror.gcr.io`), `dl-cdn.alpinelinux.org` e `sonarcloud.io` bloqueados, e o envio de tags a partir do cloud recusado (o Mário envia a tag do Mac).

## 8. Revisão final pelo agente de IA

Revisão pedida no Ex. 6, feita sobre a documentação, a configuração Docker, a pipeline e os resultados dos testes:

- **Documentação:** o README chega para arrancar a aplicação do zero, como mostra a §3. Os comandos de desenvolvimento, Compose, testes e CI correspondem aos ficheiros. Corrigidos os pontos 9 e 10 da §7.
- **Docker:** builds multi-stage, utilizadores sem privilégios, healthchecks nos três serviços, sem credenciais nas imagens, `.dockerignore` a excluir `.env`, `target/` e `node_modules/`. Imagem do backend com 407 MB (JRE 21 Alpine), do frontend com 92 MB.
- **Compose:** rede própria, volume persistente, arranque ordenado por `service_healthy`, `restart: unless-stopped`, todas as portas configuráveis.
- **Pipeline:** permissões só de leitura, actions fixadas por SHA, cancelamento de execuções antigas, imagens só depois das verificações e publicação só em tags de versão, depois do Trivy e do arranque.
- **Testes:** backend bem coberto (97% das linhas). O frontend fica nos 51%: os ecrãs principais têm testes de componentes, mas várias páginas só são exercitadas pelo E2E, que não conta para a cobertura.

## 9. Limitações e pendentes

- A publicação no Docker Hub só é testada a sério com o envio da tag `v0.5.0`.
- O `VITE_API_URL` fica fixado na imagem do frontend: a imagem publicada chama a API em `http://localhost:8080/api/v1`.
- Cobertura do frontend em 51% das linhas. O quality gate exige 80% só no código novo de cada PR.
- 2 vulnerabilidades do `spring-webmvc` aceites até à migração para o Spring Boot 4.
- Sem logs estruturados nem métricas além de `/actuator/health` (desafio B não feito).
- Sem limites de memória e CPU no `compose.yaml`; o backend usa `-XX:MaxRAMPercentage=75`.
- Continuam registados os pontos aceites na Ficha 04: SEC-03, SEC-04 e CR-04 ([`docs/quality-security-review.md`](quality-security-review.md)).
- Pendentes do MVP para depois das fichas: Kanban e arquivo de projetos.

## 10. Relatório da entrega

1. **Alterações implementadas:**
   - Plano de entrega.
   - Dockerfiles do backend e do frontend, com `.dockerignore`.
   - `compose.yaml` com os três serviços, healthchecks e volume persistente; `.env.example` atualizado.
   - Pipeline de CI com testes, E2E, Trivy, SonarQube com cobertura, build e arranque das imagens.
   - Publicação das imagens no Docker Hub em tags de versão.
   - README, arquitetura com diagramas Mermaid, CHANGELOG e notas da versão 0.5.0.
   - Este relatório.
2. **Resultados dos testes e da pipeline:**
   - 107 testes no backend e 32 no frontend, mais o E2E, todos verdes.
   - Contrato da API sem divergências.
   - CI verde no `main`, e a falha controlada confirmou que a pipeline para (§4).
3. **Problemas e soluções:** §7.
4. **Utilização dos agentes de IA:**
   - O agente analisou o projeto, propôs o plano e as decisões, que o Mário aprovou.
   - Escreveu os ficheiros, correu as validações locais e acompanhou a CI.
   - Corrigiu o que o Trivy e o SonarQube encontraram e fez esta revisão final.
   - O Mário criou e guardou os tokens (SonarQube, Docker Hub) e reviu e fez merge de cada PR. Também indicou os problemas do SonarQube que o agente não conseguia ver.
   - Registo por exercício em `docs/ai-log/f05-ex01` a `f05-ex06`.
5. **Limitações e pendentes:** §9.
