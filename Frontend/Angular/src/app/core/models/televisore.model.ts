import { Calibrazione } from './calibrazione.model';

export type StatoTv = 'DA_CALIBRARE' | 'IN_CALIBRAZIONE' | 'CALIBRATO';

export interface Televisore {
  id: string;
  marca: string;
  modello: string;
  codiceSeriale: string;
  pollici: number;
  tecnologia: string;
  stato: StatoTv;
  dataAggiunta: string;
  dataCalibrazione: string | null;
  aggiuntoDa: string;
  note: string;
  calibrazione: Calibrazione | null;
}

export interface NuovoTelevisore {
  marca: string;
  modello: string;
  codiceSeriale: string;
  pollici: number;
  tecnologia: string;
  note: string;
}

export const MARCHE = [
  'Samsung',
  'LG',
  'Sony',
  'Philips',
  'Panasonic',
  'Hisense',
  'TCL',
  'Sharp',
  'Altro',
] as const;

export const TECNOLOGIE = ['OLED', 'QLED', 'Neo QLED', 'Mini LED', 'LED', 'Micro LED', 'Altro'] as const;

export const ETICHETTE_STATO: Record<StatoTv, string> = {
  DA_CALIBRARE: 'Da calibrare',
  IN_CALIBRAZIONE: 'In calibrazione',
  CALIBRATO: 'Calibrato',
};

export function descrizioneTv(tv: Pick<Televisore, 'marca' | 'modello'>): string {
  return `${tv.marca} ${tv.modello}`;
}
