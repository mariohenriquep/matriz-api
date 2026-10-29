package pt.matriz.mappers;

import java.time.LocalDate;
import java.util.List;

import pt.matriz.domain.CriacaoTarefa;
import pt.matriz.domain.EdicaoTarefa;
import pt.matriz.domain.Pessoa;
import pt.matriz.domain.Tarefa;
import pt.matriz.dtos.AlteracaoTarefa;
import pt.matriz.dtos.NovaTarefa;
import pt.matriz.dtos.ResumoUtilizador;
import pt.matriz.dtos.TarefaResposta;
import pt.matriz.exceptions.ErroApi;

/** Converte tarefas entre o domínio e os DTOs de {@code /tarefas}. */
public final class TarefaMapper {

    private TarefaMapper() {
    }

    /** {@code hoje} é a data no fuso da equipa, para calcular {@code atrasada}. */
    public static TarefaResposta paraResposta(Tarefa tarefa, LocalDate hoje) {
        return new TarefaResposta(String.valueOf(tarefa.id()), String.valueOf(tarefa.projetoId()), tarefa.titulo(),
                tarefa.descricao(), tarefa.estado(), tarefa.prioridade(),
                tarefa.responsaveis().stream().map(TarefaMapper::paraResumo).toList(), paraResumo(tarefa.criador()),
                tarefa.dataLimite(), tarefa.atrasada(hoje), tarefa.totalComentarios(), tarefa.criadoEm(),
                tarefa.atualizadoEm());
    }

    public static CriacaoTarefa paraCriacao(NovaTarefa pedido) {
        return new CriacaoTarefa(pedido.titulo(), pedido.descricao(), pedido.prioridade(), pedido.dataLimite(),
                ids(pedido.responsavelIds()));
    }

    public static EdicaoTarefa paraEdicao(AlteracaoTarefa alteracao) {
        return new EdicaoTarefa(alteracao.temTitulo(), alteracao.titulo(), alteracao.temDescricao(),
                alteracao.descricao(), alteracao.temEstado(), alteracao.estado(), alteracao.temPrioridade(),
                alteracao.prioridade(), alteracao.temDataLimite(), alteracao.dataLimite());
    }

    private static ResumoUtilizador paraResumo(Pessoa pessoa) {
        return new ResumoUtilizador(String.valueOf(pessoa.id()), pessoa.nome());
    }

    /** Os ids do contrato são texto; um id que não seja numérico é um pedido inválido. */
    private static List<Long> ids(List<String> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().map(id -> {
            try {
                return Long.parseLong(id);
            } catch (NumberFormatException | NullPointerException e) {
                throw ErroApi.validacao("responsavelIds", "Identificador de responsável inválido");
            }
        }).toList();
    }
}
