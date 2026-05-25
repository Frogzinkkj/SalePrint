package com.gerenciador.impressora.repository;

import com.gerenciador.impressora.model.Localidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocalidadeRepository extends JpaRepository<Localidade, Long> {
    Optional<Localidade> findByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCase(String nome);
}
