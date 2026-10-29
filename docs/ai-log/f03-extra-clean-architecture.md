# Ficha 03 — Extra: clean architecture no backend

> Pedido do Mário a 2026-10-29, entre o Exercício 4 e o Exercício 5. Não faz parte do enunciado da ficha.
> Ferramenta de IA usada: Claude (Claude Code).

## Pedido

> "Quero que coloques a clean architecture no projeto (domain, services, controllers, repositories, dtos, mappers). Sei que vamos ter muitas alterações para fazer mas quero que cumpras este pedido. Coloca também esta informação no README do projeto e nos documentos que forem necessários."

## Plano apresentado e decisões

1. Camadas em pacotes de `pt.matriz`: `domain`, `repositories`, `services`, `controllers`, `dtos`, `mappers`, mais `security`, `exceptions` e `config` para o que é transversal.
2. Regra de dependências para dentro: `controllers → services → repositories → domain`.
3. As pastas antigas (`comum/`, `identidade/`, `projetos/`) deixam de existir; o Mário foi avisado antes de qualquer ficheiro sair do sítio.
4. Opção proposta pela IA: tirar o JPA do `Utilizador` para o domínio ficar sem anotações de persistência. **O Mário decidiu manter o JPA** (`Utilizador` continua entidade JPA dentro de `domain`).
5. O código do Ex. 5 (tarefas) que ainda não estava no repositório do Mário ficou guardado à parte e volta já na nova estrutura no Ex. 5.

## O que mudou

| Antes (por módulo) | Depois (por camada) |
|--------------------|---------------------|
| `identidade/Utilizador`, `projetos/Papel`, `projetos/ProjetoVista`, `projetos/MembroVista` | `domain/Utilizador`, `domain/Papel`, `domain/Projeto`, `domain/Membro` + novos `ContagensTarefas`, `EdicaoProjeto`, `TokenEmitido`, `SessaoIniciada` |
| `identidade/UtilizadorRepositorio`, `projetos/ProjetoRepositorio` | `repositories/` (o SQL do `ControloAcesso` passou para `ProjetoRepositorio.papel` e `projetoDaTarefa`) |
| `identidade/ServicoAutenticacao`, `projetos/ServicoProjetos`, `projetos/ControloAcesso` | `services/` (`ServicoProjetos.editar` recebe `EdicaoProjeto` do domínio, não o DTO) |
| `identidade/ControladorAutenticacao`, `projetos/ControladorProjetos` (com os records de pedido e resposta lá dentro) | `controllers/` só com as rotas; records passaram para `dtos/` (`PedidoLogin`, `SessaoResposta`, `UtilizadorResposta`, `NovoProjeto`, `AlteracaoProjeto`, `NovoMembro`, `Lista`, `ResumoUtilizador`, `Contagens`, `ProjetoResposta`, `MembroResposta`) |
| Métodos `de(...)` nos records de resposta | `mappers/ProjetoMapper`, `mappers/UtilizadorMapper` |
| `identidade/ConfiguracaoSeguranca`, `ServicoTokens`, `ValidadorVersaoToken`, `UtilizadorAtual` | `security/` |
| `comum/ErroApi`, `ErroValidacao`, `Problemas`, `TratamentoErros` | `exceptions/` |
| `comum/ConfiguracaoComum`, `DadosDemonstracao` | `config/` |

Os ficheiros foram movidos com `git mv`, por isso o histórico de cada classe mantém-se. O antigo `DadosUtilizador` (id, nome, email) era igual a `UtilizadorResposta` e foi juntado a ele.

Novo teste `ArquiteturaTest` (ArchUnit 1.4.1, só em testes) com 6 regras: o domínio não depende das outras camadas; controladores não usam repositórios; serviços não conhecem controladores, DTOs nem mappers; repositórios só conhecem o domínio; DTOs são só dados; mappers não acedem a serviços nem a repositórios.

Documentos atualizados: `README.md` (secção "Arquitetura do código"), `docs/architecture.md` (§6 nova), `docs/implementation-plan.md` (estrutura e incremento extra), `AGENTS.md` (§5 nova, "Respeitar a arquitetura do backend").

## Verificação

- `mvn test`: **48 testes, 0 falhas** (os 42 que já existiam, sem alterações além do pacote e dos imports, e os 6 novos de arquitetura).
- O contrato da API, a base de dados e o frontend não mudaram: os testes de integração (`ProjetosTest`, `AutenticacaoTest`, `ControloAcessoTest`) passam com as mesmas asserções.
- Prova de que o `ArquiteturaTest` deteta violações: acrescentou-se temporariamente um campo `ProjetoRepositorio` em `ControladorProjetos`; o teste falhou com `controladoresNaoUsamRepositorios`. A alteração foi desfeita.

## Revisão humana

- O Mário escolheu manter o JPA no `Utilizador` em vez de passar para SQL explícito.
- Compromissos registados em `docs/architecture.md` §6: `ErroApi` usado nos serviços; sem interfaces ("portas") para os repositórios enquanto houver uma só implementação.
