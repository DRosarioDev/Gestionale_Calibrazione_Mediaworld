export function formattaConSegno(valore: number): string {
  return valore > 0 ? `+${valore}` : `${valore}`;
}
