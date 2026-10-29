# Ficha 03 — Exercício 4: Implementar a gestão de projetos

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Primeiro percurso completo entre interface, API e base de dados: projetos e membros (incremento I-3).

# Context
openapi.yaml (/projetos, /projetos/{id}, /projetos/{id}/membros), US-03, US-04, US-05, US-18,
docs/implementation-plan.md §3 (regras de acesso), ControloAcesso e UtilizadorAtual do Exercício 3.

# Constraints
- Códigos HTTP e corpos como no contrato; nome 1–100 caracteres (não só espaços), descrição até 1000 (C-07).
- O projeto fica associado ao utilizador do token; o cliente não indica o Gestor.
- Listagem e operações limitadas aos projetos de que o utilizador é membro (404 para os outros).
- Pesquisa pelo nome sem distinguir maiúsculas nem acentos. Não há paginação no contrato.
- Eliminar pede confirmação na interface e apaga em cascata (RN-10), sem dados órfãos.
- Remover um membro tira-lhe as atribuições no projeto, mas as tarefas ficam (RN-03, CE-01).

# Task
1. Repositório, serviço e controlador de projetos e membros.
2. Testes por HTTP com os três utilizadores do seed.
3. Painel com lista, contagens, pesquisa e criação; detalhe com edição, eliminação e membros.

# Validation
- mvn test; npm run build e lint.
- Percurso da ficha no browser: login, criar, ver na lista, recarregar e reiniciar o backend.
```

## Resultado

| Ficheiro | Conteúdo |
|----------|----------|
| `projetos/ProjetoRepositorio.java` | SQL explícito: listagem com contagens por estado e papel numa só consulta; pesquisa com `unaccent` e curingas escapados |
| `projetos/ServicoProjetos.java` | Regras: validação, Gestor = utilizador do token, só o Gestor edita, elimina e gere membros, o Gestor não pode ser removido |
| `projetos/ControladorProjetos.java`, `AlteracaoProjeto.java` | 8 operações do contrato; o `PATCH` distingue campo ausente de `descricao: null` |
| `db/migration/V2__pesquisa_sem_acentos.sql` | Ativa a extensão `unaccent` |
| `comum/ErroApi.java`, `ErroValidacao.java`, `TratamentoErros.java` | 409, 422, 400 com campo vindo do serviço; id não numérico no caminho dá 404 |
| `test/.../ApiTeste.java`, `projetos/ProjetosTest.java` | 17 testes por HTTP, cada um numa transação desfeita no fim |
| `frontend/src/paginas/Inicio.tsx` | Painel: projetos com contagens, pesquisa ao escrever, criar projeto, estados de carregamento, vazio, erro e sucesso |
| `frontend/src/paginas/ProjetoDetalhe.tsx` | Detalhe: editar e eliminar (só Gestor, com confirmação), membros (adicionar por email e remover, com confirmação) |
| `frontend/src/componentes/` | Barra, formulário de projeto, contagens, mensagens de estado |

## Validação

| Verificação | Resultado |
|-------------|-----------|
| `mvn test` | ✅ 42 testes, 0 falhas (17 novos de projetos e membros) |
| Listagem | ✅ Ana vê só "Formação interna" (Membro) e "Website institucional" (Gestora), com contagens 1/1/1 |
| Pesquisa | ✅ "web" e "FORMACAO" encontram os projetos certos; "app" não mostra o projeto onde a Ana não é membro; `%` e `_` não funcionam como curingas |
| Criar | ✅ 201 com `Location`; quem cria fica Gestor; os outros não o veem (404); enviar `gestorId` dá 400 |
| Validação | ✅ nome em falta, vazio, só espaços ou com 101 caracteres e descrição com 1001 dão 400 com o campo; 100 e 1000 são aceites |
| Editar e eliminar | ✅ Gestor 200/204; membro 403 "Só o gestor do projeto pode fazer esta operação"; não membro 404 |
| Sem dados órfãos | ✅ eliminar "Website institucional" apaga as 3 tarefas, os 2 comentários e os membros |
| Membros | ✅ adicionar por email (sem distinguir maiúsculas) 201; repetido 409; email sem conta 422; remover o Gestor 409; remover o Rui tira-lhe o acesso e as atribuições, mas as tarefas ficam |
| **Percurso da ficha no browser** | ✅ login → criar "Website 2027" → aparece na lista → recarregar → reiniciar o backend → continua lá |
| Restante percurso no browser | ✅ pesquisa sem resultados, erro "O nome é obrigatório", adicionar a Joana, membro repetido, editar, eliminar → Cancelar (nada muda) → confirmar → sai da lista e o link dá "Projeto não encontrado"; o Rui (só membro) não vê Editar, Eliminar nem o formulário de membros |
| `npm run build`, `npm run lint` | ✅ sem erros nem avisos |

## Correções durante a implementação

- O lint avisou que os ecrãs mudavam o estado de forma síncrona dentro de um `useEffect`, o que provoca renderizações em cascata. Os pedidos passaram para efeitos que ignoram respostas antigas. Assim, uma pesquisa lenta já não substitui o resultado de uma pesquisa mais recente.
- A primeira versão da pesquisa podia ficar presa em "A carregar projetos…" no primeiro arranque. Isto acontecia quando a pausa da pesquisa terminava depois de a lista já ter chegado. Foi corrigido: a pausa só atua quando o texto muda.

## Decisões tomadas neste exercício (a validar)

- **SQL explícito (JdbcTemplate) para projetos e membros**, em vez de entidades JPA: as respostas juntam contagens e o papel de quem pede, que ficam mais claros numa consulta.
- **Nome e descrição guardados sem espaços nas pontas**; uma descrição vazia fica sem descrição (`null`).
- **Email sem conta dá 422** "Não existe utilizador com este email", como na US-04. Revela que o email não tem conta, mas só a quem é Gestor (risco RK-02, já aceite).
- **Painel e lista de projetos no mesmo ecrã**: o painel inicial pedido no Exercício 7 é esta página, com as contagens por estado.
- **Confirmação de eliminação dentro da página**, em vez da janela `confirm` do browser, para poder dizer quantas tarefas vão ser apagadas.
