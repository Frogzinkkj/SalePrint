package com.gerenciador.impressora.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LocalidadeDTO {
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;
}
