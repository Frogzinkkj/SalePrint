package com.gerenciador.impressora.service;

import com.gerenciador.impressora.dto.SubstituicaoDTO;
import com.gerenciador.impressora.dto.SubstituicaoRequestDTO;
import com.gerenciador.impressora.exception.BusinessException;
import com.gerenciador.impressora.mapper.EntityMapper;
import com.gerenciador.impressora.model.Impressora;
import com.gerenciador.impressora.model.StatusImpressora;
import com.gerenciador.impressora.model.SubstituicaoImpressora;
import com.gerenciador.impressora.repository.ImpressoraRepository;
import com.gerenciador.impressora.repository.SubstituicaoImpressoraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SubstituicaoImpressoraService {

    private final SubstituicaoImpressoraRepository substituicaoRepository;
    private final ImpressoraRepository impressoraRepository;
    private final MovimentacaoService movimentacaoService;

    @Transactional(readOnly = true)
    public List<SubstituicaoDTO> listarTodas() {
        return substituicaoRepository.findAllComDetalhes().stream()
                .map(EntityMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubstituicaoDTO> listarPorImpressora(Long impressoraId) {
        return substituicaoRepository.findByImpressoraId(impressoraId).stream()
                .map(EntityMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SubstituicaoDTO buscarPorId(Long id) {
        return substituicaoRepository.findByIdComDetalhes(id)
                .map(EntityMapper::toDto)
                .orElseThrow(() -> new BusinessException("Substituição não encontrada", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public SubstituicaoDTO registrar(SubstituicaoRequestDTO request) {
        Impressora antiga = resolverImpressora(request.getImpressoraAntigaId(), request.getNumeroSerieAntiga(), "antiga");
        Impressora nova = resolverImpressora(request.getImpressoraNovaId(), request.getNumeroSerieNova(), "nova");

        if (Objects.equals(antiga.getId(), nova.getId())) {
            throw new BusinessException("Impressora antiga e nova devem ser diferentes", HttpStatus.BAD_REQUEST);
        }

        SubstituicaoImpressora substituicao = SubstituicaoImpressora.builder()
                .impressoraAntiga(antiga)
                .impressoraNova(nova)
                .dataSubstituicao(LocalDateTime.now())
                .responsavel(request.getResponsavel().trim())
                .observacao(request.getObservacao() != null ? request.getObservacao().trim() : null)
                .build();

        SubstituicaoImpressora salva = substituicaoRepository.save(substituicao);

        if (Boolean.TRUE.equals(request.getAtualizarStatus())) {
            aplicarStatusSubstituicao(antiga, nova, request.getResponsavel());
        }

        return substituicaoRepository.findByIdComDetalhes(salva.getId())
                .map(EntityMapper::toDto)
                .orElseThrow(() -> new BusinessException("Erro ao carregar substituição salva", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private void aplicarStatusSubstituicao(Impressora antiga, Impressora nova, String responsavel) {
        if (antiga.getStatus() != StatusImpressora.RETIRADA) {
            antiga.setStatus(StatusImpressora.RETIRADA);
            impressoraRepository.save(antiga);
            movimentacaoService.registrarMovimentacao(
                    antiga,
                    antiga.getSetor(),
                    antiga.getSetor(),
                    StatusImpressora.RETIRADA,
                    responsavel,
                    null,
                    "Substituída por S/N " + nova.getNumeroSerie());
        }

        if (nova.getStatus() != StatusImpressora.ATIVA) {
            nova.setStatus(StatusImpressora.ATIVA);
            impressoraRepository.save(nova);
            movimentacaoService.registrarMovimentacao(
                    nova,
                    nova.getSetor(),
                    nova.getSetor(),
                    StatusImpressora.ATIVA,
                    responsavel,
                    null,
                    "Substituiu S/N " + antiga.getNumeroSerie());
        }
    }

    private Impressora resolverImpressora(Long id, String numeroSerie, String papel) {
        if (id != null) {
            return impressoraRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(
                            "Impressora " + papel + " não encontrada (id=" + id + ")", HttpStatus.NOT_FOUND));
        }
        if (numeroSerie != null && !numeroSerie.isBlank()) {
            return impressoraRepository.findByNumeroSerieIgnoreCase(numeroSerie.trim())
                    .orElseThrow(() -> new BusinessException(
                            "Impressora " + papel + " não encontrada (S/N=" + numeroSerie + ")", HttpStatus.NOT_FOUND));
        }
        throw new BusinessException(
                "Informe o id ou número de série da impressora " + papel, HttpStatus.BAD_REQUEST);
    }
}
