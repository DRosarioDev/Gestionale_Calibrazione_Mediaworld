import { Component, computed, EventEmitter, inject, Input, Output } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { StatisticheService } from '../../core/services/statistiche.service';
import { TelevisoriService } from '../../core/services/televisori.service';
import { UtentiService } from '../../core/services/utenti.service';
import { ToastService } from '../../core/services/toast.service';
import { iniziali, nomeCompleto } from '../../core/models/utente.model';
import { BrandLogoComponent } from '../../shared/components/brand-logo/brand-logo.component';
import { IconComponent } from '../../shared/components/icon/icon.component';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule, BrandLogoComponent, IconComponent],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  @Input() aperta = false;
  @Output() chiuso = new EventEmitter<void>();

  private readonly auth = inject(AuthService);
  private readonly statistiche = inject(StatisticheService);
  private readonly televisoriService = inject(TelevisoriService);
  private readonly utentiService = inject(UtentiService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  readonly utente = this.auth.utente;
  readonly isAdmin = this.auth.isAdmin;
  readonly daCalibrare = this.statistiche.daCalibrare;
  readonly nome = computed(() => {
    const utente = this.utente();
    return utente ? nomeCompleto(utente) : '';
  });
  readonly iniziali = computed(() => {
    const utente = this.utente();
    return utente ? iniziali(utente) : 'MW';
  });

  logout(): void {
    this.auth.logout();
    this.televisoriService.azzera();
    this.utentiService.azzera();
    this.toast.info('Sessione terminata. A presto.');
    this.router.navigateByUrl('/login');
  }
}
