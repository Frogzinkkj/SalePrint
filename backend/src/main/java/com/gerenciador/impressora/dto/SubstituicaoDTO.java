package com.gerenciador.impressora.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubstituicaoDTO {
    private Long id;
    private LocalDateTime dataSubstituicao;
    private String responsavel;
    private String observacao;

    private Long impressoraAntigaId;
    private String antigaMarca;
    private String antigaModelo;
    private String antigaNumeroSerie;
    private String antigaIp;
    private String antigaSetorNome;

    private Long impressoraNovaId;
    private String novaMarca;
    private String novaModelo;
    private String novaNumeroSerie;
    private String novaIp;
    private String novaSetorNome;
}
