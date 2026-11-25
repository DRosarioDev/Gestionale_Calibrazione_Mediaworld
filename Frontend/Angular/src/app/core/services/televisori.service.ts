import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environment/environment';
import { MetaCalibrazione, ValoriCalibrazione } from '../models/calibrazione.model';
import { NuovoTelevisore, Televisore } from '../models/televisore.model';

const BASE_URL = `${environment.backendUrl}/televisori`;

@Injectable({ providedIn: 'root' })
export class TelevisoriService {
  private readonly http = inject(HttpClient);
  private readonly _televisori = signal<Televisore[]>([]);

  readonly televisori = this._televisori.asReadonly();
  readonly totale = computed(() => this._televisori().length);

  async carica(): Promise<void> {
    const televisori = await firstValueFrom(this.http.get<Televisore[]>(BASE_URL));
    this._televisori.set(televisori);
  }

  getById(id: string): Televisore | undefined {
    return this._televisori().find((tv) => tv.id === id);
  }

  async aggiungi(input: NuovoTelevisore): Promise<Televisore> {
    const tv = await firstValueFrom(this.http.post<Televisore>(BASE_URL, input));
    this._televisori.update((televisori) => [tv, ...televisori]);
    return tv;
  }

  async avviaCalibrazione(id: string): Promise<Televisore> {
    const tv = await firstValueFrom(
      this.http.patch<Televisore>(`${BASE_URL}/${id}/avvia-calibrazione`, {}),
    );
    this.aggiornaInLista(tv);
    return tv;
  }

  async salvaCalibrazione(
    id: string,
    valori: ValoriCalibrazione,
    meta: MetaCalibrazione,
  ): Promise<Televisore> {
    const richiesta = {
      note: meta.note.trim(),
      valori,
    };

    const tv = await firstValueFrom(
      this.http.put<Televisore>(`${BASE_URL}/${id}/calibrazione`, richiesta),
    );
    this.aggiornaInLista(tv);
    return tv;
  }

  async salvaReport(id: string, report: string): Promise<Televisore> {
    const tv = await firstValueFrom(
      this.http.put<Televisore>(`${BASE_URL}/${id}/calibrazione/report`, { report }),
    );
    this.aggiornaInLista(tv);
    return tv;
  }

  async elimina(id: string): Promise<void> {
    await firstValueFrom(this.http.delete<void>(`${BASE_URL}/${id}`));
    this._televisori.update((televisori) => televisori.filter((tv) => tv.id !== id));
  }

  azzera(): void {
    this._televisori.set([]);
  }

  private aggiornaInLista(televisore: Televisore): void {
    this._televisori.update((televisori) =>
      televisori.map((tv) => (tv.id === televisore.id ? televisore : tv)),
    );
  }
}
