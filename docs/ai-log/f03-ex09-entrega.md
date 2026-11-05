# Ficha 03 — Exercício 9: Rever e preparar a entrega

> Ferramenta de IA usada: Claude (Claude Code), com o prompt estruturado (B).

## Prompt

```text
# Goal
Rever a implementação e preparar a entrega da Ficha 03 (incremento I-8).

# Context
Ficha 03, Ex. 9; código completo (Ex. 2 a 8), openapi.yaml, README.md, AGENTS.md,
docs/verification.md (Ex. 8) e as duas observações do Ex. 8 aprovadas pelo Mário
(mensagem ao eliminar projeto; filtros no URL).

# Constraints
- Rever com atenção permissões, validações, tratamento de erros e alterações fora do âmbito.
- A refatoração vai num commit separado e não pode mudar o comportamento.
- Contrato, README e AGENTS.md têm de corresponder à implementação real.
- Commits pequenos, com mensagens claras.

# Task
Rever o diff; corrigir o que for encontrado; fazer as duas melhorias aprovadas; uma refatoração;
alinhar a documentação; registar 3 exemplos de uso da IA; testar a partir de uma cópia limpa.

# Validation
- ./mvnw test, npm test, npm run build, npm run lint.
- Script que compara a API em execução com o openapi.yaml.
- git clone para uma pasta nova e seguir só o README.
```

## Revisão do código

| Área | O que se viu | Resultado |
|------|--------------|-----------|
| Permissões | Todas as operações passam por `ControloAcesso` antes de ler ou alterar dados; 404 para não membros e 403 para membros sem o papel certo; o criador, o autor e o Gestor vêm sempre do token | Sem problemas |
| Validações | Limpeza e limites de texto nos serviços; valores de enum, datas e campos desconhecidos recusados no JSON; responsáveis validados como membros | Sem problemas; a lógica repetida em 5 métodos levou à refatoração |
| SQL | Parâmetros com nome em todo o lado; a única concatenação (`TarefaRepositorio.alterar`) usa o nome da coluna de um `enum` interno, nunca um valor do cliente | Sem problemas |
| Tratamento de erros | Rota inexistente, método não suportado, corpo que não é JSON e erros inesperados respondiam no formato por defeito do Spring, em inglês, e não em Problem Details como diz o contrato | **Corrigido** (`TratamentoErros` + `TratamentoErrosTest`) |
| Contrato | Script novo (`scripts/verificar-contrato.py`) provoca todos os códigos documentados das 20 operações e valida os corpos | **1 diferença:** `GET /projetos` devolvia `400` sem estar documentado. Contrato corrigido (0.3.0) |
| Fora do âmbito | Nenhuma funcionalidade dos extras (Kanban, notificações, admin) entrou no código | Sem problemas |
| Documentação | O README listava como "MVP" funcionalidades que não foram implementadas; o AGENTS.md dizia que a stack estava por decidir; a verificação do Ex. 8 dizia "74 testes" quando eram 71 | **Corrigido** |

## Alterações (um commit por passo)

1. **Erros fora do contrato:** handlers para 404/405/415/500 em Problem Details; 4xx do Spring mantêm o código; o 500 vai para o log sem detalhes na resposta. OpenAPI 0.3.0.
2. **Melhorias aprovadas pelo Mário:** filtros da lista de tarefas no URL (`filtrosDoUrl`/`filtrosParaUrl`, `useSearchParams`, sem entradas novas no histórico); mensagem "Projeto … eliminado." no painel.
3. **Refatoração:** `CampoTexto` (record) substitui `Texto` e os cinco métodos de validação; cada serviço declara os seus campos como constantes.
4. **Documentação:** README (funcionalidades entregues, resumo do arranque, contrato, estado), AGENTS.md (stack decidida, tabela de comandos), `docs/verification.md` §9–11 (revisão final, 3 exemplos de IA, cópia limpa), plano (I-8).

## Decisão sobre a refatoração

- **Oportunidade:** `tituloValido`, `nomeValido`, duas `descricaoValida` e `conteudoValido` repetiam o mesmo padrão (limpar, obrigatório/opcional, tamanho máximo), com mensagens montadas à mão.
- **Benefício:** as regras de cada campo ficam declaradas num só sítio por serviço (`TITULO`, `NOME`, `DESCRICAO`, `CONTEUDO`), e um campo novo deixa de precisar de outro método igual.
- **Comportamento preservado:** os 75 testes do backend passam sem alterações aos testes, o script de contrato não encontra divergências e os pedidos manuais devolvem as mesmas mensagens.
- **Alternativa considerada e não seguida:** passar a validação para anotações Bean Validation nos DTOs. Separava a limpeza dos espaços da validação do tamanho (o tamanho conta depois da limpeza) e mudava a ordem dos erros.

## Validação

| Verificação | Resultado |
|-------------|-----------|
| `./mvnw test` | ✅ 75 testes, 0 falhas (71 + 4 do `TratamentoErrosTest`) |
| `npm test` | ✅ 28 testes, 0 falhas (22 + 2 dos filtros no URL + 4 do `ProjetoDetalhe.test.tsx`) |
| `npm run build`, `npm run lint` | ✅ sem erros nem avisos |
| Script de contrato | ✅ 76 combinações operação/código, sem divergências |
| Browser | ✅ filtros mantidos ao voltar e ao recarregar; mensagem ao eliminar projeto, que não reaparece ao recarregar; sem erros na consola |
| Cópia limpa | ✅ clone novo, só com o README: esquema, seed, backend, frontend, testes e contrato (ver `docs/verification.md` §11) |

## Correções durante o exercício

- O primeiro teste do `ProjetoDetalhe` procurava o botão "Eliminar" pelo nome e encontrava dois (o do projeto e o de uma tarefa). Passou a usar o primeiro, que é o do projeto.
- Ao contar os testes do backend na cópia limpa, vi que eram 75 e não 78, como eu tinha previsto. O erro vinha do Ex. 8, onde escrevi "74" quando eram 71: os casos de regressão entraram em testes que já existiam. Corrigi o `docs/verification.md`, o ai-log do Ex. 8 e a mensagem do commit da refatoração, que ainda não tinha sido enviado.

## Decisões tomadas neste exercício (a validar)

- **Script de contrato em Python** (`scripts/`), fora dos testes Maven: não acrescenta dependências ao backend, mas precisa de `pyyaml` e `jsonschema`.
- **Mudar de filtro não cria entradas no histórico**, por isso "Voltar" sai da página do projeto em vez de desfazer o filtro.
