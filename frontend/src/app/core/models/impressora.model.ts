import { StatusImpressora } from './status-impressora';

export interface Impressora {
  id?: number;
  marca: string;
  modelo: string;
  numeroSerie: string;
  ip: string;
  status: StatusImpressora;
  setorId: number;
  setorNome?: string;
  localidadeNome?: string;
  observacao?: string;
}

export interface DashboardStats {
  totalAtivas: number;
  totalManutencaoQualycopy: number;
  totalBackups: number;
  totalProvisorias: number;
  totalComDefeito: number;
  totalRetiradas: number;
  totalGeral: number;
  totalModelos?: number;
}
