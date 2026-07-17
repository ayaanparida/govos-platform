export interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  tokenType: string;
}

export interface AuthUser {
  userId?: string;
  username?: string;
  email?: string;
  roles: string[];
  permissions: string[];
}

export interface AuthState {
  authenticated: boolean;
  user: AuthUser | null;
  tokens: AuthTokens | null;
}

export const EMPTY_AUTH_STATE: AuthState = {
  authenticated: false,
  user: null,
  tokens: null,
};
