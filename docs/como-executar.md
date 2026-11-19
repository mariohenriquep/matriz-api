# Como executar a Matriz

Guia completo para correr a Matriz no teu computador: configuração, base de dados, backend, frontend, Docker Compose, autenticação, testes, contrato da API, integração contínua e deploy. A apresentação do projeto está no [`README.md`](../README.md).


## Requisitos

- **Java 21** (o Maven não é preciso: o backend traz o `mvnw`, que o descarrega).
- **Node.js 22** e npm.
- **Docker** com Docker Compose, para a base de dados PostgreSQL 16, para os testes do backend e para correr a aplicação completa em containers. Só com o Docker (sem Java nem Node) já se consegue correr a aplicação: ver "Executar com Docker Compose". Em alternativa à base de dados em Docker, um PostgreSQL 16 instalado localmente (ajusta `MATRIZ_DB_URL`, `POSTGRES_USER` e `POSTGRES_PASSWORD`).
- Opcional, para verificar o contrato: Python 3 com `pyyaml` e `jsonschema`.

Resumo, a partir de uma cópia limpa (cada passo está explicado abaixo):

```bash
cp .env.example .env              # e editar: POSTGRES_PASSWORD e MATRIZ_JWT_SEGREDO
docker compose up -d postgres     # só a base de dados (tudo em Docker: ver "Executar com Docker Compose")
cd backend && ./mvnw spring-boot:run          # num terminal
cd frontend && npm install && npm run dev     # noutro terminal; abrir http://localhost:5173
```

## 1. Configuração

```bash
cp .env.example .env
```

No `.env`:

- altera `POSTGRES_PASSWORD`;
- gera o segredo dos tokens, que é obrigatório: `openssl rand -base64 48` e cola o resultado em `MATRIZ_JWT_SEGREDO`. Sem ele, o backend não arranca.

O `.env` não entra no Git; o `.env.example` só tem valores fictícios. O backend lê o `.env` da raiz do repositório.

## 2. Base de dados

```bash
docker compose up -d postgres # PostgreSQL em localhost:5433 (porta em POSTGRES_PORTA)
docker compose ps             # deve mostrar "healthy"
```

A base de dados usa a porta **5433** do computador local, para não colidir com outro PostgreSQL na 5432. Se a 5433 também estiver ocupada, muda a porta no `.env`, em `POSTGRES_PORTA` e em `MATRIZ_DB_URL`.

O esquema é criado pelo **Flyway** quando o backend arranca (scripts em `backend/src/main/resources/db/migration`). Não há passo manual.

Para apagar todos os dados e recomeçar: `docker compose down -v` (apaga o volume da base de dados).

## 3. Backend

```bash
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

A API fica em `http://localhost:8080/api/v1`. Para confirmar que arrancou e que a base de dados responde:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP"}   (inclui a ligação à base de dados)
```

## 4. Dados de demonstração

No primeiro arranque, se a base de dados não tiver utilizadores, o backend cria três contas fictícias e três projetos. Nos arranques seguintes não cria nem apaga nada. A password das três contas é o valor de `MATRIZ_SEED_PASSWORD` no `.env`.

| Conta | Gestor de | Membro de | Sem acesso a |
|-------|-----------|-----------|--------------|
| `ana@matriz.local` | Website institucional | Formação interna | App móvel |
| `rui@matriz.local` | App móvel | Website institucional | Formação interna |
| `joana@matriz.local` | Formação interna | App móvel | Website institucional |

## 5. Frontend

```bash
cd frontend
npm install
npm run dev                   # http://localhost:5173
```

O frontend chama a API em `http://localhost:8080/api/v1`. Para outro endereço, copia `frontend/.env.example` para `frontend/.env` e altera `VITE_API_URL`. O backend só aceita pedidos do browser vindos das origens em `MATRIZ_CORS_ORIGENS` (por defeito `http://localhost:5173`); por isso o Vite usa sempre a porta 5173 e recusa arrancar se estiver ocupada.

## Executar com Docker Compose

Em alternativa aos passos 2, 3 e 5, a aplicação completa (PostgreSQL, backend e frontend) corre em containers. Só precisa do Docker e do `.env` do passo 1.

