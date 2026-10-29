package pt.matriz.controllers;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pt.matriz.domain.EstadoTarefa;
import pt.matriz.domain.FiltroTarefas;
import pt.matriz.domain.Prioridade;
import pt.matriz.domain.Tarefa;
import pt.matriz.dtos.AlteracaoTarefa;
import pt.matriz.dtos.Lista;
import pt.matriz.dtos.NovaTarefa;
import pt.matriz.dtos.TarefaResposta;
import pt.matriz.mappers.TarefaMapper;
import pt.matriz.security.UtilizadorAtual;
import pt.matriz.services.ServicoTarefas;

/** {@code /projetos/{id}/tarefas} e {@code /tarefas/{id}} do openapi.yaml. */
@RestController
@RequestMapping("/api/v1")
public class ControladorTarefas {

    private final ServicoTarefas servico;
    private final UtilizadorAtual utilizadorAtual;
    private final Clock relogio;
    private final ZoneId fuso;

    public ControladorTarefas(ServicoTarefas servico, UtilizadorAtual utilizadorAtual, Clock relogio,
            @Value("${matriz.fuso-horario:Europe/Lisbon}") ZoneId fuso) {
        this.servico = servico;
        this.utilizadorAtual = utilizadorAtual;
        this.relogio = relogio;
        this.fuso = fuso;
    }

    @GetMapping("/projetos/{projetoId}/tarefas")
    public Lista<TarefaResposta> listar(@PathVariable long projetoId,
            @RequestParam(required = false) EstadoTarefa estado,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Long responsavelId) {
        LocalDate hoje = hoje();
        return Lista.de(servico.listar(projetoId, utilizadorAtual.id(),
                new FiltroTarefas(estado, prioridade, responsavelId)).stream()
                .map(tarefa -> TarefaMapper.paraResposta(tarefa, hoje)).toList());
    }

    @PostMapping("/projetos/{projetoId}/tarefas")
    public ResponseEntity<TarefaResposta> criar(@PathVariable long projetoId, @RequestBody NovaTarefa pedido) {
        Tarefa tarefa = servico.criar(projetoId, utilizadorAtual.id(), TarefaMapper.paraCriacao(pedido));
        return ResponseEntity.created(URI.create("/api/v1/tarefas/" + tarefa.id()))
                .body(TarefaMapper.paraResposta(tarefa, hoje()));
    }

    @GetMapping("/tarefas/{tarefaId}")
    public TarefaResposta obter(@PathVariable long tarefaId) {
        return TarefaMapper.paraResposta(servico.obter(tarefaId, utilizadorAtual.id()), hoje());
    }

    @PatchMapping("/tarefas/{tarefaId}")
    public TarefaResposta editar(@PathVariable long tarefaId, @RequestBody AlteracaoTarefa alteracao) {
        return TarefaMapper.paraResposta(
                servico.editar(tarefaId, utilizadorAtual.id(), TarefaMapper.paraEdicao(alteracao)), hoje());
    }

    @DeleteMapping("/tarefas/{tarefaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long tarefaId) {
        servico.eliminar(tarefaId, utilizadorAtual.id());
    }

    /** "Hoje" no fuso da equipa, para a marcação "atrasada". */
    private LocalDate hoje() {
        return LocalDate.now(relogio.withZone(fuso));
    }
}
