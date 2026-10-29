package pt.matriz.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Nomes das chaves como no schema {@code ContagensPorEstado}. */
public record Contagens(
        @JsonProperty("POR_FAZER") long porFazer,
        @JsonProperty("EM_CURSO") long emCurso,
        @JsonProperty("CONCLUIDA") long concluidas,
        long total) {
}
