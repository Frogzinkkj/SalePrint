package com.gerenciador.impressora.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "impressora_id", nullable = false)
    private Impressora impressora;

    @NotNull
    @Column(name = "data_movimentacao", nullable = false)
    private LocalDateTime dataMovimentacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_origem_id")
    private Setor setorOrigem;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_destino_id", nullable = false)
    private Setor setorDestino;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status_aplicado", nullable = false)
    private StatusImpressora statusAplicado;

    @NotBlank
    @Column(nullable = false)
    private String responsavel;

    @Column(name = "os_qualycopy")
    private String osQualycopy;

    private String descricao;
}
