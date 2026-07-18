import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <section class="portal-shell">
      <h1>GovOS Officer Portal</h1>
      <p>Foundation workspace shell. Officer features are out of scope for this sprint.</p>
      <router-outlet />
    </section>
  `,
  styles: `
    .portal-shell {
      padding: 2rem;
      font-family: 'Segoe UI', Roboto, sans-serif;
    }
  `,
})
export class App {}
