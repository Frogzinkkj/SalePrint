package com.gerenciador.impressora.dto;

import com.gerenciador.impressora.model.StatusImpressora;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ImpressoraDTO {
    private Long id;

    @NotBlank(message = "Marca é obrigatória")
    private String marca;

    @NotBlank(message = "Modelo é obrigatório")
    private String modelo;

    @NotBlank(message = "Número de série é obrigatório")
    private String numeroSerie;

    @NotBlank(message = "IP é obrigatório")
    private String ip;

    @NotNull(message = "Status é obrigatório")
    private StatusImpressora status;

    @NotNull(message = "Setor é obrigatório")
    private Long setorId;

    private String setorNome;
    private String localidadeNome;
    private String observacao;
}
