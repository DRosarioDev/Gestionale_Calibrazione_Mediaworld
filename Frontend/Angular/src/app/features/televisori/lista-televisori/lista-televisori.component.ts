import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { TelevisoriService } from '../../../core/services/televisori.service';
import { ETICHETTE_STATO, StatoTv, Televisore } from '../../../core/models/televisore.model';
import { formattaData } from '../../../core/utils/data.util';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { StatoBadgeComponent } from '../../../shared/components/stato-badge/stato-badge.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

type FiltroStato = 'TUTTI' | StatoTv;
type Ordinamento = 'recenti' | 'modello' | 'stato' | 'calibrazione';

@Component({
  selector: 'app-lista-televisori',
  standalone: true,
  imports: [
    RouterModule,
    PageHeaderComponent,
    StatoBadgeComponent,
    IconComponent,
    EmptyStateComponent,
  ],
  templateUrl: './lista-televisori.component.html',
  styleUrl: './lista-televisori.component.css',
})
export class ListaTelevisoriComponent {
  private readonly televisoriService = inject(TelevisoriService);
  private readonly route = inject(ActivatedRoute);

  readonly televisioni = this.televisoriService.televisori;
  readonly totale = this.televisoriService.totale;

  readonly cerca = signal('');
  readonly stato = signal<FiltroStato>('TUTTI');
  readonly marca = signal('TUTTE');
  readonly ordinamento = signal<Ordinamento>('recenti');

  readonly filtriStato: Array<{ valore: FiltroStato; etichetta: string }> = [
    { valore: 'TUTTI', etichetta: 'Tutti' },
    { valore: 'DA_CALIBRARE', etichetta: ETICHETTE_STATO.DA_CALIBRARE },
    { valore: 'IN_CALIBRAZIONE', etichetta: ETICHETTE_STATO.IN_CALIBRAZIONE },
    { valore: 'CALIBRATO', etichetta: ETICHETTE_STATO.CALIBRATO },
  ];

  readonly marche = computed(() =>
    [...new Set(this.televisioni().map((tv) => tv.marca))].sort((a, b) => a.localeCompare(b)),
  );

  readonly conteggi = computed<Record<FiltroStato, number>>(() => {
    const televisioni = this.televisioni();
    return {
      TUTTI: televisioni.length,
      DA_CALIBRARE: televisioni.filter((tv) => tv.stato === 'DA_CALIBRARE').length,
      IN_CALIBRAZIONE: televisioni.filter((tv) => tv.stato === 'IN_CALIBRAZIONE').length,
      CALIBRATO: televisioni.filter((tv) => tv.stato === 'CALIBRATO').length,
    };
  });

  readonly televisoriFiltrati = computed<Televisore[]>(() => {
    const testo = this.cerca().trim().toLowerCase();
    const stato = this.stato();
    const marca = this.marca();

    let lista = this.televisioni().filter((tv) => {
      if (stato !== 'TUTTI' && tv.stato !== stato) return false;
      if (marca !== 'TUTTE' && tv.marca !== marca) return false;
      if (!testo) return true;
      return [tv.marca, tv.modello, tv.codiceSeriale]
        .join(' ')
        .toLowerCase()
        .includes(testo);
    });

    const ordine: Record<StatoTv, number> = {
      DA_CALIBRARE: 0,
      IN_CALIBRAZIONE: 1,
      CALIBRATO: 2,
    };

    switch (this.ordinamento()) {
      case 'modello':
        lista = lista.sort((a, b) => `${a.marca} ${a.modello}`.localeCompare(`${b.marca} ${b.modello}`));
        break;
      case 'stato':
        lista = lista.sort(
          (a, b) =>
            ordine[a.stato] - ordine[b.stato] ||
            new Date(b.dataAggiunta).getTime() - new Date(a.dataAggiunta).getTime(),
        );
        break;
      case 'calibrazione':
        lista = lista.sort((a, b) => {
          const dataA = a.dataCalibrazione ? new Date(a.dataCalibrazione).getTime() : 0;
          const dataB = b.dataCalibrazione ? new Date(b.dataCalibrazione).getTime() : 0;
          return dataB - dataA;
        });
        break;
      default:
        lista = lista.sort(
          (a, b) => new Date(b.dataAggiunta).getTime() - new Date(a.dataAggiunta).getTime(),
        );
    }

    return lista;
  });

  constructor() {
    this.route.queryParamMap.pipe(takeUntilDestroyed()).subscribe((parametri) => {
      const query = parametri.get('q');
      if (query !== null) this.cerca.set(query);

      const stato = parametri.get('stato');
      if (stato && this.isFiltroStato(stato)) {
        this.stato.set(stato);
      }
    });
  }

  aggiornaRicerca(event: Event): void {
    this.cerca.set((event.target as HTMLInputElement).value);
  }

  aggiornaMarca(event: Event): void {
    this.marca.set((event.target as HTMLSelectElement).value);
  }

  aggiornaOrdinamento(event: Event): void {
    this.ordinamento.set((event.target as HTMLSelectElement).value as Ordinamento);
  }

  impostaStato(stato: FiltroStato): void {
    this.stato.set(stato);
  }

  azzeraFiltri(): void {
    this.cerca.set('');
    this.stato.set('TUTTI');
    this.marca.set('TUTTE');
    this.ordinamento.set('recenti');
  }

  data(iso: string | null | undefined): string {
    return formattaData(iso);
  }

  private isFiltroStato(valore: string): valore is FiltroStato {
    return ['TUTTI', 'DA_CALIBRARE', 'IN_CALIBRAZIONE', 'CALIBRATO'].includes(valore);
  }
}
