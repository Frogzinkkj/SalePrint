package com.gerenciador.impressora.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalAtivas;
    private long totalManutencaoQualycopy;
    private long totalBackups;
    private long totalProvisorias;
    private long totalComDefeito;
    private long totalRetiradas;
    private long totalGeral;
    private long totalModelos;
}
