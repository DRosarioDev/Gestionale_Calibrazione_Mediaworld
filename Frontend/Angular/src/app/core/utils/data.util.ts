const formatoData = new Intl.DateTimeFormat('it-IT', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
});

const formatoDataCompleta = new Intl.DateTimeFormat('it-IT', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
});

const formatoDataOra = new Intl.DateTimeFormat('it-IT', {
  day: '2-digit',
  month: '2-digit',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

const formatoMese = new Intl.DateTimeFormat('it-IT', { month: 'short' });

export function capitalizza(testo: string): string {
  return testo.charAt(0).toUpperCase() + testo.slice(1);
}

export function formattaData(iso: string | null | undefined): string {
  if (!iso) return '—';
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) return '—';
  return formatoData.format(data);
}

export function formattaDataCompleta(iso: string | null | undefined): string {
  if (!iso) return '—';
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) return '—';
  return formatoDataCompleta.format(data);
}

export function formattaDataOra(iso: string | null | undefined): string {
  if (!iso) return '—';
  const data = new Date(iso);
  if (Number.isNaN(data.getTime())) return '—';
  return formatoDataOra.format(data);
}

export function etichettaMese(data: Date): string {
  return capitalizza(formatoMese.format(data).replace('.', ''));
}

export function oggiEsteso(): string {
  return capitalizza(
    new Intl.DateTimeFormat('it-IT', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(new Date()),
  );
}

export function dataGiorniFa(giorni: number, ora = 10): string {
  const data = new Date();
  data.setDate(data.getDate() - giorni);
  data.setHours(ora, 0, 0, 0);
  return data.toISOString();
}

export function dataPerFile(iso: string | null | undefined): string {
  const data = iso ? new Date(iso) : new Date();
  const anno = data.getFullYear();
  const mese = `${data.getMonth() + 1}`.padStart(2, '0');
  const giorno = `${data.getDate()}`.padStart(2, '0');
  return `${anno}${mese}${giorno}`;
}

export function nomeFileSicuro(testo: string): string {
  return testo
    .trim()
    .replace(/[^a-zA-Z0-9-_]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '');
}
