import { Component, input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { NavItem } from '../../models/nav-item.model';

@Component({
  selector: 'govos-admin-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, MatListModule, MatIconModule],
  template: `
    <nav class="sidebar" aria-label="Primary navigation">
      <div class="sidebar-brand">
        <mat-icon>account_balance</mat-icon>
        <span>GovOS Admin</span>
      </div>
      <mat-nav-list>
        @for (item of items(); track item.route) {
          <a
            mat-list-item
            [routerLink]="item.route"
            routerLinkActive="active"
            [routerLinkActiveOptions]="{ exact: true }">
            <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
            <span matListItemTitle>{{ item.label }}</span>
          </a>
        }
      </mat-nav-list>
    </nav>
  `,
  styles: `
    .sidebar {
      width: var(--govos-sidebar-width);
      height: 100%;
      background: var(--govos-surface);
      border-right: 1px solid var(--govos-border);
      display: flex;
      flex-direction: column;
    }

    .sidebar-brand {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem 1.25rem;
      font-weight: 700;
      border-bottom: 1px solid var(--govos-border);
    }

    .active {
      background: color-mix(in srgb, var(--govos-content-bg) 70%, transparent);
    }
  `,
})
export class AdminSidebarComponent {
  readonly items = input<NavItem[]>([]);
}
