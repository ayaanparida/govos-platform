import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { DEFAULT_ENVIRONMENT, GOVOS_ENVIRONMENT, GovosEnvironment } from './lib/environment/govos-environment';

export function provideGovosCore(environment: Partial<GovosEnvironment> = {}): EnvironmentProviders {
  return makeEnvironmentProviders([
    {
      provide: GOVOS_ENVIRONMENT,
      useValue: { ...DEFAULT_ENVIRONMENT, ...environment } satisfies GovosEnvironment,
    },
  ]);
}

export * from './lib/environment/govos-environment';
