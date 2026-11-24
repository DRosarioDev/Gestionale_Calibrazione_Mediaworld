import { Injectable, signal } from '@angular/core';

export type TipoToast = 'successo' | 'errore' | 'info' | 'attenzione';

export interface Toast {
  id: number;
  messaggio: string;
  tipo: TipoToast;
}

@Injectable({ providedIn: 'root' })
export class ToastService {
  private contatore = 0;
  private readonly _toasts = signal<Toast[]>([]);
  readonly toasts = this._toasts.asReadonly();

  successo(messaggio: string, durata = 3800): void {
    this.mostra(messaggio, 'successo', durata);
  }

  errore(messaggio: string, durata = 5000): void {
    this.mostra(messaggio, 'errore', durata);
  }

  info(messaggio: string, durata = 3800): void {
    this.mostra(messaggio, 'info', durata);
  }

  attenzione(messaggio: string, durata = 4500): void {
    this.mostra(messaggio, 'attenzione', durata);
  }

  mostra(messaggio: string, tipo: TipoToast = 'info', durata = 3800): void {
    const id = ++this.contatore;
    this._toasts.update((toasts) => [...toasts, { id, messaggio, tipo }]);
    setTimeout(() => this.rimuovi(id), durata);
  }

  rimuovi(id: number): void {
    this._toasts.update((toasts) => toasts.filter((toast) => toast.id !== id));
  }
}
