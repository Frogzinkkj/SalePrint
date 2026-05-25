import { Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { ImpressoraListComponent } from './components/impressora-list/impressora-list.component';
import { ImpressoraFormComponent } from './components/impressora-form/impressora-form.component';
import { TimelineComponent } from './components/timeline/timeline.component';
import { ImportacaoComponent } from './components/importacao/importacao.component';
import { SubstituicaoListComponent } from './components/substituicao-list/substituicao-list.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'impressoras', component: ImpressoraListComponent },
  { path: 'substituicoes', component: SubstituicaoListComponent },
  { path: 'impressoras/nova', component: ImpressoraFormComponent },
  { path: 'impressoras/:id/editar', component: ImpressoraFormComponent },
  { path: 'impressoras/:id/historico', component: TimelineComponent },
  { path: 'importacao', component: ImportacaoComponent },
  { path: '**', redirectTo: '' }
];
