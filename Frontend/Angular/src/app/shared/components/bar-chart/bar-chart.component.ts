import { Component, Input } from '@angular/core';
import { BarraGrafico } from '../../../core/services/statistiche.service';

@Component({
  selector: 'app-bar-chart',
  standalone: true,
  templateUrl: './bar-chart.component.html',
  styleUrl: './bar-chart.component.css',
})
export class BarChartComponent {
  @Input() dati: BarraGrafico[] = [];
  @Input() altezza = 170;

  get massimo(): number {
    return Math.max(1, ...this.dati.map((dato) => dato.valore));
  }

  percentuale(valore: number): number {
    return Math.max(valore > 0 ? 4 : 0, (valore / this.massimo) * 100);
  }
}
