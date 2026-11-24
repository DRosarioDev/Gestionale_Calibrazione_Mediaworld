export type ModalitaColore = 'RGB' | 'HSL';

export type ColoreBase = 'rosso' | 'verde' | 'blu' | 'ciano' | 'magenta' | 'giallo';

export interface Rgb {
  r: number;
  g: number;
  b: number;
}

export interface Hsl {
  h: number;
  s: number;
  l: number;
}

export interface ColoreCalibrazione {
  rgb: Rgb;
  hsl: Hsl;
}

/**
 * Parametro personalizzato della calibrazione.
 * Ogni televisione espone parametri diversi, quindi nome e valore sono liberi.
 */
export interface ParametroCalibrazione {
  etichetta: string;
  valore: string;
}

export interface ValoriCalibrazione {
  parametri: ParametroCalibrazione[];
  biancoGain: Rgb;
  biancoOffset: Rgb;
  /** Se false la calibrazione riguarda solo la regolazione bianco/nero, senza RGBCMY. */
  coloriAbilitati: boolean;
  modalitaColore: ModalitaColore;
  colori: Record<ColoreBase, ColoreCalibrazione>;
}

export interface Calibrazione {
  dataCalibrazione: string;
  tecnico: string;
  note: string;
  valori: ValoriCalibrazione;
}

export interface MetaCalibrazione {
  tecnico: string;
  note: string;
}

export const COLORI: ReadonlyArray<{ chiave: ColoreBase; etichetta: string; hex: string }> = [
  { chiave: 'rosso', etichetta: 'Rosso', hex: '#ef4444' },
  { chiave: 'verde', etichetta: 'Verde', hex: '#22c55e' },
  { chiave: 'blu', etichetta: 'Blu', hex: '#3b82f6' },
  { chiave: 'ciano', etichetta: 'Ciano', hex: '#06b6d4' },
  { chiave: 'magenta', etichetta: 'Magenta', hex: '#d946ef' },
  { chiave: 'giallo', etichetta: 'Giallo', hex: '#eab308' },
];
