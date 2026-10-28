# Restrições, pressupostos e decisões pendentes — Matriz

> Ficha 01, Exercício 4. ✅ = confirmado pelo Mário (atuando como cliente). Sem ✅ = pressuposto por confirmar.

## 1. Restrições conhecidas

| ID | Restrição | Origem |
|----|-----------|--------|
| R-01 | Projeto desenvolvido por fases, nas aulas da UC *AI-Powered Software Engineering*. | Enunciado |
| R-02 | Nas Aulas 01 e 02 não se implementa código funcional; só especificação e conceção. | Enunciado |
| R-03 | Todo o trabalho é versionado em Git neste repositório. | Enunciado |
| R-04 | A documentação em `docs/` e o `AGENTS.md` servem de contexto aos agentes de IA nas fases seguintes. | Enunciado |
| R-05 | O cliente deu apenas uma frase de descrição; o resto são decisões do Mário ou pressupostos. | Enunciado |

## 2. Pressupostos e decisões

| ID | Pressuposto / decisão | Estado | Impacto se mudar |
|----|-----------------------|--------|------------------|
| P-01 | Equipas internas, até ~50 pessoas e ~100 projetos ativos. | Por confirmar | Escala e arquitetura. |
| P-02 | Existe um Administrador global que gere todos os projetos e promove outros a administrador; os restantes são membros dos projetos onde participam. | ✅ | Modelo de permissões. |
| P-03 | Qualquer utilizador autenticado pode criar projetos. | ✅ | Permissões. |
| P-04 | Três estados fixos (`Por fazer`, `Em curso`, `Concluída`), qualquer transição permitida. | ✅ | Regras das tarefas e progresso. |
| P-05 | Progresso = % de tarefas `Concluída` sobre o total do projeto, arredondado às unidades. | Por confirmar | Painel de progresso. |
| P-06 | Prioridades `Baixa`, `Média` (por defeito), `Alta`. | Por confirmar | Modelo de dados. |
| P-07 | Uma tarefa tem no máximo um responsável. | Por confirmar | Modelo de dados. |
| P-08 | Só se adicionam a projetos pessoas que já têm conta. | Por confirmar | US-04. |
| P-09 | Interface só em português. | Por confirmar | Internacionalização. |
| P-10 | Os projetos arquivam-se (e podem ser reativados); não se apagam. | ✅ | Retenção de dados. |
| P-11 | Em edições simultâneas da mesma tarefa, prevalece a última gravação. | Por confirmar | Integridade dos dados. |
| P-12 | Os valores de desempenho do RNF-08 são uma estimativa nossa, não do cliente. | Por confirmar | Testes de desempenho. |
| P-13 | Uma tarefa pertence sempre a um projeto; campos: título, descrição, prioridade, responsável, data limite, estado. | Por confirmar | Modelo de dados. |
| P-14 | Cada utilizador só vê os projetos onde é membro (exceto administradores). | Por confirmar | Privacidade. |
| P-15 | Âmbito do MVP: RF-01 a RF-19 de `docs/requirements.md`. | ✅ | Planeamento. |
| P-16 | Aplicação web para computador e tablet; sem app móvel nativa. | Por confirmar | Interface. |
| P-17 | Sem prazo ou orçamento definidos; segue-se o calendário das aulas. | Por confirmar | Planeamento. |
| P-18 | Dados pessoais: só nome e email. Aplica-se o RGPD. | Por confirmar | Privacidade. |
| P-19 | Sem integrações externas; autenticação própria com email e password. | Por confirmar | Autenticação. |
| P-20 | O primeiro administrador global é criado na instalação, por configuração (revisto no Ex. 6, R-12); tem de existir sempre pelo menos um. | ✅ | Arranque do sistema. |
| P-21 | Notificações só in-app, nos eventos: atribuição de tarefa, adição a projeto, mudança de estado de tarefa atribuída. | Por confirmar | RF-16. |
| P-22 | Quem cria um projeto é o seu Gestor e não pode sair dele. | ✅ | Permissões. |
| P-23 | Password com pelo menos 8 caracteres. | ✅ | Registo. |
| P-24 | Nome do projeto até 100 caracteres; título da tarefa até 200. | ✅ | Validação. |
| P-25 | Quem faz uma ação não recebe notificação dela. | ✅ | Notificações. |
| P-26 | As notificações não lidas mantêm-se até serem abertas; não expiram no MVP. | ✅ | Armazenamento. |
| P-27 | Uma tarefa com data limite no passado e não concluída aparece como "Atrasada". | ✅ | US-06, interface. |
| P-28 | A sessão expira após 8 horas de inatividade (a rever com D-05). | Por confirmar | RNF-02. |
| P-29 | Após 5 tentativas de login falhadas seguidas, a conta fica bloqueada para login durante 15 minutos. | Por confirmar | RNF-11. |
| P-30 | Consultar as notificações a cada 30 segundos é suficiente (sem tempo real). | Por confirmar | RF-16, arquitetura. |
| P-31 | Frontend e backend são publicados no mesmo domínio. | Por confirmar | Autenticação, arquitetura. |

