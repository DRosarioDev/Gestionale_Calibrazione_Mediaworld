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
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UtentiService } from '../../../../core/services/utenti.service';
import { iniziali, nomeCompleto, Utente } from '../../../../core/models/utente.model';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';
import { IconComponent } from '../../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-reimposta-password-modal',
  standalone: true,
  imports: [ReactiveFormsModule, ModalComponent, IconComponent],
  templateUrl: './reimposta-password-modal.component.html',
  styleUrl: './reimposta-password-modal.component.css',
})
export class ReimpostaPasswordModalComponent implements OnChanges {
  private readonly fb = inject(FormBuilder);
  private readonly utentiService = inject(UtentiService);

  @Input() aperto = false;
  @Input() utente: Utente | null = null;
  @Output() reimpostata = new EventEmitter<Utente>();
  @Output() chiuso = new EventEmitter<void>();

  readonly mostraPassword = signal(false);
  readonly errore = signal<string | null>(null);
  readonly salvataggio = signal(false);

  readonly form = this.fb.nonNullable.group({
    nuovaPassword: ['', [Validators.required, Validators.minLength(8)]],
    confermaPassword: ['', Validators.required],
  });

  get nomeUtente(): string {
    return this.utente ? nomeCompleto(this.utente) : '';
  }

  get inizialiUtente(): string {
    return this.utente ? iniziali(this.utente) : '';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['aperto'] && this.aperto) {
      this.errore.set(null);
      this.mostraPassword.set(false);
      this.form.reset({ nuovaPassword: '', confermaPassword: '' });
    }
  }

  async reimposta(): Promise<void> {
    const utente = this.utente;
    if (!utente) return;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errore.set('Inserisci una nuova password di almeno 8 caratteri.');
      return;
    }

    const { nuovaPassword, confermaPassword } = this.form.getRawValue();

    if (nuovaPassword !== confermaPassword) {
      this.errore.set('Le due password non coincidono.');
      return;
    }

    this.errore.set(null);
    this.salvataggio.set(true);

    try {
      await this.utentiService.reimpostaPassword(utente.id, nuovaPassword);
      this.reimpostata.emit(utente);
      this.chiudi();
    } catch (errore: unknown) {
      this.errore.set(
        errore instanceof Error
          ? errore.message
          : 'Non è stato possibile reimpostare la password.',
      );
    } finally {
      this.salvataggio.set(false);
    }
  }

  chiudi(): void {
    this.chiuso.emit();
  }
}
