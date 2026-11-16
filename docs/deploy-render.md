# Deploy no Render (plano gratuito)

A Matriz pode ficar disponível na Internet no plano gratuito do [Render](https://render.com), descrita no ficheiro [`render.yaml`](../render.yaml) (um *Blueprint*). O Render cria três peças a partir dele:

| Peça | Tipo no Render | De onde vem |
|---|---|---|
| `matriz-db` | PostgreSQL 16 gratuito (Frankfurt) | criada pelo Render |
| `matriz-backend` | Web service gratuito (Frankfurt) | imagem `marrike/matriz-backend` do Docker Hub, com a versão fixada no `render.yaml` |
| `matriz-frontend` | Site estático (CDN) | construído pelo Render a partir da pasta `frontend/` do `main` |

O browser chama a API diretamente (`VITE_API_URL`) e o backend autoriza a origem do frontend por CORS (`MATRIZ_CORS_ORIGENS`), como no Docker Compose. O backend liga-se à base de dados pela rede privada do Render; por isso a base de dados e o backend estão na mesma região.

## Limites do plano gratuito

- **O backend adormece** depois de 15 minutos sem pedidos. O pedido seguinte acorda-o, mas o arranque do Spring Boot na instância gratuita (0,1 CPU) demora **cerca de 2 a 4 minutos** (medido com os mesmos limites: 122 s com as opções da JVM do `render.yaml`, 220 s sem elas). Durante esse tempo o frontend abre, mas o login e as listas falham até o backend responder.
- **A base de dados gratuita expira 30 dias depois de ser criada.** Há mais 14 dias para passar a um plano pago; depois disso o Render **apaga-a com os dados**. Só pode haver uma base de dados gratuita por conta, tem 1 GB e não tem cópias de segurança. Para manter os dados, fazer um `pg_dump` antes de expirar (secção [Base de dados](#base-de-dados)).
- 750 horas gratuitas de instância por mês na conta (chegam para um serviço ligado o mês inteiro; um serviço a dormir não gasta horas).
- Sem disco persistente nem acesso por shell ao container; o Render pode reiniciar um serviço gratuito a qualquer momento. A Matriz não guarda nada em disco, por isso isto não a afeta.
- O site estático não adormece.
- Os endereços `*.onrender.com` são públicos. Não é um ambiente de produção: serve para demonstração.

## Contas de demonstração

Desde a 0.8.0 há registo público ("Criar conta" na página inicial), mas as contas novas ficam no plano gratuito e não criam projetos. Por isso no Render o seed continua ligado (`MATRIZ_SEED_ATIVO=true`): no primeiro arranque, com a base de dados vazia, cria as contas `ana@matriz.local`, `rui@matriz.local` e `joana@matriz.local` e os três projetos de exemplo, todos com a password `MATRIZ_SEED_PASSWORD`. Esta password é escolhida por quem cria o Blueprint e nunca entra no Git. Como o site é público, usar uma password forte (por exemplo `openssl rand -base64 18`). O seed não volta a correr enquanto houver utilizadores.

## Primeira instalação

Antes: a imagem com a versão do `render.yaml` tem de estar no Docker Hub (a CI publica-a quando é criada a tag `vX.Y.Z` no `main`). Confirmar em <https://hub.docker.com/r/marrike/matriz-backend/tags>.

1. Criar uma conta em <https://dashboard.render.com/register> (pode ser com a conta do GitHub). O plano gratuito não pede cartão.
2. Em <https://dashboard.render.com/blueprints>, **New Blueprint Instance**. Ligar o GitHub quando o Render pedir e dar acesso ao repositório `mariohenriquep/matriz-api`. Escolher o repositório e o ramo `main`; o Render lê o `render.yaml`.
3. Dar um nome ao Blueprint (por exemplo `matriz`) e preencher os três valores pedidos:
   - `MATRIZ_CORS_ORIGENS` = `https://matriz-frontend.onrender.com`
   - `VITE_API_URL` = `https://matriz-backend.onrender.com/api/v1`
   - `MATRIZ_SEED_PASSWORD` = a password das contas de demonstração (guardá-la num gestor de passwords).
4. **Deploy Blueprint**. O Render cria a base de dados, depois o backend (o primeiro deploy demora alguns minutos: aplica as migrações Flyway e cria as contas) e o frontend.
5. Confirmar os endereços reais no topo de cada serviço. Se um nome já estava ocupado, o Render acrescenta um sufixo (por exemplo `matriz-backend-a1b2.onrender.com`). Nesse caso corrigir as variáveis em **Environment**: `MATRIZ_CORS_ORIGENS` no backend e `VITE_API_URL` no frontend, e no frontend fazer **Manual Deploy → Clear build cache & deploy**, porque o endereço da API fica fixado na build.
6. Testar: `https://<backend>/actuator/health` deve devolver `{"status":"UP"}`; depois abrir o frontend e entrar com `ana@matriz.local` e a password escolhida.

O segredo dos tokens (`MATRIZ_JWT_SEGREDO`) é gerado pelo Render e os dados de ligação à base de dados passam diretamente da base de dados para o backend: nenhum deles precisa de ser copiado à mão.

## Atualizar para uma nova versão

- **Backend**: depois de criada a tag e publicada a imagem, mudar a tag em `image.url` no `render.yaml` num PR. Com o merge, o Render sincroniza o Blueprint e faz o deploy da nova imagem; as migrações Flyway correm no arranque. (O Render não volta a descarregar a imagem sozinho se a mesma tag for reescrita.)
- **Frontend**: cada merge no `main` que mude `frontend/` volta a construir o site.

## Base de dados

Os dados de ligação estão na página da base de dados no Render (**Connections**). O endereço externo serve para uma cópia a partir do Mac, por exemplo antes de a base de dados expirar:

```bash
pg_dump "<External Database URL>" --no-owner -Fc -f matriz-$(date +%F).dump
```

Para continuar depois de a base de dados gratuita expirar sem pagar: apagar a base de dados no Render, criar uma nova a partir do Blueprint (**Manual Sync**) e repor a cópia com `pg_restore --no-owner -d "<External Database URL>" matriz-AAAA-MM-DD.dump`.

## Variáveis de ambiente no Render

| Variável | Serviço | Origem |
|---|---|---|
| `MATRIZ_DB_HOST`, `MATRIZ_DB_PORTA`, `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | backend | base de dados `matriz-db` |
| `MATRIZ_JWT_SEGREDO` | backend | gerado pelo Render |
| `MATRIZ_CORS_ORIGENS`, `MATRIZ_SEED_PASSWORD` | backend | pedidos ao criar o Blueprint |
| `MATRIZ_SEED_ATIVO`, `MATRIZ_FUSO_HORARIO`, `MATRIZ_PROXIES_CONFIAVEIS`, `PORT`, `JAVA_TOOL_OPTIONS` | backend | `render.yaml` |
| `VITE_API_URL` | frontend (build) | pedido ao criar o Blueprint |
| `NODE_VERSION` | frontend (build) | `render.yaml` |

O backend aceita o endereço da base de dados de duas formas: `MATRIZ_DB_URL` com o endereço JDBC completo (desenvolvimento e Docker Compose) ou, sem ela, as partes `MATRIZ_DB_HOST`, `MATRIZ_DB_PORTA` e `POSTGRES_DB` (Render, que não dá um endereço JDBC).

## Limites de pedidos atrás do Render (0.8.0)

O login (10 por minuto) e o registo (5 por hora, 50 por hora no total) têm limites por IP. No Render, o pedido passa pelo Cloudflare e pelo balanceador do Render antes de chegar ao backend, e cada um acrescenta uma entrada ao `X-Forwarded-For`. Com `MATRIZ_PROXIES_CONFIAVEIS=2` o backend usa a penúltima entrada, a que o Cloudflare escreveu com o IP real; as entradas mais à esquerda podem ser inventadas pelo cliente e nunca são usadas.

Confirmar depois do deploy: quando um IP passa o limite, o backend escreve nos logs `Limite de pedidos atingido em login para <ip>`. Se esse IP for de uma rede `104.x`/`172.64-71.x` (Cloudflare) em vez do teu, o número de proxies está errado: ajustar `MATRIZ_PROXIES_CONFIAVEIS` no Render. Mesmo com o valor errado, o limite total de registos continua a travar a criação de contas em massa.
