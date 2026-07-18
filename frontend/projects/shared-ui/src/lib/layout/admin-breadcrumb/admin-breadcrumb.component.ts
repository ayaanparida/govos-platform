import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'govos-admin-breadcrumb',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <nav class="breadcrumb" aria-label="Breadcrumb">
      <mat-icon>home</mat-icon>
      @for (segment of segments(); track segment; let last = $last) {
        <span class="separator">/</span>
        <span [class.active]="last">{{ segment }}</span>
      }
    </nav>
  `,
  styles: `
    .breadcrumb {
      display: flex;
      align-items: center;
      gap: 0.35rem;
      padding: 0.75rem 1.5rem 0;
      color: var(--govos-muted);
      font-size: 0.875rem;
    }

    .separator {
      opacity: 0.6;
    }

    .active {
      color: var(--govos-text);
      font-weight: 600;
    }
  `,
})
export class AdminBreadcrumbComponent {
  readonly segments = input<string[]>(['Dashboard']);
}
