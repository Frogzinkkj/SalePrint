package com.gerenciador.impressora.controller;

import com.gerenciador.impressora.dto.DashboardStatsDTO;
import com.gerenciador.impressora.service.ImpressoraService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ImpressoraService impressoraService;

    @GetMapping
    public DashboardStatsDTO estatisticas() {
        return impressoraService.getDashboardStats();
    }
}
