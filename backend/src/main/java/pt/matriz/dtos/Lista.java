package pt.matriz.dtos;

import java.util.List;

/** Envelope das listagens: {@code { itens, total }}. */
public record Lista<T>(List<T> itens, int total) {

    public static <T> Lista<T> de(List<T> itens) {
        return new Lista<>(itens, itens.size());
    }
}
