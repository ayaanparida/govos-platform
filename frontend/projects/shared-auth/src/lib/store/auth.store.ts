import { Injectable, computed, signal } from '@angular/core';
import { AuthState, AuthUser, EMPTY_AUTH_STATE } from '../models/auth-state.model';
import { TokenStorageService } from '../services/token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthStore {
  private readonly state = signal<AuthState>(EMPTY_AUTH_STATE);

  readonly authenticated = computed(() => this.state().authenticated);
  readonly user = computed(() => this.state().user);
  readonly displayName = computed(() => this.state().user?.username ?? 'Guest');

  constructor(private readonly tokenStorage: TokenStorageService) {
    this.hydrateFromStorage();
  }

  setAuthenticated(user: AuthUser): void {
    this.state.update((current) => ({
      ...current,
      authenticated: true,
      user,
    }));
  }

  setSession(user: AuthUser, accessToken: string, refreshToken: string, expiresIn: number, tokenType: string): void {
    this.tokenStorage.save({ accessToken, refreshToken, expiresIn, tokenType });
    this.setAuthenticated(user);
  }

  clearSession(): void {
    this.tokenStorage.clear();
    this.state.set(EMPTY_AUTH_STATE);
  }

  snapshot(): AuthState {
    return this.state();
  }

  private hydrateFromStorage(): void {
    const accessToken = this.tokenStorage.getAccessToken();
    if (!accessToken) {
      return;
    }
    this.state.update((current) => ({
      ...current,
      authenticated: true,
    }));
  }
}
