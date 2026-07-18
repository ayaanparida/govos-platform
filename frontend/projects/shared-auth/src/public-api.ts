import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { jwtInterceptor } from './lib/interceptors/jwt.interceptor';

export function provideGovosAuth(): EnvironmentProviders {
  return makeEnvironmentProviders([provideHttpClient(withInterceptors([jwtInterceptor]))]);
}

export * from './lib/models/auth-state.model';
export * from './lib/services/token-storage.service';
export * from './lib/services/govos-auth.service';
export * from './lib/store/auth.store';
export * from './lib/interceptors/jwt.interceptor';
export * from './lib/guards/auth.guard';
