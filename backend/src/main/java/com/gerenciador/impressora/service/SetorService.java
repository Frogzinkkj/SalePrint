package com.gerenciador.impressora.service;

import com.gerenciador.impressora.dto.SetorDTO;
import com.gerenciador.impressora.exception.BusinessException;
import com.gerenciador.impressora.mapper.EntityMapper;
import com.gerenciador.impressora.model.Localidade;
import com.gerenciador.impressora.model.Setor;
import com.gerenciador.impressora.repository.SetorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SetorService {

    private final SetorRepository setorRepository;
    private final LocalidadeService localidadeService;

    @Transactional(readOnly = true)
    public List<SetorDTO> listarTodos() {
        return setorRepository.findAll().stream().map(EntityMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<SetorDTO> listarPorLocalidade(Long localidadeId) {
        return setorRepository.findByLocalidadeIdOrderByNomeAsc(localidadeId)
                .stream().map(EntityMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SetorDTO buscarPorId(Long id) {
        return EntityMapper.toDto(buscarEntidade(id));
    }

    @Transactional
    public SetorDTO criar(SetorDTO dto) {
        Localidade localidade = localidadeService.buscarEntidade(dto.getLocalidadeId());
        Setor entity = Setor.builder()
                .nome(dto.getNome().trim())
                .localidade(localidade)
                .build();
        return EntityMapper.toDto(setorRepository.save(entity));
    }

    @Transactional
    public SetorDTO atualizar(Long id, SetorDTO dto) {
        Setor entity = buscarEntidade(id);
        Localidade localidade = localidadeService.buscarEntidade(dto.getLocalidadeId());
        entity.setNome(dto.getNome().trim());
        entity.setLocalidade(localidade);
        return EntityMapper.toDto(setorRepository.save(entity));
    }

    @Transactional
    public void excluir(Long id) {
        if (!setorRepository.existsById(id)) {
            throw new BusinessException("Setor não encontrado", HttpStatus.NOT_FOUND);
        }
        setorRepository.deleteById(id);
    }

    public Setor buscarEntidade(Long id) {
        return setorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Setor não encontrado", HttpStatus.NOT_FOUND));
    }
}
