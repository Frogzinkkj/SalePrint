import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { forkJoin } from 'rxjs';
import { MovimentacaoService } from '../../core/services/movimentacao.service';
import { ImpressoraService } from '../../core/services/impressora.service';
import { SubstituicaoService } from '../../core/services/substituicao.service';
import { Movimentacao } from '../../core/models/movimentacao.model';
import { Impressora } from '../../core/models/impressora.model';
import { Substituicao } from '../../core/models/substituicao.model';
import { STATUS_LABELS, StatusImpressora } from '../../core/models/status-impressora';

@Component({
  selector: 'app-timeline',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './timeline.component.html',
  styleUrl: './timeline.component.scss'
})
export class TimelineComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly movimentacaoService = inject(MovimentacaoService);
  private readonly impressoraService = inject(ImpressoraService);
  private readonly substituicaoService = inject(SubstituicaoService);

  impressora: Impressora | null = null;
  historico: Movimentacao[] = [];
  substituicoes: Substituicao[] = [];
  statusLabels = STATUS_LABELS;
  loading = true;

  labelStatus(status: StatusImpressora): string {
    return this.statusLabels[status];
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    forkJoin({
      impressora: this.impressoraService.buscarPorId(id),
      historico: this.movimentacaoService.historico(id),
      substituicoes: this.substituicaoService.listarPorImpressora(id)
    }).subscribe({
      next: ({ impressora, historico, substituicoes }) => {
        this.impressora = impressora;
        this.historico = historico;
        this.substituicoes = substituicoes;
        this.loading = false;
      },
      error: () => (this.loading = false)
    });
  }
}
