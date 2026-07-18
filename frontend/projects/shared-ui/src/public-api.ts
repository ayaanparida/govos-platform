import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

export function provideGovosUi(): EnvironmentProviders {
  return makeEnvironmentProviders([provideAnimationsAsync()]);
}

export * from './lib/layout/admin-layout/admin-layout.component';
export * from './lib/layout/admin-toolbar/admin-toolbar.component';
export * from './lib/layout/admin-sidebar/admin-sidebar.component';
export * from './lib/layout/admin-breadcrumb/admin-breadcrumb.component';
export * from './lib/layout/admin-footer/admin-footer.component';
export * from './lib/layout/notification-panel/notification-panel.component';
export * from './lib/layout/profile-menu/profile-menu.component';
export * from './lib/models/nav-item.model';
