package com.gerenciador.impressora.repository;

import com.gerenciador.impressora.model.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    List<Movimentacao> findByImpressoraIdOrderByDataMovimentacaoDesc(Long impressoraId);

    Optional<Movimentacao> findFirstByImpressoraIdOrderByDataMovimentacaoDesc(Long impressoraId);
}
