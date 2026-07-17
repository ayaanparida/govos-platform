export * from './authentication.service';
import { AuthenticationApiService } from './authentication.service';
export * from './authentication.serviceInterface';
export * from './platform.service';
import { PlatformApiService } from './platform.service';
export * from './platform.serviceInterface';
export const APIS = [AuthenticationApiService, PlatformApiService];