```bash
docker compose up -d --build   # constrói as imagens e arranca os três serviços
docker compose ps              # os três devem ficar "healthy"
```

Abre http://localhost:8000 (porta em `MATRIZ_FRONTEND_PORTA`); a API fica em http://localhost:8080. O backend só arranca depois de a base de dados estar saudável, e o frontend depois do backend. Os dados ficam no volume `matriz-dados` e mantêm-se com `docker compose down`; `docker compose down -v` apaga-os. O frontend chama a API no endereço `VITE_API_URL`, fixado na build da imagem, e a origem `http://localhost:8000` tem de estar em `MATRIZ_CORS_ORIGENS`.

| Para… | Comando |
|-------|---------|
| Validar o `compose.yaml` e o `.env` | `docker compose config -q` |
| Ver o estado e os healthchecks | `docker compose ps` |
| Ver os logs | `docker compose logs --tail=50` (ou `docker compose logs -f backend`) |
| Reconstruir depois de mudar código | `docker compose up -d --build` |
| Parar (mantém os dados) | `docker compose down` |

Problemas comuns:

- **Porta ocupada** (`port is already allocated` ou `address already in use`): muda `POSTGRES_PORTA`, `MATRIZ_PORTA` ou `MATRIZ_FRONTEND_PORTA` no `.env`. Não corras o backend com `./mvnw spring-boot:run` e em Docker ao mesmo tempo: os dois usam a 8080.
- **O backend não fica saudável** (fica em `Restarting`): confirma em `docker compose logs backend` que `MATRIZ_JWT_SEGREDO` tem pelo menos 32 caracteres e que `POSTGRES_PASSWORD` é a mesma com que o volume foi criado (se mudaste a password, `docker compose down -v` recria a base de dados e apaga os dados).
- **O login no frontend falha com erro de rede:** falta `http://localhost:8000` em `MATRIZ_CORS_ORIGENS`, ou o `VITE_API_URL` da imagem não corresponde ao endereço da API (reconstrói com `--build`).

## Sessão e autenticação

1. O login (`POST /api/v1/auth/login`) devolve um token JWT, válido durante **8 horas a partir do login**, com ou sem atividade.
2. O frontend guarda o token no `sessionStorage`: a sessão sobrevive a recarregar a página e termina quando o separador fecha.
3. Todos os pedidos à API, exceto o registo e o login, levam `Authorization: Bearer <token>`. Sem token, ou com um token inválido ou expirado, a API responde `401` e o frontend volta ao login com a mensagem "A tua sessão terminou".
4. "Terminar sessão" (`POST /api/v1/auth/logout`) invalida no servidor todos os tokens desse utilizador, incluindo os de outros separadores.
5. As passwords são guardadas só como hash BCrypt.
6. **Registo público (0.8.0):** "Criar conta" na página inicial (`POST /api/v1/auth/registo`). A conta nova fica no plano gratuito: não cria projetos e só vê os projetos para que um Gestor a adiciona. A resposta é sempre a mesma, exista ou não uma conta com o email. Para dar o plano Profissional a uma conta (ainda sem pagamentos): `UPDATE utilizador SET plano = 'PROFISSIONAL' WHERE email = '...';`.
7. **Limites de pedidos por IP:** 10 logins por minuto e 5 registos por hora (e 50 registos por hora no total). Acima disso a API responde `429`. Configuráveis com `MATRIZ_LIMITE_LOGIN`, `MATRIZ_LIMITE_REGISTO` e `MATRIZ_LIMITE_REGISTO_TOTAL`; atrás de proxies, `MATRIZ_PROXIES_CONFIAVEIS` diz quantos acrescentam o IP ao `X-Forwarded-For` (2 no Render).

Exemplo com `curl`:

```bash
TOKEN=$(curl -s -H 'Content-Type: application/json' \
  -d '{"email":"ana@matriz.local","password":"<MATRIZ_SEED_PASSWORD>"}' \
  http://localhost:8080/api/v1/auth/login | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/auth/eu
curl -X POST -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/auth/logout
```

## Testes e verificações

