package com.gerenciador.impressora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "impressoras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Impressora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String marca;

    @NotBlank
    @Column(nullable = false)
    private String modelo;

    @NotBlank
    @Column(name = "numero_serie", nullable = false, unique = true)
    private String numeroSerie;

    @NotBlank
    @Column(nullable = false)
    private String ip;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusImpressora status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    private String observacao;
}
