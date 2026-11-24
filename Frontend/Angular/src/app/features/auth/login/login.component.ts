import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TelevisoriService } from '../../../core/services/televisori.service';
import { UtentiService } from '../../../core/services/utenti.service';
import { ToastService } from '../../../core/services/toast.service';
import { nomeCompleto } from '../../../core/models/utente.model';
import { BrandLogoComponent } from '../../../shared/components/brand-logo/brand-logo.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, BrandLogoComponent, IconComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly televisoriService = inject(TelevisoriService);
  private readonly utentiService = inject(UtentiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly toast = inject(ToastService);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  readonly mostraPassword = signal(false);
  readonly caricamento = signal(false);
  readonly errore = signal<string | null>(null);

  compilaDemo(tipo: 'admin' | 'tecnico'): void {
    const credenziali =
      tipo === 'admin'
        ? { email: 'admin@mediaworld.it', password: 'Admin123!' }
        : { email: 'tecnico@mediaworld.it', password: 'Tecnico123!' };

    this.form.setValue(credenziali);
    this.errore.set(null);
    this.toast.info('Credenziali demo inserite. Premi “Accedi”.');
  }

  async accedi(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.caricamento.set(true);
    this.errore.set(null);

    try {
      const { email, password } = this.form.getRawValue();
      const utente = await this.auth.login(email, password);
      await this.televisoriService.carica();
      if (utente.ruolo === 'ADMIN') {
        await this.utentiService.carica();
      }
      this.toast.successo(`Benvenuto, ${nomeCompleto(utente)}.`);
      const redirect = this.route.snapshot.queryParamMap.get('redirect');
      await this.router.navigateByUrl(redirect || '/dashboard');
    } catch (errore: unknown) {
      this.errore.set(
        errore instanceof Error ? errore.message : 'Accesso non riuscito. Riprova.',
      );
    } finally {
      this.caricamento.set(false);
    }
  }
}
