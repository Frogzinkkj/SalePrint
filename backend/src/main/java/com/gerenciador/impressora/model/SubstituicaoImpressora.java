package com.gerenciador.impressora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "substituicoes_impressora")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubstituicaoImpressora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "impressora_antiga_id", nullable = false)
    private Impressora impressoraAntiga;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "impressora_nova_id", nullable = false)
    private Impressora impressoraNova;

    @NotNull
    @Column(name = "data_substituicao", nullable = false)
    private LocalDateTime dataSubstituicao;

    @NotBlank
    @Column(nullable = false)
    private String responsavel;

    private String observacao;
}
