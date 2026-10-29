package pt.matriz.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pt.matriz.domain.Utilizador;

/** Utilizadores, através de Spring Data JPA (a única entidade JPA do projeto). */
public interface UtilizadorRepositorio extends JpaRepository<Utilizador, Long> {

    /** O email é único sem distinguir maiúsculas (CE-08). */
    Optional<Utilizador> findByEmailIgnoreCase(String email);
}
