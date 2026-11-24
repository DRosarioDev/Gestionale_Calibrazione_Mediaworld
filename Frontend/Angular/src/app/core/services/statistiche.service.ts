import { computed, inject, Injectable } from '@angular/core';
import { TelevisoriService } from './televisori.service';
import { Televisore } from '../models/televisore.model';
import { etichettaMese } from '../utils/data.util';

export interface SegmentoGrafico {
  etichetta: string;
  valore: number;
  colore: string;
}

export interface BarraGrafico {
  etichetta: string;
  valore: number;
  evidenza?: boolean;
}

@Injectable({ providedIn: 'root' })
export class StatisticheService {
  private readonly televisoriService = inject(TelevisoriService);

  readonly totale = computed(() => this.televisoriService.televisori().length);

  readonly calibrati = computed(
    () => this.televisoriService.televisori().filter((tv) => tv.stato === 'CALIBRATO').length,
  );

  readonly inCalibrazione = computed(
    () => this.televisoriService.televisori().filter((tv) => tv.stato === 'IN_CALIBRAZIONE').length,
  );

  readonly daCalibrare = computed(
    () => this.televisoriService.televisori().filter((tv) => tv.stato === 'DA_CALIBRARE').length,
  );

  readonly completamento = computed(() => {
    const totale = this.totale();
    return totale === 0 ? 0 : Math.round((this.calibrati() / totale) * 100);
  });

  readonly calibratiUltimi30Giorni = computed(() => {
    const soglia = Date.now() - 30 * 24 * 60 * 60 * 1000;
    return this.televisoriService
      .televisori()
      .filter((tv) => tv.dataCalibrazione && new Date(tv.dataCalibrazione).getTime() >= soglia)
      .length;
  });

  readonly perStato = computed<SegmentoGrafico[]>(() => [
    { etichetta: 'Calibrati', valore: this.calibrati(), colore: '#16a34a' },
    { etichetta: 'In calibrazione', valore: this.inCalibrazione(), colore: '#2563eb' },
    { etichetta: 'Da calibrare', valore: this.daCalibrare(), colore: '#f59e0b' },
  ]);

  readonly perMarca = computed<BarraGrafico[]>(() => {
    const conteggi = new Map<string, number>();
    for (const tv of this.televisoriService.televisori()) {
      conteggi.set(tv.marca, (conteggi.get(tv.marca) ?? 0) + 1);
    }
    return [...conteggi.entries()]
      .map(([etichetta, valore]) => ({ etichetta, valore }))
      .sort((a, b) => b.valore - a.valore)
      .slice(0, 7);
  });

  readonly calibrazioniPerMese = computed<BarraGrafico[]>(() => {
    const oggi = new Date();
    const mesi: { anno: number; mese: number; etichetta: string }[] = [];

    for (let i = 5; i >= 0; i -= 1) {
      const data = new Date(oggi.getFullYear(), oggi.getMonth() - i, 1);
      mesi.push({
        anno: data.getFullYear(),
        mese: data.getMonth(),
        etichetta: etichettaMese(data),
      });
    }

    const calibrati = this.televisoriService
      .televisori()
      .filter((tv) => tv.dataCalibrazione !== null);

    return mesi.map((mese, indice) => ({
      etichetta: mese.etichetta,
      valore: calibrati.filter((tv) => {
        const data = new Date(tv.dataCalibrazione as string);
        return data.getFullYear() === mese.anno && data.getMonth() === mese.mese;
      }).length,
      evidenza: indice === mesi.length - 1,
    }));
  });

  readonly televisoriDaCalibrare = computed<Televisore[]>(() =>
    this.televisoriService
      .televisori()
      .filter((tv) => tv.stato !== 'CALIBRATO')
      .sort((a, b) => new Date(a.dataAggiunta).getTime() - new Date(b.dataAggiunta).getTime())
      .slice(0, 6),
  );

  readonly ultimeCalibrazioni = computed<Televisore[]>(() =>
    this.televisoriService
      .televisori()
      .filter((tv) => tv.stato === 'CALIBRATO' && tv.dataCalibrazione !== null)
      .sort(
        (a, b) =>
          new Date(b.dataCalibrazione as string).getTime() -
          new Date(a.dataCalibrazione as string).getTime(),
      )
      .slice(0, 6),
  );

  readonly ultimeAggiunte = computed<Televisore[]>(() =>
    [...this.televisoriService.televisori()]
      .sort((a, b) => new Date(b.dataAggiunta).getTime() - new Date(a.dataAggiunta).getTime())
      .slice(0, 6),
  );
}
