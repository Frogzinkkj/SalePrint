package com.gerenciador.impressora.service;

import com.gerenciador.impressora.dto.ImportacaoResultDTO;
import com.gerenciador.impressora.dto.ImpressoraDTO;
import com.gerenciador.impressora.exception.BusinessException;
import com.gerenciador.impressora.model.Localidade;
import com.gerenciador.impressora.model.Setor;
import com.gerenciador.impressora.model.StatusImpressora;
import com.gerenciador.impressora.repository.LocalidadeRepository;
import com.gerenciador.impressora.repository.SetorRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportacaoCsvService {

    private static final String SETOR_SEM_NOME = "(Sem setor)";

    private final ImportacaoTransacaoService importacaoTransacaoService;
    private final LocalidadeRepository localidadeRepository;
    private final SetorRepository setorRepository;

    private final Map<String, Long> cacheSetores = new ConcurrentHashMap<>();

    public ImportacaoResultDTO importar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo CSV é obrigatório", HttpStatus.BAD_REQUEST);
        }

        cacheSetores.clear();
        List<String> erros = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        int importadas = 0;

        byte[] conteudo;
        try {
            conteudo = file.getBytes();
        } catch (java.io.IOException e) {
            throw new BusinessException("Erro ao ler arquivo: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        Charset charset = detectarCharset(conteudo);
        String texto = new String(conteudo, charset);
        if (texto.startsWith("\uFEFF")) {
            texto = texto.substring(1);
        }

        String[] linhasTexto = texto.split("\\r\\n|\\n|\\r");
        if (linhasTexto.length == 0) {
            throw new BusinessException("Arquivo CSV vazio", HttpStatus.BAD_REQUEST);
        }

        char separador = detectarSeparador(linhasTexto);
        log.info("Importação CSV: charset={}, separador='{}', linhas brutas={}",
                charset.name(), separador, linhasTexto.length);

        List<String[]> linhasDados = new ArrayList<>();
        FormatoLinha formato = FormatoLinha.SIMPLES;
        boolean headerProcessado = false;

        for (String linhaTexto : linhasTexto) {
            if (linhaTexto == null || linhaTexto.isBlank()) {
                continue;
            }
            String[] row = parseLinha(linhaTexto, separador);
            if (linhaVazia(row)) {
                continue;
            }
            if (!headerProcessado) {
                if (isLinhaCabecalho(row)) {
                    formato = detectarFormato(row);
                    headerProcessado = true;
                    continue;
                }
                formato = isLinhaDadosPlanilha(row) ? FormatoLinha.PLANILHA : FormatoLinha.SIMPLES;
                headerProcessado = true;
            }
            linhasDados.add(row);
        }

        if (linhasDados.isEmpty()) {
            avisos.add("Nenhuma linha de dados foi reconhecida. Separador detectado: '"
                    + separador + "'. Confira se o arquivo é CSV/TSV exportado do Excel.");
        }

        log.info("Importação CSV: linhas de dados={}, formato={}", linhasDados.size(), formato);

        int linhaNum = 1;
        for (String[] row : linhasDados) {
            linhaNum++;
            try {
                ImpressoraDTO dto = formato == FormatoLinha.PLANILHA
                        ? parsePlanilha(row)
                        : parseSimples(row);
                importacaoTransacaoService.salvarLinha(dto);
                importadas++;
            } catch (Exception e) {
                String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                erros.add("Linha " + linhaNum + ": " + msg);
                log.warn("Erro linha {}: {}", linhaNum, msg);
            }
        }

        boolean sucesso = importadas > 0 && erros.isEmpty();
        if (importadas == 0 && erros.isEmpty()) {
            avisos.add("Nenhuma impressora foi importada. Verifique o formato do arquivo.");
        }

        return ImportacaoResultDTO.builder()
                .sucesso(sucesso)
                .totalLinhasArquivo(linhasTexto.length)
                .totalLinhasDados(linhasDados.size())
                .totalImportadas(importadas)
                .totalErros(erros.size())
                .erros(erros)
                .avisos(avisos)
                .build();
    }

    private enum FormatoLinha {
        PLANILHA,
        SIMPLES
    }

    private ImpressoraDTO parsePlanilha(String[] row) {
        if (row.length < 6) {
            throw new BusinessException(
                    "Colunas insuficientes (" + row.length + "). Esperado: Localidade, Setor, Marca, Modelo, IP, S/N...",
                    HttpStatus.BAD_REQUEST);
        }

        String localidadeNome = col(row, 0);
        String setorNome = col(row, 1);
        String marca = col(row, 2);
        String modelo = col(row, 3);
        String ip = col(row, 4);
        String numeroSerie = col(row, 5);
        String statusTexto = row.length > 6 ? col(row, 6) : "Ativa";
        String observacao = montarObservacao(row);

        validarCamposObrigatorios(localidadeNome, marca, modelo, numeroSerie);

        if (setorNome.isBlank()) {
            setorNome = SETOR_SEM_NOME;
        }

        Long setorId = resolverSetorId(setorNome, localidadeNome);
        StatusImpressora status = mapearStatus(statusTexto, setorNome);

        ImpressoraDTO dto = new ImpressoraDTO();
        dto.setMarca(marca);
        dto.setModelo(modelo);
        dto.setNumeroSerie(numeroSerie);
        dto.setIp(resolverIp(ip, numeroSerie));
        dto.setStatus(status);
        dto.setSetorId(setorId);
        dto.setObservacao(observacao.isBlank() ? null : observacao);
        return dto;
    }

    private ImpressoraDTO parseSimples(String[] row) {
        if (row.length < 6) {
            throw new BusinessException("Colunas insuficientes (" + row.length + ")", HttpStatus.BAD_REQUEST);
        }

        String marca = col(row, 0);
        String modelo = col(row, 1);
        String numeroSerie = col(row, 2);
        String ip = col(row, 3);
        String setorNome = col(row, 4);
        String localidadeNome = col(row, 5);
        String observacao = row.length > 6 ? col(row, 6) : "";

        validarCamposObrigatorios(localidadeNome, marca, modelo, numeroSerie);

        if (setorNome.isBlank()) {
            setorNome = SETOR_SEM_NOME;
        }

        ImpressoraDTO dto = new ImpressoraDTO();
        dto.setMarca(marca);
        dto.setModelo(modelo);
        dto.setNumeroSerie(numeroSerie);
        dto.setIp(resolverIp(ip, numeroSerie));
        dto.setStatus(StatusImpressora.ATIVA);
        dto.setSetorId(resolverSetorId(setorNome, localidadeNome));
        dto.setObservacao(observacao.isBlank() ? null : observacao);
        return dto;
    }

    private Long resolverSetorId(String setorNome, String localidadeNome) {
        String chave = normalizar(localidadeNome) + "|" + normalizar(setorNome);
        return cacheSetores.computeIfAbsent(chave, k -> {
            Localidade localidade = resolverLocalidade(localidadeNome);
            return setorRepository.findByNomeIgnoreCaseAndLocalidadeId(setorNome.trim(), localidade.getId())
                    .orElseGet(() -> setorRepository.save(Setor.builder()
                            .nome(setorNome.trim())
                            .localidade(localidade)
                            .build()))
                    .getId();
        });
    }

    private String montarObservacao(String[] row) {
        StringBuilder sb = new StringBuilder();
        if (row.length > 8) {
            appendObs(sb, col(row, 8));
        }
        if (row.length > 7) {
            String dataStatus = col(row, 7);
            if (!dataStatus.isBlank()) {
                if (!sb.isEmpty()) {
                    sb.append(" | ");
                }
                sb.append("Data status: ").append(dataStatus);
            }
        }
        return sb.toString().trim();
    }

    private void appendObs(StringBuilder sb, String parte) {
        if (parte != null && !parte.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" | ");
            }
            sb.append(parte.trim());
        }
    }

    private void validarCamposObrigatorios(String localidade, String marca, String modelo, String numeroSerie) {
        if (localidade.isBlank()) {
            throw new BusinessException("Localidade vazia", HttpStatus.BAD_REQUEST);
        }
        if (marca.isBlank()) {
            throw new BusinessException("Marca vazia", HttpStatus.BAD_REQUEST);
        }
        if (modelo.isBlank()) {
            throw new BusinessException("Modelo vazio", HttpStatus.BAD_REQUEST);
        }
        if (numeroSerie.isBlank()) {
            throw new BusinessException("Número de série (S/N) vazio", HttpStatus.BAD_REQUEST);
        }
    }

    private String resolverIp(String ip, String numeroSerie) {
        if (ip != null && !ip.isBlank()) {
            return ip.trim();
        }
        return "SEM-IP-" + numeroSerie.trim().toUpperCase(Locale.ROOT);
    }

    private StatusImpressora mapearStatus(String statusTexto, String setorNome) {
        String normalizado = normalizar(statusTexto);
        String setorNorm = normalizar(setorNome);

        return switch (normalizado) {
            case "ativa" -> StatusImpressora.ATIVA;
            case "defeito", "com defeito", "com defeito interno" -> StatusImpressora.COM_DEFEITO_INTERNO;
            case "manutencao", "em manutencao" -> StatusImpressora.EM_MANUTENCAO_QUALYCOPY;
            case "provisoria" -> StatusImpressora.PROVISORIA;
            case "backup" -> StatusImpressora.BACKUP;
            case "retirada", "substituida", "substituita" -> StatusImpressora.RETIRADA;
            default -> {
                if (setorNorm.contains("backup")) {
                    yield StatusImpressora.BACKUP;
                }
                if (setorNorm.contains("retirada")) {
                    yield StatusImpressora.RETIRADA;
                }
                yield StatusImpressora.ATIVA;
            }
        };
    }

    private Localidade resolverLocalidade(String localidadeNome) {
        String nome = localidadeNome.trim();
        return localidadeRepository.findByNomeIgnoreCase(nome)
                .orElseGet(() -> {
                    String alternativo = normalizarLocalidade(nome);
                    return localidadeRepository.findByNomeIgnoreCase(alternativo)
                            .orElseThrow(() -> new BusinessException(
                                    "Localidade não encontrada: " + nome
                                            + ". Use: Dom Bosco ou Camaçari",
                                    HttpStatus.BAD_REQUEST));
                });
    }

    private String normalizarLocalidade(String nome) {
        String n = normalizar(nome);
        if (n.contains("camacari") || n.contains("camaçari")) {
            return "Camaçari";
        }
        if (n.contains("dom bosco")) {
            return "Dom Bosco";
        }
        return nome.trim();
    }

    private FormatoLinha detectarFormato(String[] header) {
        String joined = normalizar(String.join(" ", header));
        if (joined.contains("setor") && (joined.contains("s/n") || joined.contains("sn"))) {
            return FormatoLinha.PLANILHA;
        }
        if (joined.contains("marca") && joined.contains("modelo") && joined.contains("ip")) {
            String c0 = normalizar(col(header, 0));
            if (c0.isBlank() || c0.contains("dom bosco") || c0.contains("camacari") || c0.contains("local")) {
                return FormatoLinha.PLANILHA;
            }
        }
        String c0 = col(header, 0);
        String c1 = col(header, 1);
        if (!c0.isBlank() && (c0.equalsIgnoreCase("Dom Bosco") || c0.equalsIgnoreCase("Camaçari"))) {
            return FormatoLinha.PLANILHA;
        }
        if (c1.toLowerCase(Locale.ROOT).contains("setor")) {
            return FormatoLinha.PLANILHA;
        }
        return FormatoLinha.SIMPLES;
    }

    private boolean isLinhaDadosPlanilha(String[] row) {
        if (row.length < 6) {
            return false;
        }
        String loc = normalizar(col(row, 0));
        String marca = normalizar(col(row, 2));
        boolean locOk = loc.contains("dom bosco") || loc.contains("camacari");
        boolean marcaOk = marca.equals("canon") || marca.equals("epson") || marca.equals("brother")
                || marca.equals("hp") || marca.equals("xerox");
        return locOk && marcaOk;
    }

    private boolean isLinhaCabecalho(String[] row) {
        String joined = normalizar(String.join(" ", row));
        return joined.contains("marca") && joined.contains("modelo")
                || joined.contains("setor") && joined.contains("s/n")
                || joined.contains("setor") && joined.contains("sn")
                || joined.contains("numero") && joined.contains("serie");
    }

    private char detectarSeparador(String[] linhas) {
        int tabs = 0, virgulas = 0, pontoVirgula = 0;
        int amostras = Math.min(5, linhas.length);
        for (int i = 0; i < amostras; i++) {
            if (linhas[i] == null || linhas[i].isBlank()) {
                continue;
            }
            tabs += countChar(linhas[i], '\t');
            virgulas += countChar(linhas[i], ',');
            pontoVirgula += countChar(linhas[i], ';');
        }
        if (tabs >= virgulas && tabs >= pontoVirgula && tabs > 0) {
            return '\t';
        }
        if (pontoVirgula > virgulas) {
            return ';';
        }
        return ',';
    }

    private int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    private String[] parseLinha(String linha, char separador) {
        try {
            CSVParser parser = new CSVParserBuilder().withSeparator(separador).build();
            return parser.parseLine(linha);
        } catch (Exception e) {
            return linha.split(PatternQuote(separador));
        }
    }

    private String PatternQuote(char separador) {
        if (separador == '\t') {
            return "\t";
        }
        return String.valueOf(separador);
    }

    private Charset detectarCharset(byte[] conteudo) {
        if (conteudo.length >= 2 && conteudo[0] == (byte) 0xFF && conteudo[1] == (byte) 0xFE) {
            return StandardCharsets.UTF_16LE;
        }
        if (conteudo.length >= 3 && conteudo[0] == (byte) 0xEF && conteudo[1] == (byte) 0xBB && conteudo[2] == (byte) 0xBF) {
            return StandardCharsets.UTF_8;
        }
        int len = Math.min(conteudo.length, 8192);
        String utf8 = new String(conteudo, 0, len, StandardCharsets.UTF_8);
        if (utf8.contains("Dom Bosco") || utf8.contains("Cama")) {
            return StandardCharsets.UTF_8;
        }
        try {
            return Charset.forName("Windows-1252");
        } catch (Exception e) {
            return StandardCharsets.UTF_8;
        }
    }

    private boolean linhaVazia(String[] row) {
        if (row == null || row.length == 0) {
            return true;
        }
        for (String cell : row) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private String col(String[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return "";
        }
        return row[index].trim();
    }

    private String normalizar(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("á", "a")
                .replace("à", "a")
                .replace("â", "a")
                .replace("ã", "a")
                .replace("é", "e")
                .replace("ê", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ô", "o")
                .replace("õ", "o")
                .replace("ú", "u")
                .replace("ç", "c");
    }
}