```bash
cd backend && ./mvnw test     # precisa do Docker: os testes usam um PostgreSQL temporário (Testcontainers);
                              # inclui ArquiteturaTest, que verifica as dependências entre camadas
cd backend && ./mvnw test -Dtest=CampoTextoTest   # só os testes unitários da validação de texto (sem Docker)
cd backend && ./mvnw test -Dtest=CriarEConsultarTarefaTest   # integração: criar e consultar com commit real e limpeza
cd frontend && npm test       # testes de componentes e do cliente da API (Vitest + Testing Library, sem backend)
cd frontend && npm run test:cobertura   # os mesmos testes, com relatório de cobertura em frontend/coverage/
cd e2e && npm ci --ignore-scripts && ./node_modules/.bin/playwright install chromium   # só da primeira vez
cd e2e && npm test            # E2E: arranca backend (8081) e frontend (5174) sobre a BD matriz_e2e, criada de novo;
                              # precisa do PostgreSQL do docker compose ligado (docker compose up -d postgres)
cd frontend && npm run build && npm run lint
```

Com o backend a correr sobre os dados de demonstração, o script de contrato percorre todas as operações do `openapi.yaml` e confirma os códigos e os corpos das respostas:

```bash
pip install pyyaml jsonschema
MATRIZ_SEED_PASSWORD=<valor do .env> python3 scripts/verificar-contrato.py
```

Os resultados da última verificação, com os 9 cenários da Ficha 03, estão em [`docs/verification.md`](verification.md).

## Documentação da API

O contrato da API está em [`openapi.yaml`](../openapi.yaml) (OpenAPI 3.1): todas as operações, esquemas, códigos de resposta e erros. Pode ser lido em qualquer visualizador de OpenAPI, por exemplo a extensão OpenAPI do VS Code ou do IntelliJ, ou colando o ficheiro em https://editor.swagger.io. A API não serve uma página Swagger própria, para não acrescentar dependências; o script `scripts/verificar-contrato.py` confirma que a implementação corresponde ao contrato.

## Integração contínua

Cada pull request e cada push para o `main` corre a pipeline [`.github/workflows/ci.yml`](../.github/workflows/ci.yml) no GitHub Actions (separador *Actions* do repositório):

| Job | O que verifica |
|-----|----------------|
| Backend | `./mvnw verify`: testes unitários, de arquitetura e de integração |
| Frontend | lint, testes e build |
| E2E | o teste Playwright, com um PostgreSQL do Compose |
| Segurança | Trivy: vulnerabilidades HIGH/CRITICAL com correção nas dependências, configuração e segredos |
| Qualidade | testes do backend e do frontend com cobertura (JaCoCo e Vitest) e SonarQube Cloud (quality gate no PR) |
| Imagens Docker | só depois dos anteriores: build das imagens, Trivy nas imagens e arranque da aplicação com os healthchecks; em tags de versão, publicação no Docker Hub |

Num push de uma tag de versão (`vX.Y.Z`), a mesma pipeline corre de ponta a ponta e, se tudo passar, publica no Docker Hub as imagens que o Trivy analisou: `marrike/matriz-backend` e `marrike/matriz-frontend`, com as tags da versão (por exemplo `0.5.0`) e `latest`. A publicação falha se a tag não corresponder à versão do `pom.xml`, do `openapi.yaml` e dos `package.json`. Precisa da variável `DOCKERHUB_USERNAME` e do secret `DOCKERHUB_TOKEN` nas definições do repositório (*Settings → Secrets and variables → Actions*). A imagem publicada do frontend chama a API em `http://localhost:8080/api/v1`; para outro endereço é preciso construir a imagem com outro `VITE_API_URL`.

A pipeline falha se algum teste falhar, e as imagens só se constroem depois de as verificações passarem. Usa só permissão de leitura do repositório; as credenciais de teste são geradas em cada execução. Exceções aceites do Trivy, com justificação, em [`.trivyignore`](../.trivyignore).

## Deploy no Render

A aplicação pode ser publicada no plano gratuito do Render a partir do Blueprint [`render.yaml`](../render.yaml): PostgreSQL gratuito, backend a partir da imagem do Docker Hub e frontend como site estático. Passos, limites do plano gratuito (o backend adormece, a base de dados expira ao fim de 30 dias) e atualizações em [`docs/deploy-render.md`](deploy-render.md).

