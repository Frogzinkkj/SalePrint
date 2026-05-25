package com.gerenciador.impressora.controller;

import com.gerenciador.impressora.dto.LocalidadeDTO;
import com.gerenciador.impressora.service.LocalidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/localidades")
@RequiredArgsConstructor
public class LocalidadeController {

    private final LocalidadeService localidadeService;

    @GetMapping
    public List<LocalidadeDTO> listar() {
        return localidadeService.listarTodas();
    }

    @GetMapping("/{id}")
    public LocalidadeDTO buscar(@PathVariable Long id) {
        return localidadeService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocalidadeDTO criar(@Valid @RequestBody LocalidadeDTO dto) {
        return localidadeService.criar(dto);
    }

    @PutMapping("/{id}")
    public LocalidadeDTO atualizar(@PathVariable Long id, @Valid @RequestBody LocalidadeDTO dto) {
        return localidadeService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        localidadeService.excluir(id);
    }
}
