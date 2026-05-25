package com.gerenciador.impressora.controller;

import com.gerenciador.impressora.dto.SubstituicaoDTO;
import com.gerenciador.impressora.dto.SubstituicaoRequestDTO;
import com.gerenciador.impressora.service.SubstituicaoImpressoraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/substituicoes")
@RequiredArgsConstructor
public class SubstituicaoController {

    private final SubstituicaoImpressoraService substituicaoService;

    @GetMapping
    public List<SubstituicaoDTO> listar() {
        return substituicaoService.listarTodas();
    }

    @GetMapping("/{id}")
    public SubstituicaoDTO buscar(@PathVariable Long id) {
        return substituicaoService.buscarPorId(id);
    }

    @GetMapping("/impressora/{impressoraId}")
    public List<SubstituicaoDTO> listarPorImpressora(@PathVariable Long impressoraId) {
        return substituicaoService.listarPorImpressora(impressoraId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubstituicaoDTO registrar(@Valid @RequestBody SubstituicaoRequestDTO request) {
        return substituicaoService.registrar(request);
    }
}
