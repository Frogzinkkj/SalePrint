package com.gerenciador.impressora.service;

import com.gerenciador.impressora.dto.DashboardStatsDTO;
import com.gerenciador.impressora.dto.ImpressoraDTO;
import com.gerenciador.impressora.dto.MovimentacaoRequestDTO;
import com.gerenciador.impressora.exception.BusinessException;
import com.gerenciador.impressora.mapper.EntityMapper;
import com.gerenciador.impressora.model.Impressora;
import com.gerenciador.impressora.model.Setor;
import com.gerenciador.impressora.model.StatusImpressora;
import com.gerenciador.impressora.repository.ImpressoraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImpressoraService {

    private final ImpressoraRepository impressoraRepository;
    private final SetorService setorService;
    private final MovimentacaoService movimentacaoService;
    private final GlpiIntegrationService glpiIntegrationService;

    @Transactional(readOnly = true)
    public List<ImpressoraDTO> listarTodas() {
        return impressoraRepository.findAll().stream().map(EntityMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ImpressoraDTO buscarPorId(Long id) {
        return EntityMapper.toDto(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ImpressoraDTO> buscarPorTermo(String termo) {
        if (termo == null || termo.isBlank()) {
            return listarTodas();
        }
        return impressoraRepository.buscarPorIpOuNumeroSerie(termo.trim())
                .stream().map(EntityMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ImpressoraDTO> filtrarPorStatus(StatusImpressora status) {
        return impressoraRepository.findByStatus(status).stream().map(EntityMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ImpressoraDTO> filtrarPorSetor(Long setorId) {
        return impressoraRepository.findBySetorId(setorId).stream().map(EntityMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ImpressoraDTO> listarComFiltros(String busca, StatusImpressora status, Long setorId, String setorNome) {
        String termo = textoFiltro(busca);
        String setor = textoFiltro(setorNome);

        return impressoraRepository.findAllComRelacoes().stream()
                .filter(i -> termo == null || correspondeTermo(i, termo))
                .filter(i -> status == null || i.getStatus() == status)
                .filter(i -> setorId == null || i.getSetor().getId().equals(setorId))
                .filter(i -> setor == null || correspondeSetor(i, setor))
                .map(EntityMapper::toDto)
                .toList();
    }

    private String textoFiltro(String valor) {
        return (valor != null && !valor.isBlank()) ? valor.trim().toLowerCase() : null;
    }

    private boolean correspondeTermo(Impressora i, String termo) {
        return i.getIp().toLowerCase().contains(termo)
                || i.getNumeroSerie().toLowerCase().contains(termo);
    }

    private boolean correspondeSetor(Impressora i, String termo) {
        return i.getSetor().getNome().toLowerCase().contains(termo)
                || i.getSetor().getLocalidade().getNome().toLowerCase().contains(termo);
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        return DashboardStatsDTO.builder()
                .totalAtivas(impressoraRepository.countByStatus(StatusImpressora.ATIVA))
                .totalManutencaoQualycopy(impressoraRepository.countByStatus(StatusImpressora.EM_MANUTENCAO_QUALYCOPY))
                .totalBackups(impressoraRepository.countByStatus(StatusImpressora.BACKUP))
                .totalProvisorias(impressoraRepository.countByStatus(StatusImpressora.PROVISORIA))
                .totalComDefeito(impressoraRepository.countByStatus(StatusImpressora.COM_DEFEITO_INTERNO))
                .totalRetiradas(impressoraRepository.countByStatus(StatusImpressora.RETIRADA))
                .totalGeral(impressoraRepository.count())
                .build();
    }

    @Transactional
    public ImpressoraDTO criar(ImpressoraDTO dto) {
        validarNumeroSerieUnico(dto.getNumeroSerie(), null);
        if (dto.getStatus() == StatusImpressora.ATIVA) {
            validarIpUnicoAtivo(dto.getIp(), null);
        }

        Setor setor = setorService.buscarEntidade(dto.getSetorId());
        Impressora entity = Impressora.builder()
                .marca(dto.getMarca().trim())
                .modelo(dto.getModelo().trim())
                .numeroSerie(dto.getNumeroSerie().trim())
                .ip(dto.getIp().trim())
                .status(dto.getStatus())
                .setor(setor)
                .observacao(dto.getObservacao() != null ? dto.getObservacao().trim() : null)
                .build();

        Impressora salva = impressoraRepository.save(entity);
        movimentacaoService.registrarMovimentacao(
                salva, null, setor, salva.getStatus(), "Sistema", null, "Cadastro inicial");

        if (salva.getStatus() == StatusImpressora.COM_DEFEITO_INTERNO) {
            glpiIntegrationService.abrirChamadoDefeito(salva);
        }

        return EntityMapper.toDto(salva);
    }

    /**
     * Cadastro em massa via CSV: sem integração GLPI (mais rápido).
     */
    @Transactional
    public ImpressoraDTO criarNaImportacao(ImpressoraDTO dto) {
        validarNumeroSerieUnico(dto.getNumeroSerie(), null);
        if (dto.getStatus() == StatusImpressora.ATIVA) {
            validarIpUnicoAtivo(dto.getIp(), null);
        }

        Setor setor = setorService.buscarEntidade(dto.getSetorId());
        Impressora entity = Impressora.builder()
                .marca(dto.getMarca().trim())
                .modelo(dto.getModelo().trim())
                .numeroSerie(dto.getNumeroSerie().trim())
                .ip(dto.getIp().trim())
                .status(dto.getStatus())
                .setor(setor)
                .observacao(dto.getObservacao() != null ? dto.getObservacao().trim() : null)
                .build();

        Impressora salva = impressoraRepository.save(entity);
        movimentacaoService.registrarMovimentacao(
                salva, null, setor, salva.getStatus(), "Importação CSV", null,
                dto.getObservacao() != null ? dto.getObservacao() : "Importação em lote");

        return EntityMapper.toDto(salva);
    }

    @Transactional
    public ImpressoraDTO atualizar(Long id, ImpressoraDTO dto) {
        Impressora entity = buscarEntidade(id);
        validarNumeroSerieUnico(dto.getNumeroSerie(), id);

        StatusImpressora statusAnterior = entity.getStatus();
        Setor setorAnterior = entity.getSetor();
        Setor novoSetor = setorService.buscarEntidade(dto.getSetorId());

        if (dto.getStatus() == StatusImpressora.ATIVA) {
            validarIpUnicoAtivo(dto.getIp(), id);
        }

        boolean mudouSetor = !Objects.equals(setorAnterior.getId(), novoSetor.getId());
        boolean mudouStatus = statusAnterior != dto.getStatus();

        entity.setMarca(dto.getMarca().trim());
        entity.setModelo(dto.getModelo().trim());
        entity.setNumeroSerie(dto.getNumeroSerie().trim());
        entity.setIp(dto.getIp().trim());
        entity.setStatus(dto.getStatus());
        entity.setSetor(novoSetor);
        entity.setObservacao(dto.getObservacao() != null ? dto.getObservacao().trim() : null);

        Impressora salva = impressoraRepository.save(entity);

        if (mudouSetor || mudouStatus) {
            movimentacaoService.registrarMovimentacao(
                    salva,
                    setorAnterior,
                    novoSetor,
                    salva.getStatus(),
                    "Sistema",
                    null,
                    "Atualização via API");
            if (salva.getStatus() == StatusImpressora.COM_DEFEITO_INTERNO && statusAnterior != StatusImpressora.COM_DEFEITO_INTERNO) {
                glpiIntegrationService.abrirChamadoDefeito(salva);
            }
        }

        return EntityMapper.toDto(salva);
    }

    @Transactional
    public ImpressoraDTO movimentar(MovimentacaoRequestDTO request) {
        Impressora entity = buscarEntidade(request.getImpressoraId());
        Setor setorAnterior = entity.getSetor();
        StatusImpressora statusAnterior = entity.getStatus();
        Setor novoSetor = setorService.buscarEntidade(request.getSetorDestinoId());

        if (request.getStatusAplicado() == StatusImpressora.ATIVA) {
            validarIpUnicoAtivo(entity.getIp(), entity.getId());
        }

        entity.setSetor(novoSetor);
        entity.setStatus(request.getStatusAplicado());
        Impressora salva = impressoraRepository.save(entity);

        movimentacaoService.registrarMovimentacao(
                salva,
                setorAnterior,
                novoSetor,
                request.getStatusAplicado(),
                request.getResponsavel(),
                request.getOsQualycopy(),
                request.getDescricao());

        if (request.getStatusAplicado() == StatusImpressora.COM_DEFEITO_INTERNO
                && statusAnterior != StatusImpressora.COM_DEFEITO_INTERNO) {
            glpiIntegrationService.abrirChamadoDefeito(salva);
        }

        return EntityMapper.toDto(salva);
    }

    @Transactional
    public void excluir(Long id) {
        if (!impressoraRepository.existsById(id)) {
            throw new BusinessException("Impressora não encontrada", HttpStatus.NOT_FOUND);
        }
        impressoraRepository.deleteById(id);
    }

    public Impressora buscarEntidade(Long id) {
        return impressoraRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Impressora não encontrada", HttpStatus.NOT_FOUND));
    }

    private void validarNumeroSerieUnico(String numeroSerie, Long id) {
        String sn = numeroSerie.trim();
        boolean exists = id == null
                ? impressoraRepository.existsByNumeroSerieIgnoreCase(sn)
                : impressoraRepository.existsByNumeroSerieIgnoreCaseAndIdNot(sn, id);
        if (exists) {
            throw new BusinessException("Número de série já cadastrado: " + sn, HttpStatus.CONFLICT);
        }
    }

    private void validarIpUnicoAtivo(String ip, Long id) {
        String ipTrim = ip.trim();
        var existente = id == null
                ? impressoraRepository.findAtivaByIp(ipTrim, StatusImpressora.ATIVA)
                : impressoraRepository.findAtivaByIpAndIdNot(ipTrim, StatusImpressora.ATIVA, id);
        if (existente.isPresent()) {
            throw new BusinessException("IP já utilizado por outra impressora ATIVA: " + ipTrim, HttpStatus.CONFLICT);
        }
    }
}
