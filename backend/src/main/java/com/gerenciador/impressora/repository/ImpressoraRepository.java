package com.gerenciador.impressora.repository;

import com.gerenciador.impressora.model.Impressora;
import com.gerenciador.impressora.model.StatusImpressora;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImpressoraRepository extends JpaRepository<Impressora, Long> {

    Optional<Impressora> findByNumeroSerieIgnoreCase(String numeroSerie);

    Optional<Impressora> findByIp(String ip);

    boolean existsByNumeroSerieIgnoreCase(String numeroSerie);

    boolean existsByNumeroSerieIgnoreCaseAndIdNot(String numeroSerie, Long id);

    @Query("SELECT i FROM Impressora i WHERE i.status = :status AND LOWER(i.ip) = LOWER(:ip)")
    Optional<Impressora> findAtivaByIp(@Param("ip") String ip, @Param("status") StatusImpressora status);

    @Query("SELECT i FROM Impressora i WHERE i.status = :status AND LOWER(i.ip) = LOWER(:ip) AND i.id <> :id")
    Optional<Impressora> findAtivaByIpAndIdNot(@Param("ip") String ip, @Param("status") StatusImpressora status, @Param("id") Long id);

    List<Impressora> findByStatus(StatusImpressora status);

    List<Impressora> findBySetorId(Long setorId);

    @Query("SELECT i FROM Impressora i WHERE LOWER(i.ip) LIKE LOWER(CONCAT('%', :termo, '%')) OR LOWER(i.numeroSerie) LIKE LOWER(CONCAT('%', :termo, '%'))")
    List<Impressora> buscarPorIpOuNumeroSerie(@Param("termo") String termo);

    @Query("""
            SELECT DISTINCT i FROM Impressora i
            JOIN FETCH i.setor s
            JOIN FETCH s.localidade l
            ORDER BY l.nome, s.nome, i.marca
            """)
    List<Impressora> findAllComRelacoes();

    long countByStatus(StatusImpressora status);

    @Query("SELECT COUNT(DISTINCT i.modelo) FROM Impressora i")
    long countDistinctModelos();
}
