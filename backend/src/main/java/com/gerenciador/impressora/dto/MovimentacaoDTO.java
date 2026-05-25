package com.gerenciador.impressora.dto;

import com.gerenciador.impressora.model.StatusImpressora;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovimentacaoDTO {
    private Long id;
    private Long impressoraId;
    private LocalDateTime dataMovimentacao;
    private Long setorOrigemId;
    private String setorOrigemNome;
    private Long setorDestinoId;
    private String setorDestinoNome;
    private StatusImpressora statusAplicado;
    private String responsavel;
    private String osQualycopy;
    private String descricao;
}
