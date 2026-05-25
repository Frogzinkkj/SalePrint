package com.gerenciador.impressora.controller;

import com.gerenciador.impressora.dto.ImpressoraDTO;
import com.gerenciador.impressora.dto.MovimentacaoRequestDTO;
import com.gerenciador.impressora.model.StatusImpressora;
import com.gerenciador.impressora.service.ImpressoraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/impressoras")
@RequiredArgsConstructor
public class ImpressoraController {

    private final ImpressoraService impressoraService;

    @GetMapping
    public List<ImpressoraDTO> listar(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) StatusImpressora status,
            @RequestParam(required = false) Long setorId,
            @RequestParam(required = false) String setor) {
        return impressoraService.listarComFiltros(busca, status, setorId, setor);
    }

    @GetMapping("/{id:\\d+}")
    public ImpressoraDTO buscar(@PathVariable Long id) {
        return impressoraService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ImpressoraDTO criar(@Valid @RequestBody ImpressoraDTO dto) {
        return impressoraService.criar(dto);
    }

    @PutMapping("/{id:\\d+}")
    public ImpressoraDTO atualizar(@PathVariable Long id, @Valid @RequestBody ImpressoraDTO dto) {
        return impressoraService.atualizar(id, dto);
    }

    @PostMapping("/movimentar")
    public ImpressoraDTO movimentar(@Valid @RequestBody MovimentacaoRequestDTO request) {
        return impressoraService.movimentar(request);
    }

    @DeleteMapping("/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        impressoraService.excluir(id);
    }
}
