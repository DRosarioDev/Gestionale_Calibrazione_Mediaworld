import { Component, inject } from '@angular/core';
import { ToastService, TipoToast } from '../../../core/services/toast.service';
import { IconComponent, Icona } from '../icon/icon.component';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './toast-container.component.html',
  styleUrl: './toast-container.component.css',
})
export class ToastContainerComponent {
  readonly toast = inject(ToastService);

  icona(tipo: TipoToast): Icona {
    switch (tipo) {
      case 'successo':
        return 'check';
      case 'errore':
        return 'alert';
      case 'attenzione':
        return 'alert';
      default:
        return 'info';
    }
  }
}
