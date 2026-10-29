package pt.matriz.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import pt.matriz.dtos.ComentarioResposta;
import pt.matriz.dtos.Lista;
import pt.matriz.dtos.NovoComentario;
import pt.matriz.mappers.ComentarioMapper;
import pt.matriz.security.UtilizadorAtual;
import pt.matriz.services.ServicoComentarios;

/** {@code /tarefas/{id}/comentarios} do openapi.yaml. */
@RestController
@RequestMapping("/api/v1/tarefas/{tarefaId}/comentarios")
public class ControladorComentarios {

    private final ServicoComentarios servico;
    private final UtilizadorAtual utilizadorAtual;

    public ControladorComentarios(ServicoComentarios servico, UtilizadorAtual utilizadorAtual) {
        this.servico = servico;
        this.utilizadorAtual = utilizadorAtual;
    }

    @GetMapping
    public Lista<ComentarioResposta> listar(@PathVariable long tarefaId) {
        return Lista.de(servico.listar(tarefaId, utilizadorAtual.id()).stream()
                .map(ComentarioMapper::paraResposta).toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ComentarioResposta criar(@PathVariable long tarefaId, @RequestBody NovoComentario pedido) {
        return ComentarioMapper.paraResposta(servico.criar(tarefaId, utilizadorAtual.id(), pedido.conteudo()));
    }
}
