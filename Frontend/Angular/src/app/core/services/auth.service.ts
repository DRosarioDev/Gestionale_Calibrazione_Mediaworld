import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environment/environment';
import { Utente, nomeCompleto } from '../models/utente.model';

interface RispostaLogin {
  token: string;
  utente: Utente;
}

interface Sessione {
  utente: Utente;
  token: string;
}

const CHIAVE_SESSIONE = 'mediaworld_calibration_session_v1';
const BASE_URL = `${environment.backendUrl}/auth`;

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly _sessione = signal<Sessione | null>(this.leggiSessione());

  readonly sessione = this._sessione.asReadonly();
  readonly utente = computed(() => this._sessione()?.utente ?? null);
  readonly autenticato = computed(() => this._sessione() !== null);
  readonly isAdmin = computed(() => this._sessione()?.utente.ruolo === 'ADMIN');
  readonly nomeCompleto = computed(() => {
    const utente = this._sessione()?.utente;
    return utente ? nomeCompleto(utente) : null;
  });
  readonly token = computed(() => this._sessione()?.token ?? null);

  async login(email: string, password: string): Promise<Utente> {
    const risposta = await firstValueFrom(
      this.http.post<RispostaLogin>(`${BASE_URL}/login`, {
        email: email.trim().toLowerCase(),
        password,
      }),
    );

    const sessione: Sessione = { utente: risposta.utente, token: risposta.token };
    this._sessione.set(sessione);
    this.salvaSessione(sessione);
    return risposta.utente;
  }

  /**
   * Verifica il token salvato all'avvio e recupera i dati aggiornati dell'utente.
   * Se il token non è più valido la sessione locale viene eliminata.
   */
  async verificaSessione(): Promise<boolean> {
    const sessione = this._sessione();
    if (!sessione) return false;

    try {
      const utente = await firstValueFrom(this.http.get<Utente>(`${BASE_URL}/me`));
      const aggiornata: Sessione = { ...sessione, utente };
      this._sessione.set(aggiornata);
      this.salvaSessione(aggiornata);
      return true;
    } catch {
      this.logout();
      return false;
    }
  }

  async cambiaPassword(passwordAttuale: string, nuovaPassword: string): Promise<void> {
    await firstValueFrom(
      this.http.patch<void>(`${BASE_URL}/password`, { passwordAttuale, nuovaPassword }),
    );
  }

  logout(): void {
    this._sessione.set(null);
    try {
      localStorage.removeItem(CHIAVE_SESSIONE);
    } catch {
      // Nessuna azione necessaria.
    }
  }

  private leggiSessione(): Sessione | null {
    try {
      const salvata = localStorage.getItem(CHIAVE_SESSIONE);
      if (!salvata) return null;
      const sessione = JSON.parse(salvata) as Sessione;
      if (!sessione?.token || !sessione?.utente?.id) return null;
      return sessione;
    } catch {
      return null;
    }
  }

  private salvaSessione(sessione: Sessione): void {
    try {
      localStorage.setItem(CHIAVE_SESSIONE, JSON.stringify(sessione));
    } catch {
      // Sessione valida solo in memoria.
    }
  }
}
