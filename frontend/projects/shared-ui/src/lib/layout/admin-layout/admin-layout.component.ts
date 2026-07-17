import { Component, inject, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { AdminToolbarComponent } from '../admin-toolbar/admin-toolbar.component';
import { AdminSidebarComponent } from '../admin-sidebar/admin-sidebar.component';
import { AdminBreadcrumbComponent } from '../admin-breadcrumb/admin-breadcrumb.component';
import { AdminFooterComponent } from '../admin-footer/admin-footer.component';
import { ADMIN_NAV_ITEMS } from '../../models/nav-item.model';
import { GovosAuthService } from 'shared-auth';

@Component({
  selector: 'govos-admin-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    MatSidenavModule,
    AdminToolbarComponent,
    AdminSidebarComponent,
    AdminBreadcrumbComponent,
    AdminFooterComponent,
  ],
  template: `
    <mat-sidenav-container class="govos-shell">
      <mat-sidenav
        mode="side"
        [opened]="sidebarOpen()"
        [fixedInViewport]="true"
        class="layout-sidenav">
        <govos-admin-sidebar [items]="navItems" />
      </mat-sidenav>

      <mat-sidenav-content>
        <govos-admin-toolbar
          [username]="authService.user()?.username ?? 'Administrator'"
          (menuToggle)="toggleSidebar()"
          (logout)="onLogout()" />
        <govos-admin-breadcrumb [segments]="['Administration', 'Dashboard']" />
        <main class="govos-content">
          <router-outlet />
        </main>
        <govos-admin-footer />
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: `
    .layout-sidenav {
      width: var(--govos-sidebar-width);
      border-right: 0;
    }
  `,
})
export class AdminLayoutComponent {
  protected readonly authService = inject(GovosAuthService);
  protected readonly navItems = ADMIN_NAV_ITEMS;
  protected readonly sidebarOpen = signal(true);

  toggleSidebar(): void {
    this.sidebarOpen.update((open) => !open);
  }

  async onLogout(): Promise<void> {
    await this.authService.logout();
  }
}
