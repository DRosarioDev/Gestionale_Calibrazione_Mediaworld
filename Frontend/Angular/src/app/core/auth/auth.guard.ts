import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.autenticato()) return true;
  return router.createUrlTree(['/login'], { queryParams: { redirect: state.url } });
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.isAdmin()) return true;
  if (!auth.autenticato()) {
    return router.createUrlTree(['/login'], { queryParams: { redirect: '/admin/utenti' } });
  }
  return router.createUrlTree(['/dashboard']);
};

export const ospiteGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.autenticato() ? router.createUrlTree(['/dashboard']) : true;
};
