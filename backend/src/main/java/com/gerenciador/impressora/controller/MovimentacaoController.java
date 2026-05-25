package com.gerenciador.impressora.controller;

import com.gerenciador.impressora.dto.MovimentacaoDTO;
import com.gerenciador.impressora.service.MovimentacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movimentacoes")
@RequiredArgsConstructor
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @GetMapping("/historico/{impressoraId}")
    public List<MovimentacaoDTO> historico(@PathVariable Long impressoraId) {
        return movimentacaoService.historicoPorImpressora(impressoraId);
    }

    @GetMapping("/ultima/{impressoraId}")
    public MovimentacaoDTO ultima(@PathVariable Long impressoraId) {
        return movimentacaoService.ultimaMovimentacao(impressoraId);
    }
}
