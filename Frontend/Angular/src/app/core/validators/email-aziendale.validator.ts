import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

const DOMINIO = '@media-saturn.com';

export const emailAziendaleValidator: ValidatorFn = (
  control: AbstractControl,
): ValidationErrors | null => {
  const valore = control.value;
  if (typeof valore !== 'string' || valore.trim() === '') return null;

  return valore.trim().toLowerCase().endsWith(DOMINIO) ? null : { dominioNonValido: true };
};
