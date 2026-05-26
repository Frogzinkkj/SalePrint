-- Tabela de Localidades
CREATE TABLE IF NOT EXISTS localidades (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE
);

-- Tabela de Setores
CREATE TABLE IF NOT EXISTS setores (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    localidade_id BIGINT NOT NULL,
    FOREIGN KEY (localidade_id) REFERENCES localidades(id) ON DELETE CASCADE
);

-- Tabela de Impressoras
CREATE TABLE IF NOT EXISTS impressoras (
    id BIGSERIAL PRIMARY KEY,
    marca VARCHAR(255) NOT NULL,
    modelo VARCHAR(255) NOT NULL,
    numero_serie VARCHAR(255) NOT NULL UNIQUE,
    ip VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    setor_id BIGINT NOT NULL,
    observacao TEXT,
    FOREIGN KEY (setor_id) REFERENCES setores(id) ON DELETE CASCADE
);

-- Tabela de Movimentações
CREATE TABLE IF NOT EXISTS movimentacoes (
    id BIGSERIAL PRIMARY KEY,
    impressora_id BIGINT NOT NULL,
    data_movimentacao TIMESTAMP NOT NULL,
    setor_origem_id BIGINT,
    setor_destino_id BIGINT NOT NULL,
    status_aplicado VARCHAR(50) NOT NULL,
    responsavel VARCHAR(255) NOT NULL,
    os_qualycopy VARCHAR(255),
    observacao TEXT,
    descricao TEXT,
    FOREIGN KEY (impressora_id) REFERENCES impressoras(id) ON DELETE CASCADE,
    FOREIGN KEY (setor_origem_id) REFERENCES setores(id) ON DELETE SET NULL,
    FOREIGN KEY (setor_destino_id) REFERENCES setores(id) ON DELETE CASCADE
);

-- Tabela de Substituições
CREATE TABLE IF NOT EXISTS substituicoes_impressora (
    id BIGSERIAL PRIMARY KEY,
    impressora_antiga_id BIGINT NOT NULL,
    impressora_nova_id BIGINT NOT NULL,
    data_substituicao TIMESTAMP NOT NULL,
    responsavel VARCHAR(255) NOT NULL,
    observacao TEXT,
    FOREIGN KEY (impressora_antiga_id) REFERENCES impressoras(id) ON DELETE CASCADE,
    FOREIGN KEY (impressora_nova_id) REFERENCES impressoras(id) ON DELETE CASCADE
);

-- Índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_setores_localidade_id ON setores(localidade_id);
CREATE INDEX IF NOT EXISTS idx_impressoras_setor_id ON impressoras(setor_id);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_impressora_id ON movimentacoes(impressora_id);
CREATE INDEX IF NOT EXISTS idx_movimentacoes_data ON movimentacoes(data_movimentacao);
CREATE INDEX IF NOT EXISTS idx_substituicoes_impressora_antiga ON substituicoes_impressora(impressora_antiga_id);
CREATE INDEX IF NOT EXISTS idx_substituicoes_impressora_nova ON substituicoes_impressora(impressora_nova_id);
