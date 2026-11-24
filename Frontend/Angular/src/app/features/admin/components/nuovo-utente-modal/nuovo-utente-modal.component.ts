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
import { Ruolo, Utente } from '../../../../core/models/utente.model';
import { emailAziendaleValidator } from '../../../../core/validators/email-aziendale.validator';
import { ModalComponent } from '../../../../shared/components/modal/modal.component';
import { IconComponent } from '../../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-nuovo-utente-modal',
  standalone: true,
  imports: [ReactiveFormsModule, ModalComponent, IconComponent],
  templateUrl: './nuovo-utente-modal.component.html',
  styleUrl: './nuovo-utente-modal.component.css',
})
export class NuovoUtenteModalComponent implements OnChanges {
  private readonly fb = inject(FormBuilder);
  private readonly utentiService = inject(UtentiService);

  @Input() aperto = false;
  @Output() creato = new EventEmitter<Utente>();
  @Output() chiuso = new EventEmitter<void>();

  readonly mostraPassword = signal(false);
  readonly errore = signal<string | null>(null);
  readonly salvataggio = signal(false);

  readonly form = this.fb.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(2)]],
    cognome: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email, emailAziendaleValidator]],
    ruolo: this.fb.nonNullable.control<Ruolo>('TECNICO'),
    password: ['', [Validators.required, Validators.minLength(8)]],
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['aperto'] && this.aperto) {
      this.errore.set(null);
      this.mostraPassword.set(false);
      this.form.reset({
        nome: '',
        cognome: '',
        email: '',
        ruolo: 'TECNICO',
        password: '',
      });
    }
  }

  async crea(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errore.set('Completa tutti i campi e usa un’email @mediaworld.it.');
      return;
    }

    this.errore.set(null);
    this.salvataggio.set(true);

    try {
      const utente = await this.utentiService.crea(this.form.getRawValue());
      this.creato.emit(utente);
      this.chiudi();
    } catch (errore: unknown) {
      this.errore.set(
        errore instanceof Error ? errore.message : 'Non è stato possibile creare il profilo.',
      );
    } finally {
      this.salvataggio.set(false);
    }
  }

  chiudi(): void {
    this.chiuso.emit();
  }
}
