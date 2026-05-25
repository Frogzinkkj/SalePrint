package com.gerenciador.impressora.dto;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ImportacaoResultDTO {
    private boolean sucesso;
    private int totalLinhasArquivo;
    private int totalLinhasDados;
    private int totalImportadas;
    private int totalErros;
    @Builder.Default
    private List<String> erros = new ArrayList<>();
    @Builder.Default
    private List<String> avisos = new ArrayList<>();
}
