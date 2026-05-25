export interface Substituicao {
  id: number;
  dataSubstituicao: string;
  responsavel: string;
  observacao?: string;
  impressoraAntigaId: number;
  antigaMarca: string;
  antigaModelo: string;
  antigaNumeroSerie: string;
  antigaIp: string;
  antigaSetorNome?: string;
  impressoraNovaId: number;
  novaMarca: string;
  novaModelo: string;
  novaNumeroSerie: string;
  novaIp: string;
  novaSetorNome?: string;
}

export interface SubstituicaoRequest {
  impressoraAntigaId?: number;
  impressoraNovaId?: number;
  numeroSerieAntiga?: string;
  numeroSerieNova?: string;
  responsavel: string;
  observacao?: string;
  atualizarStatus?: boolean;
}
