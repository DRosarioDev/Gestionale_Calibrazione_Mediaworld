import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { EsportazioneService } from '../../../core/services/esportazione.service';
import { TelevisoriService } from '../../../core/services/televisori.service';
import { ToastService } from '../../../core/services/toast.service';
import { COLORI, ColoreBase } from '../../../core/models/calibrazione.model';
import { StatoBadgeComponent } from '../../../shared/components/stato-badge/stato-badge.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { EsportaCalibrazioneModalComponent } from '../components/esporta-calibrazione-modal/esporta-calibrazione-modal.component';
import { formattaData, formattaDataOra } from '../../../core/utils/data.util';

@Component({
  selector: 'app-dettaglio-televisore',
  standalone: true,
  imports: [
    RouterModule,
    StatoBadgeComponent,
    IconComponent,
    ModalComponent,
    EsportaCalibrazioneModalComponent,
  ],
  templateUrl: './dettaglio-televisore.component.html',
  styleUrl: './dettaglio-televisore.component.css',
})
export class DettaglioTelevisoreComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly televisoriService = inject(TelevisoriService);
  private readonly esportazione = inject(EsportazioneService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly id = signal('');
  readonly televisore = computed(() => this.televisoriService.getById(this.id()));
  readonly isAdmin = this.auth.isAdmin;
  readonly colori = COLORI;

  readonly modaleEsporta = signal(false);
  readonly confermaEliminazione = signal(false);
  readonly puoEliminare = computed(() => {
    if (this.isAdmin()) return true;
    const televisore = this.televisore();
    const utente = this.auth.nomeCompleto();
    return (
      !!televisore &&
      !!utente &&
      televisore.aggiuntoDa.trim().toLowerCase() === utente.trim().toLowerCase()
    );
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id') ?? '';
    this.id.set(id);

    if (!this.televisoriService.getById(id)) {
      this.toast.errore('Televisione non trovata.');
      void this.router.navigateByUrl('/televisori');
    }
  }

  get calibrazione() {
    return this.televisore()?.calibrazione ?? null;
  }

  get valoriImmagine(): Array<{ etichetta: string; valore: string }> {
    const calibrazione = this.calibrazione;
    if (!calibrazione) return [];
    return this.esportazione.valoriRiepilogo(calibrazione.valori);
  }

  avviaCalibrazione(): void {
    const tv = this.televisore();
    if (!tv) return;

    this.televisoriService
      .avviaCalibrazione(tv.id)
      .then(() => this.router.navigate(['/televisori', tv.id, 'calibrazione']))
      .catch((errore: unknown) =>
        this.toast.errore(
          errore instanceof Error ? errore.message : 'Impossibile avviare la calibrazione.',
        ),
      );
  }

  valoreColore(chiave: ColoreBase): string {
    const calibrazione = this.calibrazione;
    if (!calibrazione) return '—';
    const colore = calibrazione.valori.colori[chiave];
    return `${colore.valore1} · ${colore.valore2} · ${colore.valore3}`;
  }

  elimina(): void {
    const tv = this.televisore();
    if (!tv) return;

    this.televisoriService
      .elimina(tv.id)
      .then(() => {
        this.confermaEliminazione.set(false);
        this.toast.successo(`${tv.marca} ${tv.modello} eliminata dall’archivio.`);
        void this.router.navigateByUrl('/televisori');
      })
      .catch((errore: unknown) => {
        this.confermaEliminazione.set(false);
        this.toast.errore(
          errore instanceof Error ? errore.message : 'Impossibile eliminare la televisione.',
        );
      });
  }

  data(iso: string | null | undefined): string {
    return formattaData(iso);
  }

  dataOra(iso: string | null | undefined): string {
    return formattaDataOra(iso);
  }

  segno(valore: number): string {
    return valore > 0 ? `+${valore}` : `${valore}`;
  }
}
