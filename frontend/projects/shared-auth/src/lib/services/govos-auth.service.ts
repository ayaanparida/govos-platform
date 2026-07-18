import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationApiService, LoginRequest } from 'shared-api';
import { firstValueFrom, map } from 'rxjs';
import { AuthStore } from '../store/auth.store';
import { AuthUser } from '../models/auth-state.model';
import { TokenStorageService } from '../services/token-storage.service';

@Injectable({ providedIn: 'root' })
export class GovosAuthService {
  private readonly authenticationApi = inject(AuthenticationApiService);
  private readonly authStore = inject(AuthStore);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly router = inject(Router);

  readonly authenticated = this.authStore.authenticated;
  readonly user = this.authStore.user;

  async login(credentials: LoginRequest): Promise<void> {
    const response = await firstValueFrom(this.authenticationApi.login(credentials));
    const data = response.data;
    if (!data?.accessToken || !data.refreshToken) {
      throw new Error('Login response did not include tokens');
    }

    const user = this.toAuthUser(data.user);
    this.authStore.setSession(
      user,
      data.accessToken,
      data.refreshToken,
      data.expiresIn ?? 900,
      data.tokenType ?? 'Bearer',
    );
  }

  async refreshSession(): Promise<boolean> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (!refreshToken) {
      return false;
    }

    try {
      const response = await firstValueFrom(
        this.authenticationApi.refresh({ refreshToken }),
      );
      const data = response.data;
      if (!data?.accessToken || !data.refreshToken) {
        return false;
      }

      const user = this.toAuthUser(data.user);
      this.authStore.setSession(
        user,
        data.accessToken,
        data.refreshToken,
        data.expiresIn ?? 900,
        data.tokenType ?? 'Bearer',
      );
      return true;
    } catch {
      this.authStore.clearSession();
      return false;
    }
  }

  async loadCurrentUser(): Promise<void> {
    const response = await firstValueFrom(this.authenticationApi.getCurrentUser());
    const data = response.data;
    if (!data?.authenticated) {
      this.authStore.clearSession();
      return;
    }

    this.authStore.setAuthenticated({
      userId: data.userId,
      username: data.username,
      email: data.email,
      roles: data.roles ?? [],
      permissions: data.permissions ?? [],
    });
  }

  async logout(): Promise<void> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (refreshToken) {
      try {
        await firstValueFrom(this.authenticationApi.logout({ refreshToken }));
      } catch {
        // Session cleanup continues even if server logout fails.
      }
    }
    this.authStore.clearSession();
    await this.router.navigateByUrl('/auth/login');
  }

  private toAuthUser(user: {
    userId?: string;
    username?: string;
    email?: string;
    roles?: Array<string>;
    permissions?: Array<string>;
  } | undefined): AuthUser {
    return {
      userId: user?.userId,
      username: user?.username,
      email: user?.email,
      roles: user?.roles ?? [],
      permissions: user?.permissions ?? [],
    };
  }
}
