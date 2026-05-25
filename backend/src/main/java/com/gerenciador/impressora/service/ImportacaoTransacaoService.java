package com.gerenciador.impressora.service;

import com.gerenciador.impressora.dto.ImpressoraDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImportacaoTransacaoService {

    private final ImpressoraService impressoraService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void salvarLinha(ImpressoraDTO dto) {
        impressoraService.criarNaImportacao(dto);
    }
}
