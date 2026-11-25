import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { TelevisoriService } from '../../../core/services/televisori.service';
import { ToastService } from '../../../core/services/toast.service';
import { MARCHE, TECNOLOGIE } from '../../../core/models/televisore.model';
import { PageHeaderComponent } from '../../../shared/components/page-header/page-header.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';

@Component({
  selector: 'app-nuovo-televisore',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, PageHeaderComponent, IconComponent],
  templateUrl: './nuovo-televisore.component.html',
  styleUrl: './nuovo-televisore.component.css',
})
export class NuovoTelevisoreComponent {
  private readonly fb = inject(FormBuilder);
  private readonly televisoriService = inject(TelevisoriService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly marche = MARCHE;
  readonly tecnologie = TECNOLOGIE;

  readonly invio = signal(false);
  readonly errore = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    marca: [MARCHE[0] as string, [Validators.required]],
    modello: ['', [Validators.required, Validators.minLength(2)]],
    codiceSeriale: ['', [Validators.maxLength(100)]],
    pollici: [55, [Validators.required, Validators.min(10), Validators.max(150)]],
    tecnologia: [TECNOLOGIE[0] as string, [Validators.required]],
    note: [''],
  });

  async salva(avviaCalibrazione = false): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.errore.set('Completa i campi obbligatori prima di salvare.');
      return;
    }

    this.invio.set(true);
    this.errore.set(null);

    try {
      const valori = this.form.getRawValue();
      const tv = await this.televisoriService.aggiungi({
        marca: valori.marca,
        modello: valori.modello,
        codiceSeriale: valori.codiceSeriale,
        pollici: Number(valori.pollici),
        tecnologia: valori.tecnologia,
        note: valori.note,
      });

      this.toast.successo(`${tv.marca} ${tv.modello} aggiunta al parco TV.`);

      if (avviaCalibrazione) {
        await this.televisoriService.avviaCalibrazione(tv.id);
        await this.router.navigate(['/televisori', tv.id, 'calibrazione']);
      } else {
        await this.router.navigate(['/televisori', tv.id]);
      }
    } catch (errore: unknown) {
      this.errore.set(
        errore instanceof Error ? errore.message : 'Non è stato possibile salvare la televisione.',
      );
    } finally {
      this.invio.set(false);
    }
  }
}
