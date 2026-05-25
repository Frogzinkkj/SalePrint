package com.gerenciador.impressora.dto;

import com.gerenciador.impressora.model.StatusImpressora;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovimentacaoRequestDTO {

    @NotNull(message = "Impressora é obrigatória")
    private Long impressoraId;

    @NotNull(message = "Setor destino é obrigatório")
    private Long setorDestinoId;

    @NotNull(message = "Status é obrigatório")
    private StatusImpressora statusAplicado;

    @NotBlank(message = "Responsável é obrigatório")
    private String responsavel;

    private String osQualycopy;
    private String descricao;
}
