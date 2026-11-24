import { Component, computed, inject } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { StatisticheService } from '../../core/services/statistiche.service';
import { UtentiService } from '../../core/services/utenti.service';
import { formattaData, oggiEsteso } from '../../core/utils/data.util';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { StatCardComponent } from '../../shared/components/stat-card/stat-card.component';
import { DonutChartComponent } from '../../shared/components/donut-chart/donut-chart.component';
import { BarChartComponent } from '../../shared/components/bar-chart/bar-chart.component';
import { StatoBadgeComponent } from '../../shared/components/stato-badge/stato-badge.component';
import { IconComponent } from '../../shared/components/icon/icon.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    RouterModule,
    PageHeaderComponent,
    StatCardComponent,
    DonutChartComponent,
    BarChartComponent,
    StatoBadgeComponent,
    IconComponent,
    EmptyStateComponent,
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {
  private readonly auth = inject(AuthService);
  private readonly statistiche = inject(StatisticheService);
  private readonly utentiService = inject(UtentiService);

  readonly utente = this.auth.utente;
  readonly isAdmin = this.auth.isAdmin;

  readonly totale = this.statistiche.totale;
  readonly calibrati = this.statistiche.calibrati;
  readonly inCalibrazione = this.statistiche.inCalibrazione;
  readonly daCalibrare = this.statistiche.daCalibrare;
  readonly completamento = this.statistiche.completamento;
  readonly calibratiUltimi30Giorni = this.statistiche.calibratiUltimi30Giorni;
  readonly perStato = this.statistiche.perStato;
  readonly calibrazioniPerMese = this.statistiche.calibrazioniPerMese;
  readonly televisoriDaCalibrare = this.statistiche.televisoriDaCalibrare;
  readonly ultimeCalibrazioni = this.statistiche.ultimeCalibrazioni;

  readonly totaleUtenti = this.utentiService.totale;
  readonly tecnici = this.utentiService.tecnici;

  readonly saluto = computed(() => {
    const ora = new Date().getHours();
    if (ora < 12) return 'Buongiorno';
    if (ora < 18) return 'Buon pomeriggio';
    return 'Buonasera';
  });

  readonly sottotitolo = computed(() => {
    const nome = this.utente()?.nome ?? '';
    return `${this.saluto()}, ${nome} · ${oggiEsteso()}`;
  });

  data(iso: string | null | undefined): string {
    return formattaData(iso);
  }
}
