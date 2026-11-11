# Code Review e Security Review da Matriz

> Ficha PL 04, Exercício 5. Revisão feita a 2026-11-11 sobre o `main` 330a0c5 (depois dos PR #12 a #14), com a aplicação a correr localmente sobre uma base de dados nova (seed de demonstração). A revisão foi feita com IA (Claude Code, no papel de Senior Software Engineer e Application Security Reviewer). Cada conclusão foi verificada contra o código e, quando possível, contra a aplicação em execução. **Nenhuma correção foi aplicada:** as propostas da §5 esperam pela decisão do Mário.

## 1. Âmbito e método

| Parte | Âmbito | Como |
|-------|--------|------|
| Code Review | Último incremento da Ficha 03: diff do PR #11 (`117c523..88f81dd`, Ex. 9: tratamento de erros, `CampoTexto`, filtros no URL, mensagem ao eliminar projeto) | Leitura do diff à procura de bugs, validações em falta, tratamento de erros e testes em falta; cada suspeita reproduzida contra a API |
| Security Review | Todas as regras de acesso a projetos, tarefas, membros, responsáveis e comentários; autenticação e tokens | Leitura de `ControloAcesso`, dos serviços, da configuração de segurança e dos repositórios; 29 pedidos `curl` com três contas de demonstração (§3) |
| Análise estática | Todo o repositório | Semgrep, oxlint e `npm audit` (§4) |

**Legenda.** *Estado:* **Confirmado** (reproduzido ou verificado no código e na aplicação), **Hipótese** (plausível pela leitura, mas não demonstrado), **Refutado** (suspeita testada que não se verificou). *Severidade:* Alta, Média, Baixa, Info.

## 2. Code Review do PR #11

| ID | Ficheiro | Evidência | Impacto | Severidade | Estado | Correção proposta e teste de regressão |
|----|----------|-----------|---------|------------|--------|----------------------------------------|
| CR-01 | `services/CampoTexto.java` | O tamanho era medido com `String.length()` (unidades UTF-16): 200 emojis davam "tem no máximo 200 caracteres", embora o PostgreSQL os aceite (`docs/test-plan.md` §6.1) | Títulos, nomes e comentários válidos recusados | Média | Confirmado, **já corrigido** no PR #12 | Contar code points. Regressão: `CampoTextoTest.duzentosEmojisSaoDuzentosCaracteres`, `TarefasTest.validacaoDoTituloEDaDescricao`, `texto.test.ts` |
| CR-02 | `services/ServicoProjetos.java:37` | O limite da pesquisa (100) é verificado **antes** de retirar os espaços e em unidades UTF-16. `GET /projetos?pesquisa=` com 95 letras e 10 espaços à volta dá `400`, embora a pesquisa limpa tenha 95 caracteres | Uma pesquisa válida colada com espaços é recusada | Baixa | Confirmado (pedido real) | Limpar primeiro com `CampoTexto.limpo` e contar code points. Regressão: caso novo em `ProjetosTest` (95 letras com espaços à volta → `200`) |
| CR-03 | `frontend/src/api/tarefas.ts` (`filtrosDoUrl`) | O comentário diz que valores desconhecidos no URL são ignorados, mas `responsavelId` aceita qualquer texto. Com `?responsavelId=abc` a API responde `400` "Valor inválido para responsavelId" e a lista mostra um erro; "Tentar novamente" repete o mesmo erro | Um link editado à mão ou antigo abre a página com erro em vez de abrir sem filtro | Baixa | Confirmado na API; na interface pela leitura de `Tarefas.tsx` (estado `erro`) | Aceitar só ids numéricos (`/^\d+$/`), como já se faz com o estado e a prioridade. Regressão: caso novo em `calculos.test.ts` |
| CR-04 | `exceptions/TratamentoErros.java` (`erroInesperado`) | O ramo que mantém os 4xx do Spring MVC (`ErrorResponse`) devolve o tipo `/erros/pedido-invalido`, que não está no `openapi.yaml`, e não tem teste. Não encontrei nenhuma rota atual que lá chegue | Nenhum observado | Info | Hipótese (código possivelmente inalcançável) | Sem correção. Fica registado para quando houver um parâmetro obrigatório ou cabeçalho exigido |

Verificado sem problemas no mesmo diff: a mensagem "Projeto … eliminado." passa pelo `state` do histórico e é mostrada como texto (o React escapa-a; só a própria aplicação pode pôr `state`); o `erroInesperado` não revela detalhes internos (coberto por `TratamentoErrosTest.erroInesperadoDa500SemDetalhesInternos`); a refatoração para `CampoTexto` manteve as mensagens e os testes anteriores verdes.

## 3. Security Review: acesso a recursos (IDOR / Broken Access Control)

### 3.1 Cenário com duas (e três) contas de demonstração

Contas do seed: **Ana**, Gestora do *Website institucional* (projeto 1, tarefa 2 criada por ela); **Joana**, sem acesso ao projeto 1; **Rui**, membro do projeto 1 mas não Gestor. Pedidos feitos com `curl` contra `http://localhost:8080/api/v1`, cada um com o token da conta indicada.

```text
== Joana (sem acesso) contra recursos da Ana ==
GET    /projetos/1                          -> 404 "Projeto não encontrado"
GET    /projetos/999999                     -> 404 "Projeto não encontrado"   (igual: não revela que o 1 existe)
PATCH  /projetos/1 {"nome":"invadido"}      -> 404
DELETE /projetos/1                          -> 404
GET    /projetos/1/membros                  -> 404
POST   /projetos/1/membros {"email":"joana@…"} -> 404   (não se consegue juntar a si própria)
GET    /projetos/1/tarefas                  -> 404
POST   /projetos/1/tarefas {"titulo":"intrusa"} -> 404
GET    /tarefas/2                           -> 404 "Tarefa não encontrada"
GET    /tarefas/999999                      -> 404 "Tarefa não encontrada"    (igual)
PATCH  /tarefas/2 {"estado":"CONCLUIDA"}    -> 404
DELETE /tarefas/2                           -> 404
GET    /tarefas/2/comentarios               -> 404
POST   /tarefas/2/comentarios               -> 404
POST   /tarefas/2/responsaveis              -> 404
DELETE /tarefas/2/responsaveis/2            -> 404
GET    /projetos?pesquisa=Website           -> 200 {"itens":[],"total":0}
== Rui (membro, não Gestor) ==
PATCH  /projetos/1                          -> 403 "Só o gestor do projeto pode fazer esta operação"
DELETE /projetos/1                          -> 403
POST   /projetos/1/membros                  -> 403
DELETE /tarefas/2 (criada pela Ana)         -> 403 "Só quem criou a tarefa ou o gestor do projeto a pode eliminar"
== Campos que o cliente não pode definir (mass assignment) ==
POST   /projetos/1/tarefas {"titulo":"x","criadorId":"1"} -> 400 "Campo não permitido: criadorId"
PATCH  /tarefas/2 {"projetoId":"3"}         -> 400 "Campo não permitido: projetoId"
POST   /projetos {"nome":"P","gestorId":"1"} -> 400 "Campo não permitido: gestorId"
== Depois de tudo, para a Ana ==
GET    /projetos/1 -> 200 (nome original);  GET /tarefas/2 -> 200 (estado original)
```

**Conclusão: não há IDOR.** Nenhuma das tentativas sem permissão (15 da Joana, 4 do Rui e 3 de campos proibidos) teve efeito. Um não membro recebe sempre o mesmo `404` de um recurso inexistente (CE-07), e um membro sem papel de Gestor recebe `403`. Pela leitura do código, as 15 operações sobre um projeto ou uma tarefa existentes passam por `ControloAcesso.exigirMembro`, `exigirGestor` ou `exigirMembroDaTarefa` antes de lerem ou alterarem dados, e o projeto de uma tarefa vem sempre da base de dados, nunca do pedido. As restantes (login, sessão, listar e criar projetos) só usam o utilizador do token.

Testes automáticos que já protegem este comportamento (e servem de regressão): `TarefasTest.quemNaoEMembroRecebe404EmTodasAsOperacoes`, `TarefasTest.soOCriadorOuOGestorEliminam`, `ProjetosTest.projetoAlheioEInexistenteDaoOMesmo404`, `ResponsaveisEComentariosTest.naoMembroNaoGereResponsaveis`, `ResponsaveisEComentariosTest.naoMembroNaoVeNemComenta`, `ControloAcessoTest`, `CriarEConsultarTarefaTest.tarefaInexistenteEAlheiaDaoOMesmo404`.

### 3.2 Outros findings de segurança

| ID | Onde | Evidência | Impacto | Severidade | Estado | Correção proposta e teste de regressão |
|----|------|-----------|---------|------------|--------|----------------------------------------|
| SEC-01 | `services/ServicoAutenticacao.java` | 20 logins seguidos com password errada para a Ana: todos `401`, e o login certo a seguir dá `200`. Não há bloqueio nem limite de tentativas. O RNF-11 (Must: bloquear 15 min após 5 falhas) foi adiado como extra na Ficha 03 | Ataque de força bruta ou de dicionário às passwords sem qualquer travão | **Média** | Confirmado | Implementar o RNF-11: contador de falhas e `bloqueado_ate` na tabela `utilizador` (migration V3), mesma resposta `401` para não revelar o bloqueio, desbloqueio automático. Regressão: `AutenticacaoTest` (5 falhas → a 6.ª tentativa, mesmo com a password certa, é recusada; depois do prazo, entra), com o relógio controlado no teste |
| SEC-02 | `application.yml` (`matriz.seed.ativo: ${MATRIZ_SEED_ATIVO:true}`) | Por omissão o seed está **ligado**. Numa instalação nova sem a variável definida, se `MATRIZ_SEED_PASSWORD` estiver definida, são criadas três contas com uma password conhecida. Se não estiver, o backend não arranca | Contas de demonstração numa instalação real | Baixa | Confirmado na configuração; o impacto é uma hipótese (não há instalação de produção) | Mudar o valor por omissão para `false` e manter `MATRIZ_SEED_ATIVO=true` no `.env.example` (desenvolvimento). Regressão: teste de configuração que arranca sem a variável e confirma que não cria utilizadores |
| SEC-03 | `frontend/src/auth/sessao.ts` | O token fica no `sessionStorage` (decisão da Ficha 03, Ex. 3), acessível a JavaScript da página | Se um dia houver XSS, o token pode ser roubado (válido até 8 h ou até ao logout) | Baixa | Hipótese: não há XSS conhecido (0 usos de `dangerouslySetInnerHTML`, `innerHTML` ou `eval`; o React escapa o texto). Não há configuração de produção, por isso também não há `Content-Security-Policy` | Sem correção agora. Quando houver configuração de produção, acrescentar uma CSP restritiva |
| SEC-04 | `POST /projetos/{id}/membros` | Adicionar um email inexistente dá `422` "Não existe utilizador com este email"; um email existente dá `201` (`ProjetosTest.gestorAdicionaMembroPorEmail`) | Um Gestor consegue saber se um email tem conta | Info | Confirmado. Risco já aceite (RK-02, Ficha 01) | Sem correção |

### 3.3 Suspeitas testadas e refutadas

| Suspeita | Verificação | Resultado |
|----------|-------------|-----------|
| Enumeração de contas no login | Password errada para `ana@` e para `ninguem@` | Mesmo `401` e mesma mensagem. O código compara com um hash fictício quando o email não existe, para o tempo de resposta não denunciar a conta |
| Token com `alg: none` | Token da Ana com o cabeçalho trocado por `{"alg":"none"}` e sem assinatura | `401`: o descodificador só aceita HS256 |
| Token adulterado ou de uma sessão terminada | Último carácter alterado; token usado depois de `POST /auth/logout` | `401` nos dois. O logout revoga o token (versão do token na base de dados) |
| Corrida ao atribuir o mesmo responsável | 10 pedidos `POST /tarefas/{id}/responsaveis` em paralelo | 1 × `201` e 9 × `409`; uma só linha na base de dados e nenhum `500` |
| Password com mais de 72 bytes (limite do BCrypt) dá `500` | Login com 100 caracteres, com conta existente e inexistente | `401` nos dois, sem erro inesperado no log |
| Injeção de SQL | Leitura dos repositórios | Todas as consultas usam parâmetros (`:nome`). O único texto juntado ao SQL é o nome da coluna em `TarefaRepositorio.alterar`, que vem de um `enum` do código. A pesquisa escapa `%` e `_` |
| Secrets no repositório | Semgrep (regras de secrets), histórico do Git, ficheiros de configuração | Nenhum. `.env` nunca entrou no Git; o `.env.example` só tem valores fictícios e o segredo JWT vazio de propósito |

## 4. Análise estática

| Ferramenta | Comando | Resultado | Leitura |
|------------|---------|-----------|---------|
| **Semgrep** 1.185.0 | `semgrep scan --metrics=off --config <regras>/java --config <regras>/typescript --config <regras>/javascript --config <regras>/generic/secrets --config <regras>/dockerfile --config <regras>/yaml .` | 648 regras em 160 ficheiros: **86 findings, todos `jsx-not-internationalized` (INFO)**; 0 em Java, 0 secrets, 0 de segurança | Os 86 são falsos positivos para este projeto: a interface é só em português (RNF-07) e não há requisito de internacionalização. Nenhuma ação |
| **oxlint** (já configurado) | `cd frontend && npm run lint` | 0 avisos, 0 erros | |
| **npm audit** | `cd frontend && npm audit`; `cd e2e && npm audit` | 0 vulnerabilidades nos dois | |

**Limitações da análise estática.** O registo de regras `semgrep.dev` está bloqueado pela rede deste ambiente, por isso não se usaram os pacotes `p/owasp-top-ten` e `p/java`. Usaram-se as mesmas regras a partir do repositório público `semgrep/semgrep-rules` (clonado à parte, fora do projeto). **Não foi executado:** análise de vulnerabilidades das dependências Maven do backend (ex.: OWASP Dependency-Check, que precisa da base de dados NVD) nem SonarQube, que não está configurado no projeto.

## 5. Decisões pedidas ao Mário

A ficha pede para corrigir **só os problemas confirmados e prioritários, depois de revisão humana**, com um teste de regressão por correção. Proposta:

| Finding | Severidade | Recomendação |
|---------|------------|--------------|
| SEC-01 Sem limite de tentativas de login (RNF-11) | Média | **Corrigir agora.** É o único finding de segurança confirmado com impacto real, e o requisito é Must |
| CR-02 Limite da pesquisa contado antes da limpeza | Baixa | Corrigir: são duas linhas, com teste |
| CR-03 `responsavelId` inválido no URL | Baixa | Corrigir: uma linha, com teste |
| SEC-02 Seed ligado por omissão | Baixa | Corrigir: mudar o valor por omissão, com teste |
| SEC-03, SEC-04, CR-04 | Baixa / Info | Não corrigir agora; ficam registados |

*(A preencher depois da decisão: o que foi corrigido, com que commits e testes.)*

## 6. Verificações executadas e por executar

**Executadas (2026-11-11, ambiente cloud):** leitura do diff do PR #11 e de todas as regras de acesso; 29 pedidos de IDOR e de tokens (§3.1); 7 suspeitas testadas (§3.3); Semgrep, oxlint e `npm audit` (§4); suíte de testes existente verde antes da revisão (100 backend, 31 frontend, 1 E2E).

**Por executar ou validar:** análise de dependências do backend; revisão de uma configuração de produção (HTTPS, CSP, cabeçalhos), que ainda não existe; testes de carga (RNF-08); o caso CR-04, se alguma rota passar a ter parâmetros obrigatórios.
