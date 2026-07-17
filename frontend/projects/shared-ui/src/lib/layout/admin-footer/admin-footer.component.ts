import { Component } from '@angular/core';

@Component({
  selector: 'govos-admin-footer',
  standalone: true,
  template: `
    <footer class="footer">
      <span>GovOS Enterprise Government Platform</span>
      <span>© {{ year }} Government Operating System</span>
    </footer>
  `,
  styles: `
    .footer {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
      flex-wrap: wrap;
      padding: 1rem 1.5rem 1.5rem;
      color: var(--govos-muted);
      font-size: 0.8125rem;
    }
  `,
})
export class AdminFooterComponent {
  readonly year = new Date().getFullYear();
}
