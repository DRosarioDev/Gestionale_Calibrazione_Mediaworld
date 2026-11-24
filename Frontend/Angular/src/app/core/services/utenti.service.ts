import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environment/environment';
import { NuovoUtente, Utente } from '../models/utente.model';

const BASE_URL = `${environment.backendUrl}/utenti`;

@Injectable({ providedIn: 'root' })
export class UtentiService {
  private readonly http = inject(HttpClient);
  private readonly _utenti = signal<Utente[]>([]);

  readonly utenti = this._utenti.asReadonly();
  readonly totale = computed(() => this._utenti().length);
  readonly amministratori = computed(
    () => this._utenti().filter((utente) => utente.ruolo === 'ADMIN' && utente.attivo).length,
  );
  readonly tecnici = computed(
    () => this._utenti().filter((utente) => utente.ruolo === 'TECNICO' && utente.attivo).length,
  );

  async carica(): Promise<void> {
    const utenti = await firstValueFrom(this.http.get<Utente[]>(BASE_URL));
    this._utenti.set(utenti);
  }

  perId(id: string): Utente | undefined {
    return this._utenti().find((utente) => utente.id === id);
  }

  async crea(input: NuovoUtente): Promise<Utente> {
    const utente = await firstValueFrom(this.http.post<Utente>(BASE_URL, input));
    this._utenti.update((utenti) => [...utenti, utente]);
    return utente;
  }

  async cambiaStato(id: string, attivo: boolean): Promise<Utente> {
    const utente = await firstValueFrom(
      this.http.patch<Utente>(`${BASE_URL}/${id}/stato`, { attivo }),
    );
    this._utenti.update((utenti) => utenti.map((item) => (item.id === id ? utente : item)));
    return utente;
  }

  azzera(): void {
    this._utenti.set([]);
  }
}
