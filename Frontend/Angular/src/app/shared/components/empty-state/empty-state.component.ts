import { Component, Input } from '@angular/core';
import { IconComponent, Icona } from '../icon/icon.component';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './empty-state.component.html',
  styleUrl: './empty-state.component.css',
})
export class EmptyStateComponent {
  @Input() icona: Icona = 'search';
  @Input() titolo = 'Nessun risultato';
  @Input() messaggio = '';
}
