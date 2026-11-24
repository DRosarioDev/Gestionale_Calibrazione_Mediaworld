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
  private readonly toast = inject(ToastService);

  @Input() aperto = false;
  @Input() televisore: Televisore | null = null;
  @Output() chiuso = new EventEmitter<void>();

  readonly testo = signal('');
  readonly nomeFile = signal('');
  private testoOriginale = '';

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['aperto'] && this.aperto) {
      this.prepara();
    }
  }

  get modificato(): boolean {
    return this.testo() !== this.testoOriginale;
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
    this.testoOriginale = this.esportazione.costruisciTesto(this.televisore);
    this.testo.set(this.testoOriginale);
    this.nomeFile.set(this.esportazione.nomeFilePredefinito(this.televisore));
  }
}
