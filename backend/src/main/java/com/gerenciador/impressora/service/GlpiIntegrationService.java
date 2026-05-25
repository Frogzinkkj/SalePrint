package com.gerenciador.impressora.service;

import com.gerenciador.impressora.model.Impressora;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
@Slf4j
public class GlpiIntegrationService {

    private final RestClient restClient;

    @Value("${glpi.enabled:false}")
    private boolean enabled;

    @Value("${glpi.url:}")
    private String glpiUrl;

    public GlpiIntegrationService() {
        this.restClient = RestClient.create();
    }

    public void abrirChamadoDefeito(Impressora impressora) {
        if (!enabled) {
            log.info("GLPI desabilitado. Chamado não enviado para impressora id={}", impressora.getId());
            return;
        }

        String setorNome = impressora.getSetor().getNome();
        String localidade = impressora.getSetor().getLocalidade().getNome();
        String titulo = "Impressora com defeito interno";
        String conteudo = String.format(
                "Chamado automático - Gerenciador de Impressoras%n" +
                        "IP: %s%nS/N: %s%nMarca/Modelo: %s %s%nSetor: %s (%s)",
                impressora.getIp(),
                impressora.getNumeroSerie(),
                impressora.getMarca(),
                impressora.getModelo(),
                setorNome,
                localidade);

        Map<String, Object> payload = Map.of(
                "input", Map.of(
                        "name", titulo,
                        "content", conteudo,
                        "type", 1,
                        "itilcategories_id", 0));

        try {
            restClient.post()
                    .uri(glpiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Chamado GLPI enviado para impressora id={}", impressora.getId());
        } catch (Exception e) {
            log.error("Falha ao enviar chamado GLPI: {}", e.getMessage());
        }
    }
}
