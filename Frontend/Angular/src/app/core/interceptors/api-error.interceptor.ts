import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

interface ErroreApi {
  message?: string;
}

/**
 * Trasforma le risposte di errore delle API in Error con il messaggio
 * leggibile restituito dal backend, così i componenti possono mostrarlo
 * direttamente all'utente.
 */
export const apiErrorInterceptor: HttpInterceptorFn = (request, next) =>
  next(request).pipe(
    catchError((errore: unknown) => {
      if (!(errore instanceof HttpErrorResponse)) {
        return throwError(() => errore);
      }

      const corpo = errore.error as ErroreApi | string | null;
      let messaggio = 'Si è verificato un errore. Riprova.';

      if (typeof corpo === 'string' && corpo.trim()) {
        messaggio = corpo;
      } else if (corpo && typeof corpo === 'object' && typeof corpo.message === 'string') {
        messaggio = corpo.message;
      } else if (errore.status === 0) {
        messaggio = 'Server non raggiungibile. Verifica che il backend sia avviato.';
      } else if (errore.status === 401) {
        messaggio = 'Sessione scaduta o credenziali non valide. Effettua di nuovo l’accesso.';
      } else if (errore.status === 403) {
        messaggio = 'Non hai i permessi necessari per questa operazione.';
      }

      return throwError(() => new Error(messaggio));
    }),
  );
