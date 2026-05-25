import { StatusImpressora } from './status-impressora';

export interface Movimentacao {
  id: number;
  impressoraId: number;
  dataMovimentacao: string;
  setorOrigemId?: number;
  setorOrigemNome?: string;
  setorDestinoId: number;
  setorDestinoNome?: string;
  statusAplicado: StatusImpressora;
  responsavel: string;
  osQualycopy?: string;
  descricao?: string;
}

export interface MovimentacaoRequest {
  impressoraId: number;
  setorDestinoId: number;
  statusAplicado: StatusImpressora;
  responsavel: string;
  osQualycopy?: string;
  descricao?: string;
}