## 3. Segurança e privacidade

- Passwords guardadas só com hash forte e salt (ex.: bcrypt ou Argon2), nunca em texto simples.
- Autenticação obrigatória em todas as operações exceto registo e login; sessões ou tokens com expiração.
- Autorização verificada no servidor em cada pedido: é membro? é Gestor? é administrador? A interface nunca é a única barreira.
- Um pedido a um projeto onde o utilizador não tem acesso devolve "não encontrado", para não revelar que o projeto existe.
- Mensagens de login genéricas ("Credenciais inválidas"), para não revelar que emails estão registados.
- Limitar tentativas de login seguidas (proteção contra força bruta): RNF-11.
- Validação e limites de tamanho em todos os campos; proteção contra injeção (SQL, XSS).
- Promover a administrador é uma ação sensível: só um administrador a pode fazer e fica registada (RF-19).
- HTTPS em produção.
- RGPD: recolher só nome e email; a política de apagamento de contas fica pendente (D-06).
- Segredos (passwords da base de dados, chaves) nunca no repositório; usar variáveis de ambiente.

### Riscos conhecidos e aceites

| ID | Risco | Decisão | Origem |
|----|-------|---------|--------|
| RK-01 | **Registo aberto:** qualquer pessoa com acesso ao endereço da Matriz pode criar conta e projetos, embora os utilizadores previstos sejam internos (P-01). | Aceite no MVP. Ver D-09. | Ex. 6, R-13 |
| RK-02 | **Enumeração de contas:** a mensagem "Não existe utilizador com este email" (US-04) revela a um utilizador autenticado se um email tem conta. | Aceite: só utilizadores autenticados a veem e precisam de saber porque falhou. | Ex. 6, R-14 |

## 4. Decisões técnicas pendentes

| ID | Decisão | Quando | Notas |
|----|---------|--------|-------|
| D-01 | Linguagem e framework do backend. | Aula 02/03 | O `.gitignore` inicial do repositório é de Java, mas isso não é uma decisão tomada. |
| D-02 | Framework do frontend. | Aula 02/03 | — |
| D-03 | Base de dados. | Aula 02 | Proposto: relacional (`docs/architecture.md` §5); motor por decidir. |
| D-04 | Estilo de arquitetura (monólito, monólito modular, microservices). | Aula 02 | Proposto: monólito modular (`docs/architecture.md` §3). |
| D-05 | Mecanismo de autenticação (sessão com cookie ou token). | Aula 02/03 | — |
| D-06 | Política de apagamento de contas e retenção de dados. | Antes de produção | — |
| D-07 | Como entregar as notificações in-app (consulta periódica ou tempo real). | Aula 02/03 | Proposto: consulta periódica (`docs/architecture.md` §5). |
| D-08 | Alojamento e ambiente de produção. | Mais tarde | — |
| D-09 | Restringir o registo (domínio de email da empresa ou só por convite). | Antes de produção | Ver RK-01. |

## 5. Limitações de âmbito

- O MVP cobre só o que está em `docs/requirements.md` §6. Tudo o que está em §7 fica de fora, mesmo que pareça simples.
- Sem integração com sistemas externos nem migração de dados de outras ferramentas.
- O MVP não garante alta disponibilidade nem muitos utilizadores em simultâneo.
