export type Ruolo = 'ADMIN' | 'TECNICO';

export interface Utente {
  id: string;
  nome: string;
  cognome: string;
  email: string;
  ruolo: Ruolo;
  attivo: boolean;
  dataCreazione: string;
}

export interface NuovoUtente {
  nome: string;
  cognome: string;
  email: string;
  ruolo: Ruolo;
  password: string;
}

export const ETICHETTE_RUOLO: Record<Ruolo, string> = {
  ADMIN: 'Amministratore',
  TECNICO: 'Tecnico',
};

export function nomeCompleto(utente: Pick<Utente, 'nome' | 'cognome'>): string {
  return `${utente.nome} ${utente.cognome}`.trim();
}

export function iniziali(utente: Pick<Utente, 'nome' | 'cognome'>): string {
  return `${utente.nome.charAt(0)}${utente.cognome.charAt(0)}`.toUpperCase();
}
