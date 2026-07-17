import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { GovosAuthService } from '../services/govos-auth.service';

export const authGuard: CanActivateFn = async () => {
  const authService = inject(GovosAuthService);
  const router = inject(Router);

  if (authService.authenticated()) {
    return true;
  }

  const refreshed = await authService.refreshSession();
  if (refreshed) {
    try {
      await authService.loadCurrentUser();
      return true;
    } catch {
      await router.navigateByUrl('/auth/login');
      return false;
    }
  }

  await router.navigateByUrl('/auth/login');
  return false;
};
