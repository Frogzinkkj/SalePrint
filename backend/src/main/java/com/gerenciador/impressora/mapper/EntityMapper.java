package com.gerenciador.impressora.mapper;

import com.gerenciador.impressora.dto.*;
import com.gerenciador.impressora.model.Impressora;
import com.gerenciador.impressora.model.Localidade;
import com.gerenciador.impressora.model.Movimentacao;
import com.gerenciador.impressora.model.Setor;
import com.gerenciador.impressora.model.SubstituicaoImpressora;

public final class EntityMapper {

    private EntityMapper() {
    }

    public static LocalidadeDTO toDto(Localidade entity) {
        LocalidadeDTO dto = new LocalidadeDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        return dto;
    }

    public static SetorDTO toDto(Setor entity) {
        SetorDTO dto = new SetorDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setLocalidadeId(entity.getLocalidade().getId());
        dto.setLocalidadeNome(entity.getLocalidade().getNome());
        return dto;
    }

    public static ImpressoraDTO toDto(Impressora entity) {
        ImpressoraDTO dto = new ImpressoraDTO();
        dto.setId(entity.getId());
        dto.setMarca(entity.getMarca());
        dto.setModelo(entity.getModelo());
        dto.setNumeroSerie(entity.getNumeroSerie());
        dto.setIp(entity.getIp());
        dto.setStatus(entity.getStatus());
        dto.setSetorId(entity.getSetor().getId());
        dto.setSetorNome(entity.getSetor().getNome());
        dto.setLocalidadeNome(entity.getSetor().getLocalidade().getNome());
        dto.setObservacao(entity.getObservacao());
        return dto;
    }

    public static MovimentacaoDTO toDto(Movimentacao entity) {
        MovimentacaoDTO dto = new MovimentacaoDTO();
        dto.setId(entity.getId());
        dto.setImpressoraId(entity.getImpressora().getId());
        dto.setDataMovimentacao(entity.getDataMovimentacao());
        if (entity.getSetorOrigem() != null) {
            dto.setSetorOrigemId(entity.getSetorOrigem().getId());
            dto.setSetorOrigemNome(entity.getSetorOrigem().getNome());
        }
        dto.setSetorDestinoId(entity.getSetorDestino().getId());
        dto.setSetorDestinoNome(entity.getSetorDestino().getNome());
        dto.setStatusAplicado(entity.getStatusAplicado());
        dto.setResponsavel(entity.getResponsavel());
        dto.setOsQualycopy(entity.getOsQualycopy());
        dto.setDescricao(entity.getDescricao());
        return dto;
    }

    public static SubstituicaoDTO toDto(SubstituicaoImpressora entity) {
        SubstituicaoDTO dto = new SubstituicaoDTO();
        dto.setId(entity.getId());
        dto.setDataSubstituicao(entity.getDataSubstituicao());
        dto.setResponsavel(entity.getResponsavel());
        dto.setObservacao(entity.getObservacao());

        Impressora antiga = entity.getImpressoraAntiga();
        dto.setImpressoraAntigaId(antiga.getId());
        dto.setAntigaMarca(antiga.getMarca());
        dto.setAntigaModelo(antiga.getModelo());
        dto.setAntigaNumeroSerie(antiga.getNumeroSerie());
        dto.setAntigaIp(antiga.getIp());
        dto.setAntigaSetorNome(antiga.getSetor().getNome());

        Impressora nova = entity.getImpressoraNova();
        dto.setImpressoraNovaId(nova.getId());
        dto.setNovaMarca(nova.getMarca());
        dto.setNovaModelo(nova.getModelo());
        dto.setNovaNumeroSerie(nova.getNumeroSerie());
        dto.setNovaIp(nova.getIp());
        dto.setNovaSetorNome(nova.getSetor().getNome());
        return dto;
    }
}
