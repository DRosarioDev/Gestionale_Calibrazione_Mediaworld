import { Component, Input } from '@angular/core';
import { ETICHETTE_RUOLO, Ruolo } from '../../../core/models/utente.model';

@Component({
  selector: 'app-ruolo-badge',
  standalone: true,
  templateUrl: './ruolo-badge.component.html',
  styleUrl: './ruolo-badge.component.css',
})
export class RuoloBadgeComponent {
  @Input({ required: true }) ruolo!: Ruolo;

  get etichetta(): string {
    return ETICHETTE_RUOLO[this.ruolo] ?? this.ruolo;
  }
}
