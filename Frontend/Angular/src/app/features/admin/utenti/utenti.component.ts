import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { UtentiService } from '../../../core/services/utenti.service';
import { ToastService } from '../../../core/services/toast.service';
import { iniziali, nomeCompleto, Utente } from '../../../core/models/utente.model';
import { formattaData } from '../../../core/utils/data.util';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { RuoloBadgeComponent } from '../../../shared/components/ruolo-badge/ruolo-badge.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { NuovoUtenteModalComponent } from '../components/nuovo-utente-modal/nuovo-utente-modal.component';

@Component({
  selector: 'app-utenti',
  standalone: true,
  imports: [
    PageHeaderComponent,
    StatCardComponent,
    RuoloBadgeComponent,
    IconComponent,
    ModalComponent,
    NuovoUtenteModalComponent,
  ],
  templateUrl: './utenti.component.html',
  styleUrl: './utenti.component.css',
})
export class UtentiComponent implements OnInit {
  private readonly utentiService = inject(UtentiService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly utenti = this.utentiService.utenti;
  readonly totale = this.utentiService.totale;
  readonly amministratori = this.utentiService.amministratori;
  readonly tecnici = this.utentiService.tecnici;
  readonly utenteCorrente = this.auth.utente;

  readonly modaleNuovo = signal(false);
  readonly utenteDaDisattivare = signal<Utente | null>(null);
  readonly caricamento = signal(false);

  readonly attivi = computed(() => this.utenti().filter((utente) => utente.attivo).length);
  readonly disattivati = computed(() => this.utenti().filter((utente) => !utente.attivo).length);

  readonly utentiOrdinati = computed(() =>
    [...this.utenti()].sort((a, b) => {
      if (a.ruolo !== b.ruolo) return a.ruolo === 'ADMIN' ? -1 : 1;
      return nomeCompleto(a).localeCompare(nomeCompleto(b));
    }),
  );

  ngOnInit(): void {
    if (this.utenti().length === 0) {
      void this.carica();
    }
  }

  nome(utente: Utente): string {
    return nomeCompleto(utente);
  }

  iniziali(utente: Utente): string {
    return iniziali(utente);
  }

  data(iso: string): string {
    return formattaData(iso);
  }

  onProfiloCreato(utente: Utente): void {
    this.toast.successo(`Profilo di ${nomeCompleto(utente)} creato correttamente.`);
  }

  richiediCambioStato(utente: Utente): void {
    if (utente.id === this.utenteCorrente()?.id) {
      this.toast.attenzione('Non puoi disattivare il profilo con cui hai effettuato l’accesso.');
      return;
    }

    if (utente.attivo) {
      this.utenteDaDisattivare.set(utente);
    } else {
      void this.applicaStato(utente, true);
    }
  }

  confermaDisattivazione(): void {
    const utente = this.utenteDaDisattivare();
    if (!utente) return;
    this.utenteDaDisattivare.set(null);
    void this.applicaStato(utente, false);
  }

  private async carica(): Promise<void> {
    this.caricamento.set(true);
    try {
      await this.utentiService.carica();
    } catch (errore: unknown) {
      this.toast.errore(
        errore instanceof Error ? errore.message : 'Impossibile caricare i profili.',
      );
    } finally {
      this.caricamento.set(false);
    }
  }

  private async applicaStato(utente: Utente, attivo: boolean): Promise<void> {
    try {
      await this.utentiService.cambiaStato(utente.id, attivo);
      this.toast.successo(
        attivo
          ? `Profilo di ${nomeCompleto(utente)} riattivato.`
          : `Profilo di ${nomeCompleto(utente)} disattivato.`,
      );
    } catch (errore: unknown) {
      this.toast.errore(
        errore instanceof Error ? errore.message : 'Impossibile aggiornare il profilo.',
      );
    }
  }
}
