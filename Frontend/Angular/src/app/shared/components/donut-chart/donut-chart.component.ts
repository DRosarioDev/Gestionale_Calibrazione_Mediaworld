import { Component, Input } from '@angular/core';
import { SegmentoGrafico } from '../../../core/services/statistiche.service';

interface SegmentoCalcolato extends SegmentoGrafico {
  percentuale: number;
  offset: number;
}

@Component({
  selector: 'app-donut-chart',
  standalone: true,
  templateUrl: './donut-chart.component.html',
  styleUrl: './donut-chart.component.css',
})
export class DonutChartComponent {
  @Input() segmenti: SegmentoGrafico[] = [];
  @Input() etichettaTotale = 'TV';
  @Input() dimensione = 196;

  get totale(): number {
    return this.segmenti.reduce((totale, segmento) => totale + segmento.valore, 0);
  }

  get segmentiCalcolati(): SegmentoCalcolato[] {
    const totale = this.totale;
    if (totale === 0) return [];

    let offset = 0;
    return this.segmenti
      .filter((segmento) => segmento.valore > 0)
      .map((segmento) => {
        const percentuale = (segmento.valore / totale) * 100;
        const calcolato: SegmentoCalcolato = { ...segmento, percentuale, offset };
        offset += percentuale;
        return calcolato;
      });
  }
}
