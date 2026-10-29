# Ficha 03 — Exercício 3: Implementar o acesso à aplicação

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Identificar o utilizador e aplicar as regras de acesso no backend (incremento I-2).

# Context
openapi.yaml (/auth/login, /auth/eu, /auth/logout; erros 401/403/404), docs/implementation-plan.md §3
(regras de acesso), docs/database.md (UTILIZADOR.versao_token). Decisões do Mário: JWT bearer com
validade de 8 h fixas a partir do login; token no sessionStorage; CORS no backend em vez do proxy
do Vite.

# Constraints
- Passwords só como hash (BCrypt). Credenciais e segredos fora do repositório.
- A identidade vem sempre do token, nunca de campos enviados pelo cliente.
- Login falhado com mensagem genérica (não revelar se o email existe).
- Quem não é membro de um projeto recebe 404; membro sem o papel recebe 403.
- Sem bloqueio após tentativas falhadas (RNF-11 é extra).

# Task
1. Spring Security sem estado, validação de JWT e os três endpoints de autenticação.
2. Logout que invalida os tokens já emitidos.
3. Componente central de controlo de acesso para os exercícios seguintes.
4. CORS limitado às origens do .env.
5. Frontend: cliente da API, ecrã de login, envio para o login sem sessão e aviso de sessão terminada.

# Validation
- Testes: credenciais válidas e inválidas, sem token, token adulterado, expirado, de outro segredo,
  depois do logout; dois utilizadores diferentes; CORS.
- curl contra a aplicação a correr; percurso no browser.
```

## Resultado

| Ficheiro | Conteúdo |
|----------|----------|
| `identidade/ConfiguracaoSeguranca.java` | Spring Security sem sessão; só o login e o `/actuator/health` são públicos; validação de JWT (HS256, emissor, validade, versão do token); CORS; 401 no formato do contrato |
| `identidade/ServicoTokens.java` | Emissão do JWT com `sub` (id), `ver` (versão do token) e validade de 8 h. O segredo é obrigatório e tem pelo menos 32 caracteres |
| `identidade/ValidadorVersaoToken.java` | Recusa tokens anteriores a um logout ou de utilizadores que já não existem |
| `identidade/ServicoAutenticacao.java`, `ControladorAutenticacao.java` | `/auth/login`, `/auth/eu`, `/auth/logout` |
| `identidade/UtilizadorAtual.java` | Única forma de saber quem faz o pedido (a partir do token) |
| `projetos/ControloAcesso.java` | `exigirMembro` (404), `exigirGestor` (403/404), `exigirMembroDaTarefa` (404) |
| `comum/ErroApi.java`, `TratamentoErros.java`, `Problemas.java` | Erros Problem Details em português; campos desconhecidos no JSON dão 400 |
| `frontend/src/api/`, `frontend/src/auth/`, `frontend/src/paginas/` | Cliente da API, sessão no `sessionStorage`, rota protegida, ecrã de login e painel provisório |
| `openapi.yaml` (0.2.1), `README.md` | Ciclo do token documentado; exemplos com `curl` |
| `docs/requirements.md` (RNF-02), `docs/constraints.md` (P-28) | Sessão de 8 h a partir do login, em vez de "8 h de inatividade" |

## Validação

| Verificação | Resultado |
|-------------|-----------|
| `mvn test` | ✅ 25 testes, 0 falhas (14 de autenticação, 5 de controlo de acesso, 6 de esquema e seed) |
| Login válido | ✅ token e `expiraEm` 8 h depois |
| Password errada e email inexistente | ✅ mesma resposta 401 "Email ou password incorretos" |
| Sem token, token adulterado, expirado, assinado com outro segredo | ✅ 401 em todos |
| Logout | ✅ invalida todos os tokens da pessoa; os de outros utilizadores continuam válidos |
| Dois utilizadores | ✅ `/auth/eu` devolve cada um; Ana recebe 404 no projeto "App móvel" (onde não é membro) e 403 ao gerir "Formação interna" (onde é só membro) |
| CORS | ✅ `http://localhost:5173` aceite; outra origem recusada (403) |
| Backend sem `MATRIZ_JWT_SEGREDO` | ✅ não arranca, com mensagem explícita |
| `curl` contra a aplicação a correr | ✅ health, 401 sem token, login, `/auth/eu`, logout e 401 depois do logout |
| Browser (Playwright) | ✅ sem sessão → login; password errada → erro e email mantido; login → painel; recarregar → mantém a sessão; token inválido → login com "A tua sessão terminou"; terminar sessão → login, `sessionStorage` vazio |
| `npm run build`, `npm run lint` | ✅ |

## Correção durante a implementação

O primeiro teste de CORS falhou: o pedido de uma origem permitida recebia 403. O Spring Security só usa a configuração de CORS se o bean se chamar `corsConfigurationSource`, e eu tinha-lhe dado outro nome. Corrigido e confirmado pelo teste, que agora verifica também a origem recusada.

## Decisões tomadas neste exercício (a validar)

- **Biblioteca oficial do Spring para o JWT** (`spring-boot-starter-oauth2-resource-server`), em vez de código próprio de assinatura.
- **`MATRIZ_JWT_SEGREDO` vazio no `.env.example`**, de propósito: obriga cada instalação a gerar o seu segredo, em vez de todas usarem o mesmo valor de exemplo.
- **Login com email inexistente demora o mesmo** que com password errada (compara com um hash fictício), para não se descobrir que contas existem pelo tempo de resposta.
- **`/actuator/health` público mas sem detalhes**: sem sessão só mostra `{"status":"UP"}`.
- **O ecrã de login passou do Exercício 4 para este**, porque a ficha pede no Exercício 3 que a interface encaminhe para o login e avise quando a sessão termina.
- **Ao abrir a aplicação com sessão guardada**, o frontend confirma o token com `/auth/eu`; se tiver havido logout noutro separador, volta ao login.
- **Vite fixo na porta 5173** (`strictPort`): se mudasse sozinho de porta, o CORS recusaria os pedidos sem um erro claro.
- **O teste por HTTP de "a Ana não vê os projetos do Rui"** fica para o Exercício 4, quando existirem os endpoints de projetos. Agora está coberto pelo teste do `ControloAcesso`.
