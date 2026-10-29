# Ficha 03 — Exercício 2: Preparar a aplicação e a persistência

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Criar a estrutura executável da Matriz e a persistência, seguindo o incremento I-1 do plano.

# Context
docs/implementation-plan.md (stack, estrutura, §4 alterações ao modelo), docs/database.md,
docs/architecture.md. Stack: Java 21 + Spring Boot 3 (Maven), PostgreSQL 16 + Flyway,
React + Vite + TypeScript. O Mário tem Docker no Mac.

# Constraints
- Credenciais só em variáveis de ambiente; .env fora do Git; .env.example com valores fictícios.
- Esquema só com o âmbito da Ficha 03: utilizadores, projetos, membros, tarefas, atribuições
  e comentários. Os extras ficam para migrations próprias.
- O seed não pode apagar nem duplicar dados quando a aplicação reinicia.
- Ainda sem endpoints de negócio nem autenticação (Exercício 3).

# Task
1. Criar backend/ (pom, mvnw, configuração), docker-compose.yml e .env.example.
2. Criar a migration V1 a partir de docs/database.md.
3. Criar o seed com pelo menos dois utilizadores e projetos com acessos distintos.
4. Criar frontend/ inicial com Vite.
5. Registar os comandos no README.

# Validation
- Testes contra um PostgreSQL real (Testcontainers): tabelas criadas, seed, acessos distintos,
  segundo arranque sem duplicados.
- Arrancar o backend duas vezes contra o Docker Compose e contar as linhas.
- npm run build e npm run lint sem erros.
```

## Resultado

| Ficheiro | Conteúdo |
|----------|----------|
| `backend/pom.xml`, `mvnw`, `.mvn/` | Spring Boot 3.5.16: Web, Validation, Data JPA, Actuator, Flyway, driver PostgreSQL, BCrypt (`spring-security-crypto`); testes com Testcontainers |
| `backend/src/main/resources/application.yml` | Ligação à BD por variáveis de ambiente; lê o `.env` da raiz; `ddl-auto: validate` (o Hibernate nunca altera o esquema) |
| `backend/.../db/migration/V1__esquema_inicial.sql` | 6 tabelas, chaves compostas, `ON DELETE CASCADE` (RN-10), `CHECK` de estados, prioridades e textos só com espaços, email único sem distinguir maiúsculas |
| `backend/.../comum/DadosDemonstracao.java` | Seed: 3 contas, 3 projetos com acessos cruzados, 6 tarefas, 2 comentários. Só corre se não houver utilizadores |
| `backend/src/test/.../EsquemaESeedTest.java` | 6 testes de esquema e seed |
| `docker-compose.yml`, `.env.example` | PostgreSQL 16 com volume persistente e healthcheck |
| `frontend/` | Vite 8 + React 19 + TypeScript, página provisória, proxy de `/api` para o backend |
| `README.md` | Secção "Como executar" com todos os comandos |

## Validação

| Verificação | Resultado |
|-------------|-----------|
| `mvn test` (PostgreSQL 16 em Testcontainers) | ✅ 6 testes, 0 falhas |
| 1.º arranque com `./mvnw spring-boot:run` contra o Docker Compose | ✅ Flyway aplicou V1; seed criou 3 utilizadores, 3 projetos, 6 tarefas, 2 comentários; `/actuator/health` com `db: UP` |
| 2.º arranque | ✅ "Schema is up to date"; "Seed ignorado"; contagens iguais |
| Password das contas | ✅ guardada como hash BCrypt (verificado no teste), nunca em claro |
| `.env` ignorado pelo Git, `.env.example` incluído | ✅ `git check-ignore` |
| `npm run build`, `npm run lint` | ✅ sem erros nem avisos |

## Decisões tomadas neste exercício (a validar)

- **Seed por código Java em vez de SQL na migration.** Uma migration corre sempre, também em produção; o seed fica separado e pode ser desligado (`MATRIZ_SEED_ATIVO=false`). A password das contas vem do `.env` (`MATRIZ_SEED_PASSWORD`) para não haver passwords no código.
- **Spring Security completo só no Exercício 3.** Agora entra apenas o BCrypt, para o seed poder gravar hashes. Sem isto, o Spring Security fecharia todos os endpoints com uma password gerada antes de existir login.
- **Actuator `/actuator/health`** para confirmar que a aplicação e a base de dados respondem. No Exercício 3 decide-se se fica público.
- **Proxy do Vite para `/api`** em vez de configurar CORS no backend: em desenvolvimento, o frontend e a API parecem a mesma origem.
- **Limites que não estavam documentados:** `nome` do utilizador até 100 caracteres e `email` até 254 (máximo habitual de um endereço de email). Propostos aqui; não mudam o contrato.
- **Colunas dos extras não criadas** (`is_admin_global`, `tentativas_falhadas`, `bloqueado_ate`, `arquivado_em`, NOTIFICACAO, REGISTO_ADMIN), como previsto no plano §4.

## Problemas no ambiente de desenvolvimento

O Maven Central e o Docker Hub responderam "429 Too Many Requests" ao ambiente onde a IA trabalhou. Usou-se o espelho da Google para o Maven e o `mirror.gcr.io` para a imagem do PostgreSQL, **só nesse ambiente** (`~/.m2/settings.xml`, fora do repositório). O projeto continua a usar os repositórios oficiais.
