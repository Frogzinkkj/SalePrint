export enum StatusImpressora {
  ATIVA = 'ATIVA',
  BACKUP = 'BACKUP',
  PROVISORIA = 'PROVISORIA',
  COM_DEFEITO_INTERNO = 'COM_DEFEITO_INTERNO',
  EM_MANUTENCAO_QUALYCOPY = 'EM_MANUTENCAO_QUALYCOPY',
  RETIRADA = 'RETIRADA'
}

export const STATUS_LABELS: Record<StatusImpressora, string> = {
  [StatusImpressora.ATIVA]: 'Ativa',
  [StatusImpressora.BACKUP]: 'Backup',
  [StatusImpressora.PROVISORIA]: 'Provisória',
  [StatusImpressora.COM_DEFEITO_INTERNO]: 'Com defeito interno',
  [StatusImpressora.EM_MANUTENCAO_QUALYCOPY]: 'Manutenção Qualycopy',
  [StatusImpressora.RETIRADA]: 'Retirada'
};
