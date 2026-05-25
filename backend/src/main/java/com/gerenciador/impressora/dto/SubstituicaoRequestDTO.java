package com.gerenciador.impressora.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubstituicaoRequestDTO {

    private Long impressoraAntigaId;
    private Long impressoraNovaId;

    private String numeroSerieAntiga;
    private String numeroSerieNova;

    @NotBlank(message = "Responsável é obrigatório")
    private String responsavel;

    private String observacao;

    private Boolean atualizarStatus = true;
}
