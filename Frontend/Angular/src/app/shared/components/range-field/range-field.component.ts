import {
  Component,
  forwardRef,
  Input,
} from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

let contatoreCampi = 0;

@Component({
  selector: 'app-range-field',
  standalone: true,
  templateUrl: './range-field.component.html',
  styleUrl: './range-field.component.css',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => RangeFieldComponent),
      multi: true,
    },
  ],
})
export class RangeFieldComponent implements ControlValueAccessor {
  @Input({ required: true }) etichetta!: string;
  @Input() minimo = 0;
  @Input() massimo = 100;
  @Input() passo = 1;
  @Input() unita = '';
  @Input() suggerimento = '';
  @Input() accento: 'neutro' | 'rosso' | 'verde' | 'blu' | 'ambra' = 'neutro';

  readonly id = `range-field-${++contatoreCampi}`;
  valore: number | null = null;
  disabilitato = false;

  private onChange: (valore: number | null) => void = () => undefined;
  onTouched: () => void = () => undefined;

  writeValue(valore: number | null): void {
    this.valore = valore ?? null;
  }

  registerOnChange(fn: (valore: number | null) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabilitato: boolean): void {
    this.disabilitato = disabilitato;
  }

  onNumero(event: Event): void {
    const raw = (event.target as HTMLInputElement).value;
    if (raw.trim() === '') {
      this.valore = null;
      this.onChange(null);
      this.onTouched();
      return;
    }
    const numero = Number(raw.replace(',', '.'));
    if (Number.isNaN(numero)) return;
    this.valore = numero;
    this.onChange(numero);
    this.onTouched();
  }

  onRange(event: Event): void {
    const numero = Number((event.target as HTMLInputElement).value);
    this.valore = numero;
    this.onChange(numero);
    this.onTouched();
  }
}
