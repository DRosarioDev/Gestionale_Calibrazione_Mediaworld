import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { adminGuard, authGuard, ospiteGuard } from './core/auth/auth.guard';
import { ShellComponent } from './layout/shell/shell.component';

const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then((m) => m.LoginComponent),
    canActivate: [ospiteGuard],
    title: 'Accedi · MediaWorld Calibration Suite',
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
        title: 'Panoramica · MediaWorld Calibration Suite',
      },
      {
        path: 'televisori',
        loadComponent: () =>
          import('./features/televisori/lista-televisori/lista-televisori.component').then(
            (m) => m.ListaTelevisoriComponent,
          ),
        title: 'Televisori · MediaWorld Calibration Suite',
      },
      {
        path: 'televisori/nuovo',
        loadComponent: () =>
          import('./features/televisori/nuovo-televisore/nuovo-televisore.component').then(
            (m) => m.NuovoTelevisoreComponent,
          ),
        title: 'Nuova televisione · MediaWorld Calibration Suite',
      },
      {
        path: 'televisori/:id/modifica',
        loadComponent: () =>
          import('./features/televisori/nuovo-televisore/nuovo-televisore.component').then(
            (m) => m.NuovoTelevisoreComponent,
          ),
        title: 'Modifica TV · MediaWorld Calibration Suite',
      },
      {
        path: 'televisori/:id',
        loadComponent: () =>
          import('./features/televisori/dettaglio-televisore/dettaglio-televisore.component').then(
            (m) => m.DettaglioTelevisoreComponent,
          ),
        title: 'Dettaglio TV · MediaWorld Calibration Suite',
      },
      {
        path: 'televisori/:id/calibrazione',
        loadComponent: () =>
          import('./features/televisori/calibrazione/calibrazione.component').then(
            (m) => m.CalibrazioneComponent,
          ),
        title: 'Calibrazione · MediaWorld Calibration Suite',
      },
      {
        path: 'profilo',
        loadComponent: () =>
          import('./features/account/profilo/profilo.component').then((m) => m.ProfiloComponent),
        title: 'Il mio account · MediaWorld Calibration Suite',
      },
      {
        path: 'admin/utenti',
        loadComponent: () =>
          import('./features/admin/utenti/utenti.component').then((m) => m.UtentiComponent),
        canActivate: [adminGuard],
        title: 'Gestione utenti · MediaWorld Calibration Suite',
      },
    ],
  },
  {
    path: '**',
    loadComponent: () =>
      import('./features/pagina-non-trovata/pagina-non-trovata.component').then(
        (m) => m.PaginaNonTrovataComponent,
      ),
    title: 'Pagina non trovata · MediaWorld Calibration Suite',
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'top' })],
  exports: [RouterModule],
})
export class AppRoutingModule {}
