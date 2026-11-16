# Extras: página inicial pública e registo seguro (0.8.0)

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Página inicial pública (apresentação dos serviços) com inscrição de novos membros, garantindo a segurança do
login e do registo.

# Context
main na versão 0.7.1 (deploy no Render). Login com bloqueio por conta (RNF-11), passwords bcrypt, JWT, CORS
restrito. Backend em clean architecture (ArquiteturaTest), contrato em openapi.yaml.

# Constraints (do Mário)
- Os novos membros não criam projetos: só podem ser adicionados a equipas e projetos por um Gestor.
- Criar projetos e outras funcionalidades será pago no futuro: não implementar pagamentos, mas deixar o modelo
  preparado sem complicar.
- Segurança do login e do registo em primeiro lugar.
- Confirmação por email e recuperação de password ficam para depois (precisam de um serviço de email).
```

## Decisões

| Decisão | Porquê |
|---------|--------|
| Coluna `plano` (`GRATUITO`/`PROFISSIONAL`) em vez de um papel global | O papel (Gestor/Membro) é por projeto; o que se paga é uma capacidade da conta. Um enum chega agora e acomoda planos futuros. |
| Contas existentes ficam `PROFISSIONAL` | Ninguém perde o que já fazia (seed e contas do Render). |
| Registo responde sempre `202` com a mesma mensagem e calcula sempre o hash | Não revela que emails existem (nem pela resposta nem pelo tempo). Por isso já não se inicia sessão no registo: US-01 crit. 1 e 3 alterados. |
| Limites por IP em memória, sem dependência nova | Uma só instância no Render gratuito; janela fixa simples, com teto de memória. Bucket4j/Redis só se houver várias instâncias. |
| Limite total de registos (50/hora) | Trava a criação em massa mesmo vinda de muitos IPs, ou se o IP do cliente for mal detetado. |
| IP do cliente contado a partir da direita do `X-Forwarded-For` | A entrada mais à esquerda é escrita pelo cliente e seria fácil de falsificar. |
| bcrypt com custo 10 (o atual) | O mínimo do OWASP; no Render (0,1 CPU) um custo maior tornaria cada login lento e facilitaria negar o serviço. |
| Regras da password: 8–64 caracteres, ≤72 bytes, ≥4 diferentes, lista de comuns, sem o email | Segue o NIST 800-63B (comprimento e lista de proibidas em vez de regras de composição); 72 bytes é o limite do bcrypt. |
| Campo-armadilha `site` | Barato e eficaz contra robôs simples, sem CAPTCHA externo. |
| `Conta` separada de `Utilizador` no contrato | O plano só aparece ao próprio, nunca na lista de membros de um projeto. |

## Verificação

- Backend: `./mvnw verify` com 163 testes verdes (novos: `RegistoTest`, `LimitesPedidosTest`, `RegrasContaTest`, `LimitadorPedidosTest`, `EnderecoClienteTest`), incluindo o `ArquiteturaTest`.
- Frontend: `npm test` (62 testes), `npm run build` e `oxlint` sem avisos.
- E2E: 4 testes verdes; o novo (`registo.spec.ts`) percorre apresentação → registo → login sem projetos → registo repetido com a mesma mensagem → a Ana adiciona a conta → a conta vê o projeto.
- `scripts/verificar-contrato.py` sem divergências.

## Pendentes

- Confirmar no Render que o IP nos logs do limite é o do cliente (ver `docs/deploy-render.md`).
- Confirmação do email e recuperação da password (serviço de email); pagamentos.
