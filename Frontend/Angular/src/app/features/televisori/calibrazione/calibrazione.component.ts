import { Component, computed, inject, OnInit, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { TelevisoriService } from '../../../core/services/televisori.service';
import { ToastService } from '../../../core/services/toast.service';
import {
  Calibrazione,
  COLORI,
  ColoreBase,
  ColoreCalibrazione,
  ParametroCalibrazione,
  ValoriCalibrazione,
} from '../../../core/models/calibrazione.model';
import { nomeCompleto } from '../../../core/models/utente.model';
import { formattaData, formattaDataOra } from '../../../core/utils/data.util';
import { StatoBadgeComponent } from '../../../shared/components/stato-badge/stato-badge.component';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { RangeFieldComponent } from '../../../shared/components/range-field/range-field.component';

type GruppoParametro = FormGroup<{
  etichetta: FormControl<string>;
  valore: FormControl<string>;
}>;

type GruppoColore = FormGroup<{
  valore1: FormControl<number>;
  valore2: FormControl<number>;
  valore3: FormControl<number>;
}>;

type CampoColore = 'valore1' | 'valore2' | 'valore3';

function etichettaNonVuota(controllo: AbstractControl<string>): ValidationErrors | null {
  return controllo.value.trim().length > 0 ? null : { required: true };
}

@Component({
  selector: 'app-calibrazione',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule, StatoBadgeComponent, IconComponent, RangeFieldComponent],
  templateUrl: './calibrazione.component.html',
  styleUrl: './calibrazione.component.css',
})
export class CalibrazioneComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly televisoriService = inject(TelevisoriService);
  private readonly auth = inject(AuthService);
  private readonly toast = inject(ToastService);

  readonly id = signal('');
  readonly televisore = computed(() => this.televisoriService.getById(this.id()));
  readonly colori = COLORI;
  readonly coloreSelezionato = signal<ColoreBase>('rosso');
  readonly salvataggio = signal(false);
  readonly esistente = signal(false);

  readonly form = this.fb.nonNullable.group({
    parametri: this.fb.array<GruppoParametro>([]),
    biancoGain: this.fb.nonNullable.group({
      r: [0, [Validators.required, Validators.min(-50), Validators.max(50)]],
      g: [0, [Validators.required, Validators.min(-50), Validators.max(50)]],
      b: [0, [Validators.required, Validators.min(-50), Validators.max(50)]],
    }),
    biancoOffset: this.fb.nonNullable.group({
      r: [0, [Validators.required, Validators.min(-50), Validators.max(50)]],
      g: [0, [Validators.required, Validators.min(-50), Validators.max(50)]],
      b: [0, [Validators.required, Validators.min(-50), Validators.max(50)]],
    }),
    coloriAbilitati: [true],
    colori: this.fb.nonNullable.group({
      rosso: this.creaGruppoColore(),
      verde: this.creaGruppoColore(),
      blu: this.creaGruppoColore(),
      ciano: this.creaGruppoColore(),
      magenta: this.creaGruppoColore(),
      giallo: this.creaGruppoColore(),
    }),
    note: [''],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id') ?? '';
    this.id.set(id);

    const tv = this.televisoriService.getById(id);
    if (!tv) {
      this.toast.errore('Televisione non trovata.');
      void this.router.navigateByUrl('/televisori');
      return;
    }

    if (tv.calibrazione) {
      this.caricaCalibrazione(tv.calibrazione);
      this.esistente.set(true);
    }

    if (tv.stato === 'DA_CALIBRARE') {
      void this.avviaCalibrazione(tv.id);
    }
  }

  get parametri(): GruppoParametro[] {
    return this.form.controls.parametri.controls;
  }

  get coloreAttivo(): { chiave: ColoreBase; etichetta: string; hex: string } {
    return this.colori.find((colore) => colore.chiave === this.coloreSelezionato()) ?? this.colori[0];
  }

  get dettaglioColore(): string {
    return this.riassuntoColore(this.coloreSelezionato());
  }

  aggiungiParametro(): void {
    this.form.controls.parametri.push(this.creaGruppoParametro());
  }

  rimuoviParametro(indice: number): void {
    this.form.controls.parametri.removeAt(indice);
  }

  riassuntoColore(chiave: ColoreBase): string {
    const gruppo = this.form.controls.colori.controls[chiave];
    return `${gruppo.controls.valore1.value} · ${gruppo.controls.valore2.value} · ${gruppo.controls.valore3.value}`;
  }

  controlloColore(campo: CampoColore): FormControl<number> {
    return this.form.controls.colori.controls[this.coloreSelezionato()].controls[campo];
  }

  async salva(): Promise<void> {
    this.rimuoviParametriVuoti();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.toast.errore('Controlla i valori evidenziati prima di salvare la calibrazione.');
      return;
    }

    const tv = this.televisore();
    const utente = this.auth.utente();
    if (!tv || !utente) {
      this.toast.errore('Sessione non valida. Effettua di nuovo l’accesso.');
      return;
    }

    this.salvataggio.set(true);

    try {
      const raw = this.form.getRawValue();
      const valori: ValoriCalibrazione = {
        parametri: this.estraiParametri(),
        biancoGain: {
          r: Number(raw.biancoGain.r),
          g: Number(raw.biancoGain.g),
          b: Number(raw.biancoGain.b),
        },
        biancoOffset: {
          r: Number(raw.biancoOffset.r),
          g: Number(raw.biancoOffset.g),
          b: Number(raw.biancoOffset.b),
        },
        coloriAbilitati: raw.coloriAbilitati,
        colori: this.estraiColori(),
      };

      await this.televisoriService.salvaCalibrazione(tv.id, valori, {
        tecnico: nomeCompleto(utente),
        note: raw.note,
      });

      this.toast.successo(`Calibrazione di ${tv.marca} ${tv.modello} salvata.`);
      await this.router.navigate(['/televisori', tv.id]);
    } catch (errore: unknown) {
      this.toast.errore(
        errore instanceof Error ? errore.message : 'Errore durante il salvataggio della calibrazione.',
      );
    } finally {
      this.salvataggio.set(false);
    }
  }

  annulla(): void {
    const tv = this.televisore();
    void this.router.navigate(tv ? ['/televisori', tv.id] : ['/televisori']);
  }

  data(iso: string | null | undefined): string {
    return formattaData(iso);
  }

  dataOra(iso: string | null | undefined): string {
    return formattaDataOra(iso);
  }

  private async avviaCalibrazione(id: string): Promise<void> {
    try {
      await this.televisoriService.avviaCalibrazione(id);
    } catch (errore: unknown) {
      this.toast.errore(
        errore instanceof Error ? errore.message : 'Impossibile avviare la calibrazione.',
      );
    }
  }

  private creaGruppoParametro(etichetta = '', valore = ''): GruppoParametro {
    return this.fb.nonNullable.group({
      etichetta: [etichetta, [etichettaNonVuota, Validators.maxLength(80)]],
      valore: [valore, [Validators.maxLength(120)]],
    });
  }

  private creaGruppoColore(valore1 = 0, valore2 = 0, valore3 = 0): GruppoColore {
    return this.fb.nonNullable.group({
      valore1: [valore1, [Validators.required]],
      valore2: [valore2, [Validators.required]],
      valore3: [valore3, [Validators.required]],
    });
  }

  private caricaCalibrazione(calibrazione: Calibrazione): void {
    const valori = calibrazione.valori;
    this.form.patchValue({
      biancoGain: { ...valori.biancoGain },
      biancoOffset: { ...valori.biancoOffset },
      coloriAbilitati: valori.coloriAbilitati ?? true,
      note: calibrazione.note,
    });

    this.form.controls.parametri.clear();
    for (const parametro of valori.parametri ?? []) {
      this.form.controls.parametri.push(
        this.creaGruppoParametro(parametro.etichetta, parametro.valore),
      );
    }

    for (const colore of this.colori) {
      const gruppo = this.form.controls.colori.controls[colore.chiave];
      const dati = valori.colori[colore.chiave];
      gruppo.patchValue({
        valore1: dati.valore1,
        valore2: dati.valore2,
        valore3: dati.valore3,
      });
    }
  }

  private rimuoviParametriVuoti(): void {
    const controlli = this.form.controls.parametri;
    for (let indice = controlli.length - 1; indice >= 0; indice--) {
      const gruppo = controlli.at(indice);
      if (!gruppo.controls.etichetta.value.trim() && !gruppo.controls.valore.value.trim()) {
        controlli.removeAt(indice);
      }
    }
  }

  private estraiParametri(): ParametroCalibrazione[] {
    return this.form.controls.parametri.controls
      .map((gruppo) => ({
        etichetta: gruppo.controls.etichetta.value.trim(),
        valore: gruppo.controls.valore.value.trim(),
      }))
      .filter((parametro) => parametro.etichetta.length > 0);
  }

  private estraiColori(): Record<ColoreBase, ColoreCalibrazione> {
    const colori = {} as Record<ColoreBase, ColoreCalibrazione>;
    for (const colore of this.colori) {
      const gruppo = this.form.controls.colori.controls[colore.chiave];
      colori[colore.chiave] = {
        valore1: gruppo.controls.valore1.value,
        valore2: gruppo.controls.valore2.value,
        valore3: gruppo.controls.valore3.value,
      };
    }
    return colori;
  }
}
