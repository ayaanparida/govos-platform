export interface GovosEnvironment {
  production: boolean;
  apiBaseUrl: string;
  appName: string;
}

export const GOVOS_ENVIRONMENT = 'GOVOS_ENVIRONMENT';

export const DEFAULT_ENVIRONMENT: GovosEnvironment = {
  production: false,
  apiBaseUrl: 'http://localhost:8080',
  appName: 'GovOS Admin',
};
