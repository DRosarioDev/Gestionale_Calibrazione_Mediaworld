import { Component, Input } from '@angular/core';
import { IconComponent, Icona } from '../icon/icon.component';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './stat-card.component.html',
  styleUrl: './stat-card.component.css',
})
export class StatCardComponent {
  @Input() etichetta = '';
  @Input() valore: string | number = 0;
  @Input() descrizione = '';
  @Input() icona: Icona = 'chart';
  @Input() colore: 'rosso' | 'verde' | 'blu' | 'ambra' | 'neutro' = 'neutro';
  @Input() tendenza = '';
}
