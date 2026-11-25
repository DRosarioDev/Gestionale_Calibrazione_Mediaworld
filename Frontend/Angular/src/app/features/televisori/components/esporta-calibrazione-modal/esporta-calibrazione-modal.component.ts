import {
  Component,
  EventEmitter,
  inject,
  Input,
  OnChanges,
  Output,
  signal,
  SimpleChanges,
} from '@angular/core';
import { Televisore } from '../../../../core/models/televisore.model';
import { EsportazioneService } from '../../../../core/services/esportazione.service';
import { TelevisoriService } from '../../../../core/services/televisori.service';
import { ToastService } from '../../../../core/services/toast.service';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';
import { IconComponent } from '../../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-esporta-calibrazione-modal',
  standalone: true,
  imports: [ModalComponent, IconComponent],
  templateUrl: './esporta-calibrazione-modal.component.html',
  styleUrl: './esporta-calibrazione-modal.component.css',
})
export class EsportaCalibrazioneModalComponent implements OnChanges {
  private readonly esportazione = inject(EsportazioneService);
  private readonly televisoriService = inject(TelevisoriService);
  private readonly toast = inject(ToastService);

  @Input() aperto = false;
  @Input() televisore: Televisore | null = null;
  @Output() chiuso = new EventEmitter<void>();

  readonly testo = signal('');
  readonly nomeFile = signal('');
  readonly salvataggio = signal(false);
  readonly salvato = signal(false);
  private testoOriginale = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['aperto'] && this.aperto) {
      this.prepara();
    }
  }

  get modificato(): boolean {
    return this.testo() !== this.testoOriginale;
  }

  get reportSalvato(): boolean {
    return this.salvato() && !this.modificato;
  }

  get numeroCaratteri(): number {
    return this.testo().length;
  }

  aggiornaTesto(event: Event): void {
    this.testo.set((event.target as HTMLTextAreaElement).value);
  }

  aggiornaNomeFile(event: Event): void {
    this.nomeFile.set((event.target as HTMLInputElement).value);
  }

  ripristina(): void {
    this.testo.set(this.testoOriginale);
    if (this.televisore) {
      this.nomeFile.set(this.esportazione.nomeFilePredefinito(this.televisore));
    }
  }

  rigenera(): void {
    if (!this.televisore) return;
    this.testo.set(this.esportazione.costruisciTesto(this.televisore));
    this.nomeFile.set(this.esportazione.nomeFilePredefinito(this.televisore));
  }

  async salvaReport(): Promise<void> {
    const tv = this.televisore;
    if (!tv) return;

    if (!this.testo().trim()) {
      this.toast.errore('Il report è vuoto: scrivi il contenuto prima di salvarlo.');
      return;
    }

    this.salvataggio.set(true);
    try {
      await this.televisoriService.salvaReport(tv.id, this.testo());
      this.testoOriginale = this.testo();
      this.salvato.set(true);
      this.toast.successo('Report salvato nella scheda della televisione.');
    } catch (errore: unknown) {
      this.toast.errore(
        errore instanceof Error ? errore.message : 'Errore durante il salvataggio del report.',
      );
    } finally {
      this.salvataggio.set(false);
    }
  }

  scarica(): void {
    if (!this.nomeFile().trim()) {
      this.toast.errore('Inserisci il nome del file prima di esportarlo.');
      return;
    }
    this.esportazione.scarica(this.nomeFile(), this.testo());
    this.toast.successo('Report di calibrazione esportato in formato .txt.');
    this.chiudi();
  }

  async copia(): Promise<void> {
    try {
      await this.esportazione.copia(this.testo());
      this.toast.successo('Testo copiato negli appunti.');
    } catch {
      this.toast.errore('Impossibile copiare il testo. Prova a scaricare il file.');
    }
  }

  chiudi(): void {
    this.chiuso.emit();
  }

  private prepara(): void {
    if (!this.televisore) return;

    const reportSalvato = this.televisore.calibrazione?.report;
    const haReportSalvato = typeof reportSalvato === 'string' && reportSalvato.trim().length > 0;

    this.testoOriginale = haReportSalvato
      ? (reportSalvato as string)
      : this.esportazione.costruisciTesto(this.televisore);
    this.salvato.set(haReportSalvato);
    this.testo.set(this.testoOriginale);
    this.nomeFile.set(this.esportazione.nomeFilePredefinito(this.televisore));
  }
}
