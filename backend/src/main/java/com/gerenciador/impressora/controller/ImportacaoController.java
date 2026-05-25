package com.gerenciador.impressora.controller;

import com.gerenciador.impressora.dto.ImportacaoResultDTO;
import com.gerenciador.impressora.service.ImportacaoCsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/importacao")
@RequiredArgsConstructor
public class ImportacaoController {

    private final ImportacaoCsvService importacaoCsvService;

    @PostMapping("/csv")
    public ImportacaoResultDTO importarCsv(@RequestParam("file") MultipartFile file) {
        return importacaoCsvService.importar(file);
    }
}
