import { Injectable } from '@angular/core';
import { AuthTokens } from '../models/auth-state.model';

const ACCESS_TOKEN_KEY = 'govos.accessToken';
const REFRESH_TOKEN_KEY = 'govos.refreshToken';
const EXPIRES_IN_KEY = 'govos.expiresIn';
const TOKEN_TYPE_KEY = 'govos.tokenType';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  save(tokens: AuthTokens): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
    localStorage.setItem(EXPIRES_IN_KEY, String(tokens.expiresIn));
    localStorage.setItem(TOKEN_TYPE_KEY, tokens.tokenType);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  clear(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(EXPIRES_IN_KEY);
    localStorage.removeItem(TOKEN_TYPE_KEY);
  }
}
