package com.gerenciador.impressora.repository;

import com.gerenciador.impressora.model.SubstituicaoImpressora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubstituicaoImpressoraRepository extends JpaRepository<SubstituicaoImpressora, Long> {

    @Query("""
            SELECT s FROM SubstituicaoImpressora s
            JOIN FETCH s.impressoraAntiga ia
            JOIN FETCH s.impressoraNova ino
            JOIN FETCH ia.setor sa
            JOIN FETCH sa.localidade
            JOIN FETCH ino.setor sn
            JOIN FETCH sn.localidade
            WHERE s.id = :id
            """)
    Optional<SubstituicaoImpressora> findByIdComDetalhes(@Param("id") Long id);

    @Query("""
            SELECT s FROM SubstituicaoImpressora s
            JOIN FETCH s.impressoraAntiga ia
            JOIN FETCH s.impressoraNova ino
            JOIN FETCH ia.setor sa
            JOIN FETCH sa.localidade
            JOIN FETCH ino.setor sn
            JOIN FETCH sn.localidade
            ORDER BY s.dataSubstituicao DESC
            """)
    List<SubstituicaoImpressora> findAllComDetalhes();

    @Query("""
            SELECT s FROM SubstituicaoImpressora s
            JOIN FETCH s.impressoraAntiga ia
            JOIN FETCH s.impressoraNova ino
            JOIN FETCH ia.setor sa
            JOIN FETCH sa.localidade
            JOIN FETCH ino.setor sn
            JOIN FETCH sn.localidade
            WHERE ia.id = :impressoraId OR ino.id = :impressoraId
            ORDER BY s.dataSubstituicao DESC
            """)
    List<SubstituicaoImpressora> findByImpressoraId(@Param("impressoraId") Long impressoraId);
}
