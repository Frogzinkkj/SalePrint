package com.gerenciador.impressora.repository;

import com.gerenciador.impressora.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SetorRepository extends JpaRepository<Setor, Long> {
    List<Setor> findByLocalidadeIdOrderByNomeAsc(Long localidadeId);
    Optional<Setor> findByNomeIgnoreCaseAndLocalidadeId(String nome, Long localidadeId);
}
