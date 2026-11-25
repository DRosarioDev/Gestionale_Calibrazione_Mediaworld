export type ColoreBase = 'rosso' | 'verde' | 'blu' | 'ciano' | 'magenta' | 'giallo';

export interface Rgb {
  r: number;
  g: number;
  b: number;
}

/**
 * I tre valori che il tecnico inserisce nella TV per un singolo colore.
 * Non c'e' alcuna conversione: sono esattamente i numeri letti/scritti nel
 * menu della televisione (l'ordine e il significato dipendono dal modello).
 */
export interface ColoreCalibrazione {
  valore1: number;
  valore2: number;
  valore3: number;
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
