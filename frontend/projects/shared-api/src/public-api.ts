/*
 * Public API Surface of shared-api — OpenAPI generated client
 */

export * from './lib/generated';
export { BASE_PATH } from './lib/generated/variables';
export { Configuration } from './lib/generated/configuration';
export type { ConfigurationParameters } from './lib/generated/configuration';

import { BASE_PATH } from './lib/generated/variables';

export function provideGovosApi(basePath: string) {
  return [{ provide: BASE_PATH, useValue: basePath }];
}
