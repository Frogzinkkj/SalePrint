package com.gerenciador.impressora.controller;

import com.gerenciador.impressora.dto.SetorDTO;
import com.gerenciador.impressora.service.SetorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/setores")
@RequiredArgsConstructor
public class SetorController {

    private final SetorService setorService;

    @GetMapping
    public List<SetorDTO> listar() {
        return setorService.listarTodos();
    }

    @GetMapping("/localidade/{localidadeId}")
    public List<SetorDTO> listarPorLocalidade(@PathVariable Long localidadeId) {
        return setorService.listarPorLocalidade(localidadeId);
    }

    @GetMapping("/{id}")
    public SetorDTO buscar(@PathVariable Long id) {
        return setorService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SetorDTO criar(@Valid @RequestBody SetorDTO dto) {
        return setorService.criar(dto);
    }

    @PutMapping("/{id}")
    public SetorDTO atualizar(@PathVariable Long id, @Valid @RequestBody SetorDTO dto) {
        return setorService.atualizar(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        setorService.excluir(id);
    }
}
