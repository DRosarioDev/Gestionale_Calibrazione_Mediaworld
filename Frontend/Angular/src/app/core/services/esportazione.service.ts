import { Injectable } from '@angular/core';
import { COLORI, ValoriCalibrazione } from '../models/calibrazione.model';
import { Televisore } from '../models/televisore.model';
import { formattaConSegno } from '../utils/colore.util';
import { dataPerFile, formattaDataCompleta, formattaDataOra, nomeFileSicuro } from '../utils/data.util';

@Injectable({ providedIn: 'root' })
export class EsportazioneService {
  costruisciTesto(tv: Televisore): string {
    const righe: string[] = [];
    const sep = '='.repeat(64);
    const sotto = '-'.repeat(64);

    const riga = (etichetta: string, valore: string | number): string =>
      `${etichetta.padEnd(24, '.')} ${valore}`;

    righe.push(sep);
    righe.push('        MEDIAWORLD - SCHEDA DI CALIBRAZIONE');
    righe.push('              Centro di Arese');
    righe.push(sep);
    righe.push('');
    righe.push('DATI TELEVISORE');
    righe.push(sotto);
    righe.push(riga('Marca', tv.marca));
    righe.push(riga('Modello', tv.modello));
    righe.push(riga('Pollici', `${tv.pollici}"`));
    righe.push(riga('Tecnologia', tv.tecnologia));

    if (!tv.calibrazione) {
      righe.push('');
      righe.push(sotto);
      righe.push('Calibrazione non ancora eseguita.');
      righe.push(sotto);
      return righe.join('\r\n');
    }

    const cal = tv.calibrazione;
    const valori = cal.valori;

    righe.push('');
    righe.push('CALIBRAZIONE');
    righe.push(sotto);

    righe.push('');
    righe.push('PARAMETRI');
    righe.push(sotto);
    if (!valori.parametri || valori.parametri.length === 0) {
      righe.push('Nessun parametro registrato.');
    } else {
      for (const parametro of valori.parametri) {
        righe.push(riga(parametro.etichetta, parametro.valore));
      }
    }

    righe.push('');
    righe.push('BILANCIAMENTO BIANCO');
    righe.push(sotto);
    righe.push(
      riga(
        'Bianco Gain',
        `R ${formattaConSegno(valori.biancoGain.r)}  G ${formattaConSegno(valori.biancoGain.g)}  B ${formattaConSegno(valori.biancoGain.b)}`,
      ),
    );
    righe.push(
      riga(
        'Bianco Offset',
        `R ${formattaConSegno(valori.biancoOffset.r)}  G ${formattaConSegno(valori.biancoOffset.g)}  B ${formattaConSegno(valori.biancoOffset.b)}`,
      ),
    );

    if (valori.coloriAbilitati) {
      righe.push('');
      righe.push('COLORI');
      righe.push(sotto);
      for (const colore of COLORI) {
        const dati = valori.colori[colore.chiave];
        righe.push(
          riga(
            colore.etichetta,
            `${String(dati.valore1).padStart(4)}  ${String(dati.valore2).padStart(4)}  ${String(dati.valore3).padStart(4)}`,
          ),
        );
      }
    } else {
      righe.push('');
    }

    if (cal.note.trim()) {
      righe.push('');
      righe.push('NOTE');
      righe.push(sotto);
      righe.push(cal.note.trim());
    }


    return righe.join('\r\n');
  }

  nomeFilePredefinito(tv: Televisore): string {
    const data = dataPerFile(tv.dataCalibrazione);
    const identificativo = nomeFileSicuro(tv.codiceSeriale) || nomeFileSicuro(tv.modello) || 'tv';
    return `MediaWorld_calibrazione_${identificativo}_${data}.txt`;
  }

  scarica(nomeFile: string, contenuto: string): void {
    const nome = nomeFile.trim().toLowerCase().endsWith('.txt')
      ? nomeFile.trim()
      : `${nomeFile.trim()}.txt`;
    const blob = new Blob([contenuto], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = nome || 'calibrazione.txt';
    document.body.appendChild(link);
    link.click();
    link.remove();
    URL.revokeObjectURL(url);
  }

  async copia(testo: string): Promise<void> {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(testo);
      return;
    }

    const area = document.createElement('textarea');
    area.value = testo;
    area.style.position = 'fixed';
    area.style.opacity = '0';
    document.body.appendChild(area);
    area.select();
    document.execCommand('copy');
    area.remove();
  }

  valoriRiepilogo(valori: ValoriCalibrazione): Array<{ etichetta: string; valore: string }> {
    return (valori.parametri ?? []).map((parametro) => ({
      etichetta: parametro.etichetta,
      valore: parametro.valore || '—',
    }));
  }
}
