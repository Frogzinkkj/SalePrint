package com.gerenciador.impressora.service;

import com.gerenciador.impressora.dto.MovimentacaoDTO;
import com.gerenciador.impressora.exception.BusinessException;
import com.gerenciador.impressora.mapper.EntityMapper;
import com.gerenciador.impressora.model.Impressora;
import com.gerenciador.impressora.model.Movimentacao;
import com.gerenciador.impressora.model.Setor;
import com.gerenciador.impressora.model.StatusImpressora;
import com.gerenciador.impressora.repository.MovimentacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;

    @Transactional
    public MovimentacaoDTO registrarMovimentacao(
            Impressora impressora,
            Setor setorOrigem,
            Setor setorDestino,
            StatusImpressora statusAplicado,
            String responsavel,
            String osQualycopy,
            String descricao) {

        Movimentacao movimentacao = Movimentacao.builder()
                .impressora(impressora)
                .dataMovimentacao(LocalDateTime.now())
                .setorOrigem(setorOrigem)
                .setorDestino(setorDestino)
                .statusAplicado(statusAplicado)
                .responsavel(responsavel.trim())
                .osQualycopy(osQualycopy != null ? osQualycopy.trim() : null)
                .descricao(descricao != null ? descricao.trim() : null)
                .build();

        return EntityMapper.toDto(movimentacaoRepository.save(movimentacao));
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoDTO> historicoPorImpressora(Long impressoraId) {
        return movimentacaoRepository.findByImpressoraIdOrderByDataMovimentacaoDesc(impressoraId)
                .stream().map(EntityMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public MovimentacaoDTO ultimaMovimentacao(Long impressoraId) {
        return movimentacaoRepository.findFirstByImpressoraIdOrderByDataMovimentacaoDesc(impressoraId)
                .map(EntityMapper::toDto)
                .orElseThrow(() -> new BusinessException("Nenhuma movimentação encontrada", HttpStatus.NOT_FOUND));
    }
}
