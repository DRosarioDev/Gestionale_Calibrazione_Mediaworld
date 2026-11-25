import { ParametroCalibrazione } from '../models/calibrazione.model';

export interface EsitoTemplateParametri {
  parametri: ParametroCalibrazione[];
  /** Righe considerate (esclusi vuoti, commenti e righe decorative). */
  righeValutate: number;
  /** Righe considerate ma scartate perche' non nel formato chiave:valore. */
  righeIgnorate: number;
}

const COMMENTO = /^(#|\/\/)/;
const RIGA_DECORATIVA = /^[-=_*~.\s]+$/;
const SEPARATORE = /[:=]|\t|\s{2,}/;

/**
 * Legge un template testuale e lo converte in parametri etichetta/valore.
 *
 * Formati accettati, uno per riga:
 *   ETICHETTA: valore
 *   ETICHETTA = valore
 *   ETICHETTA<TAB>valore
 *   ETICHETTA  valore        (due o piu' spazi)
 *
 * Vengono ignorati: righe vuote, commenti che iniziano con # o // e righe
 * decorative (---, ===, ...). Le righe senza separatore non vengono importate
 * e sono conteggiate in righeIgnorate, cosi' l'anteprima puo' avvisare l'utente.
 */
export function leggiTemplateParametri(contenuto: string): EsitoTemplateParametri {
  const righe = contenuto.replace(/^\uFEFF/, '').split(/\r\n|\r|\n/);
  const parametri: ParametroCalibrazione[] = [];
  let righeValutate = 0;
  let righeIgnorate = 0;

  for (const rigaGrezza of righe) {
    const riga = rigaGrezza.trim();

    if (!riga || COMMENTO.test(riga) || RIGA_DECORATIVA.test(riga)) {
      continue;
    }

    righeValutate++;

    const posizione = riga.search(SEPARATORE);
    if (posizione <= 0) {
      righeIgnorate++;
      continue;
    }

    const etichetta = normalizzaTesto(riga.slice(0, posizione));
    const valore = normalizzaTesto(riga.slice(posizione).replace(/^[:=\s]+/, ''));

    if (!etichetta || etichetta.length > 80 || valore.length > 120) {
      righeIgnorate++;
      continue;
    }

    parametri.push({ etichetta, valore });
  }

  return { parametri, righeValutate, righeIgnorate };
}

function normalizzaTesto(testo: string): string {
  return testo.replace(/\s+/g, ' ').trim();
}
