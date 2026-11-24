import { Component, computed, EventEmitter, HostListener, inject, Output, signal } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { StatisticheService } from '../../core/services/statistiche.service';
import { TelevisoriService } from '../../core/services/televisori.service';
import { UtentiService } from '../../core/services/utenti.service';
import { ToastService } from '../../core/services/toast.service';
import { iniziali } from '../../core/models/utente.model';
import { ETICHETTE_RUOLO } from '../../core/models/utente.model';
import { oggiEsteso } from '../../core/utils/data.util';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [IconComponent],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.css',
})
export class TopbarComponent {
  @Output() apriMenu = new EventEmitter<void>();

  private readonly auth = inject(AuthService);
  private readonly statistiche = inject(StatisticheService);
  private readonly televisoriService = inject(TelevisoriService);
  private readonly utentiService = inject(UtentiService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly utente = this.auth.utente;
  readonly isAdmin = this.auth.isAdmin;
  readonly daCalibrare = this.statistiche.daCalibrare;
  readonly oggi = oggiEsteso();
  readonly ricerca = signal('');
  readonly menuAperto = signal(false);

  readonly nome = computed(() => {
    const utente = this.utente();
    return utente ? `${utente.nome} ${utente.cognome}` : '';
  });
  readonly ruolo = computed(() => {
    const utente = this.utente();
    return utente ? ETICHETTE_RUOLO[utente.ruolo] : '';
  });
  readonly inizialiUtente = computed(() => {
    const utente = this.utente();
    return utente ? iniziali(utente) : 'MW';
  });

  cerca(event: Event): void {
    event.preventDefault();
    const query = this.ricerca().trim();
    this.router.navigate(['/televisori'], query ? { queryParams: { q: query } } : {});
  }

  aggiornaRicerca(event: Event): void {
    this.ricerca.set((event.target as HTMLInputElement).value);
  }

  vaiACalibrare(): void {
    this.router.navigate(['/televisori'], { queryParams: { stato: 'DA_CALIBRARE' } });
  }

  vaiAUtenti(): void {
    this.menuAperto.set(false);
    this.router.navigateByUrl('/admin/utenti');
  }

  logout(): void {
    this.auth.logout();
    this.televisoriService.azzera();
    this.utentiService.azzera();
    this.toast.info('Sessione terminata. A presto.');
    this.router.navigateByUrl('/login');
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;
    if (!target?.closest('.menu-utente')) {
      this.menuAperto.set(false);
    }
  }
}
