package com.gerenciador.impressora.service;

import com.gerenciador.impressora.dto.LocalidadeDTO;
import com.gerenciador.impressora.exception.BusinessException;
import com.gerenciador.impressora.mapper.EntityMapper;
import com.gerenciador.impressora.model.Localidade;
import com.gerenciador.impressora.repository.LocalidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocalidadeService {

    private final LocalidadeRepository localidadeRepository;

    @Transactional(readOnly = true)
    public List<LocalidadeDTO> listarTodas() {
        return localidadeRepository.findAll().stream().map(EntityMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public LocalidadeDTO buscarPorId(Long id) {
        return EntityMapper.toDto(buscarEntidade(id));
    }

    @Transactional
    public LocalidadeDTO criar(LocalidadeDTO dto) {
        if (localidadeRepository.existsByNomeIgnoreCase(dto.getNome().trim())) {
            throw new BusinessException("Localidade já existe: " + dto.getNome(), HttpStatus.CONFLICT);
        }
        Localidade entity = Localidade.builder().nome(dto.getNome().trim()).build();
        return EntityMapper.toDto(localidadeRepository.save(entity));
    }

    @Transactional
    public LocalidadeDTO atualizar(Long id, LocalidadeDTO dto) {
        Localidade entity = buscarEntidade(id);
        String nome = dto.getNome().trim();
        localidadeRepository.findByNomeIgnoreCase(nome).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("Localidade já existe: " + nome, HttpStatus.CONFLICT);
            }
        });
        entity.setNome(nome);
        return EntityMapper.toDto(localidadeRepository.save(entity));
    }

    @Transactional
    public void excluir(Long id) {
        if (!localidadeRepository.existsById(id)) {
            throw new BusinessException("Localidade não encontrada", HttpStatus.NOT_FOUND);
        }
        localidadeRepository.deleteById(id);
    }

    public Localidade buscarEntidade(Long id) {
        return localidadeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Localidade não encontrada", HttpStatus.NOT_FOUND));
    }
}
