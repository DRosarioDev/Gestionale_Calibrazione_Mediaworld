import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { iniziali, nomeCompleto } from '../../../core/models/utente.model';
import { formattaData } from '../../../core/utils/data.util';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { RuoloBadgeComponent } from '../../../shared/components/ruolo-badge/ruolo-badge.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-profilo',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterModule,
    PageHeaderComponent,
    RuoloBadgeComponent,
    IconComponent,
  ],
  templateUrl: './profilo.component.html',
  styleUrl: './profilo.component.css',
})
export class ProfiloComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly utente = this.auth.utente;
  readonly nome = computed(() => {
    const utente = this.utente();
    return utente ? nomeCompleto(utente) : '';
  });
  readonly inizialiUtente = computed(() => {
    const utente = this.utente();
    return utente ? iniziali(utente) : 'MW';
  });
  readonly dataCreazione = computed(() => formattaData(this.utente()?.dataCreazione));

  readonly mostraPassword = signal(false);
  readonly salvataggio = signal(false);
  readonly errore = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    passwordAttuale: ['', Validators.required],
    nuovaPassword: ['', [Validators.required, Validators.minLength(8)]],
    confermaPassword: ['', Validators.required],
  });

  async salva(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errore.set('Completa tutti i campi: la nuova password deve avere almeno 8 caratteri.');
      return;
    }

    const { passwordAttuale, nuovaPassword, confermaPassword } = this.form.getRawValue();

    if (nuovaPassword !== confermaPassword) {
      this.errore.set('Le due nuove password non coincidono.');
      return;
    }

    if (nuovaPassword === passwordAttuale) {
      this.errore.set('La nuova password deve essere diversa da quella attuale.');
      return;
    }

    this.errore.set(null);
    this.salvataggio.set(true);

    try {
      await this.auth.cambiaPassword(passwordAttuale, nuovaPassword);
      this.form.reset({ passwordAttuale: '', nuovaPassword: '', confermaPassword: '' });
      this.mostraPassword.set(false);
      this.toast.successo('Password aggiornata correttamente.');
    } catch (errore: unknown) {
      this.errore.set(
        errore instanceof Error
          ? errore.message
          : 'Non è stato possibile aggiornare la password.',
      );
    } finally {
      this.salvataggio.set(false);
    }
  }
}
