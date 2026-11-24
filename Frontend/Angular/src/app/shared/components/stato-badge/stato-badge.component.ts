import { Component, Input } from '@angular/core';
import { ETICHETTE_STATO, StatoTv } from '../../../core/models/televisore.model';

@Component({
  selector: 'app-stato-badge',
  standalone: true,
  templateUrl: './stato-badge.component.html',
  styleUrl: './stato-badge.component.css',
})
export class StatoBadgeComponent {
  @Input({ required: true }) stato!: StatoTv;

  get etichetta(): string {
    return ETICHETTE_STATO[this.stato] ?? this.stato;
  }

  get classe(): string {
    return `stato-${this.stato.toLowerCase()}`;
  }
}
