import { Hsl, Rgb } from '../models/calibrazione.model';

export function limita(valore: number, minimo: number, massimo: number): number {
  return Math.min(massimo, Math.max(minimo, valore));
}

export function arrotonda(valore: number, decimali = 0): number {
  const fattore = Math.pow(10, decimali);
  return Math.round(valore * fattore) / fattore;
}

/** Converte un colore RGB (0-255) in HSL (h 0-360, s/l 0-100). */
export function rgbToHsl(rgb: Rgb): Hsl {
  const r = limita(rgb.r, 0, 255) / 255;
  const g = limita(rgb.g, 0, 255) / 255;
  const b = limita(rgb.b, 0, 255) / 255;

  const max = Math.max(r, g, b);
  const min = Math.min(r, g, b);
  const delta = max - min;

  let h = 0;
  if (delta !== 0) {
    if (max === r) h = ((g - b) / delta) % 6;
    else if (max === g) h = (b - r) / delta + 2;
    else h = (r - g) / delta + 4;
    h *= 60;
    if (h < 0) h += 360;
  }

  const l = (max + min) / 2;
  const s = delta === 0 ? 0 : delta / (1 - Math.abs(2 * l - 1));

  return { h: Math.round(h), s: Math.round(s * 100), l: Math.round(l * 100) };
}

/** Converte un colore HSL (h 0-360, s/l 0-100) in RGB (0-255). */
export function hslToRgb(hsl: Hsl): Rgb {
  const h = ((hsl.h % 360) + 360) % 360;
  const s = limita(hsl.s, 0, 100) / 100;
  const l = limita(hsl.l, 0, 100) / 100;

  const c = (1 - Math.abs(2 * l - 1)) * s;
  const x = c * (1 - Math.abs(((h / 60) % 2) - 1));
  const m = l - c / 2;

  let rp = 0;
  let gp = 0;
  let bp = 0;
  if (h < 60) [rp, gp, bp] = [c, x, 0];
  else if (h < 120) [rp, gp, bp] = [x, c, 0];
  else if (h < 180) [rp, gp, bp] = [0, c, x];
  else if (h < 240) [rp, gp, bp] = [0, x, c];
  else if (h < 300) [rp, gp, bp] = [x, 0, c];
  else [rp, gp, bp] = [c, 0, x];

  return {
    r: Math.round((rp + m) * 255),
    g: Math.round((gp + m) * 255),
    b: Math.round((bp + m) * 255),
  };
}

export function hslCss(hsl: Hsl, alpha = 1): string {
  return `hsla(${hsl.h}, ${hsl.s}%, ${hsl.l}%, ${alpha})`;
}

export function rgbCss(rgb: Rgb): string {
  return `rgb(${rgb.r}, ${rgb.g}, ${rgb.b})`;
}

export function formattaConSegno(valore: number): string {
  return valore > 0 ? `+${valore}` : `${valore}`;
}
