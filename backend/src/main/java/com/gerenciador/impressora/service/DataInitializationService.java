package com.gerenciador.impressora.service;

import com.gerenciador.impressora.model.Localidade;
import com.gerenciador.impressora.model.Setor;
import com.gerenciador.impressora.repository.LocalidadeRepository;
import com.gerenciador.impressora.repository.SetorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializationService implements ApplicationRunner {

    private final LocalidadeRepository localidadeRepository;
    private final SetorRepository setorRepository;

    private static final List<String> LOCALIDADES = List.of("Dom Bosco", "Camaçari");
    private static final List<String> SETORES = List.of(
            "Tesouraria", "Secretaria", "RH", "Financeiro", "Tecnologia da Informação", "Diretoria");

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String nomeLocalidade : LOCALIDADES) {
            Localidade localidade = localidadeRepository.findByNomeIgnoreCase(nomeLocalidade)
                    .orElseGet(() -> localidadeRepository.save(
                            Localidade.builder().nome(nomeLocalidade).build()));

            for (String nomeSetor : SETORES) {
                setorRepository.findByNomeIgnoreCaseAndLocalidadeId(nomeSetor, localidade.getId())
                        .orElseGet(() -> setorRepository.save(Setor.builder()
                                .nome(nomeSetor)
                                .localidade(localidade)
                                .build()));
            }
        }
    }
}
