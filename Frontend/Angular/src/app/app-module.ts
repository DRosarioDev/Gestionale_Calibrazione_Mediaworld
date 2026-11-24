import {
  inject,
  LOCALE_ID,
  NgModule,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localeIt from '@angular/common/locales/it';
import { BrowserModule } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { ToastContainerComponent } from './shared/components/toast-container/toast-container.component';
import { authTokenInterceptor } from './core/interceptors/auth-token.interceptor';
import { apiErrorInterceptor } from './core/interceptors/api-error.interceptor';
import { AuthService } from './core/services/auth.service';
import { TelevisoriService } from './core/services/televisori.service';
import { UtentiService } from './core/services/utenti.service';

registerLocaleData(localeIt);

/**
 * Ripristina la sessione salvata e carica i dati prima di attivare le rotte,
 * così i componenti possono continuare a leggere i signal in modo sincrono.
 */
function inizializzaDati(): Promise<void> {
  const auth = inject(AuthService);
  const televisori = inject(TelevisoriService);
  const utenti = inject(UtentiService);

  return auth.verificaSessione().then(async (sessioneValida) => {
    if (!sessioneValida) return;

    try {
      await televisori.carica();
      if (auth.isAdmin()) {
        await utenti.carica();
      }
    } catch {
      // Il backend potrebbe essere momentaneamente non raggiungibile:
      // l'utente resta sulla schermata di accesso e può riprovare.
    }
  });
}

@NgModule({
  declarations: [App],
  imports: [BrowserModule, AppRoutingModule, ToastContainerComponent],
  providers: [
    provideBrowserGlobalErrorListeners(),
    { provide: LOCALE_ID, useValue: 'it-IT' },
    provideHttpClient(withInterceptors([authTokenInterceptor, apiErrorInterceptor])),
    provideAppInitializer(inizializzaDati),
  ],
  bootstrap: [App],
})
export class AppModule {}
