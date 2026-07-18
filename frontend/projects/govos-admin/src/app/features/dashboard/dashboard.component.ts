import { Component, inject, OnInit, signal } from '@angular/core';
import { PlatformApiService } from 'shared-api';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <section>
      <h1 class="govos-page-title">Platform Dashboard</h1>

      @if (loading()) {
        <p>Loading platform metadata...</p>
      } @else if (errorMessage()) {
        <p class="error">{{ errorMessage() }}</p>
      } @else {
        <div class="govos-card-grid">
          <article class="govos-card">
            <h3>Application</h3>
            <p>{{ info()?.applicationName }} · {{ info()?.version }}</p>
            <p>{{ info()?.environment }} · Java {{ info()?.javaVersion }}</p>
          </article>

          <article class="govos-card">
            <h3>Platform Version</h3>
            <p>{{ version()?.release }} ({{ version()?.version }})</p>
            <p>Released {{ version()?.releaseDate }}</p>
          </article>

          <article class="govos-card">
            <h3>Platform Health</h3>
            <p>Database: <span [class]="statusClass(health()?.database)">{{ health()?.database }}</span></p>
            <p>Disk: <span [class]="statusClass(health()?.disk)">{{ health()?.disk }}</span></p>
            <p>Memory: <span [class]="statusClass(health()?.memory)">{{ health()?.memory }}</span></p>
            <p>Uptime: {{ health()?.uptime }}</p>
          </article>

          <article class="govos-card">
            <h3>Build</h3>
            <p>{{ build()?.artifact }} · {{ build()?.buildNumber }}</p>
            <p>{{ build()?.gitBranch }} @ {{ build()?.gitCommit }}</p>
          </article>
        </div>

        <section class="modules">
          <h2>Modules</h2>
          <ul>
            @for (module of modules(); track module.moduleName) {
              <li>{{ module.moduleName }} · {{ module.version }} · {{ module.status }}</li>
            }
          </ul>
        </section>
      }
    </section>
  `,
  styles: `
    .modules {
      margin-top: 1.5rem;
      background: var(--govos-surface);
      border: 1px solid var(--govos-border);
      border-radius: 12px;
      padding: 1rem 1.25rem;
    }

    .modules ul {
      margin: 0;
      padding-left: 1.25rem;
    }

    .error {
      color: #b91c1c;
    }
  `,
})
export class DashboardComponent implements OnInit {
  private readonly platformApi = inject(PlatformApiService);

  readonly loading = signal(true);
  readonly errorMessage = signal<string | null>(null);
  readonly info = signal<any>(null);
  readonly version = signal<any>(null);
  readonly health = signal<any>(null);
  readonly build = signal<any>(null);
  readonly modules = signal<any[]>([]);

  async ngOnInit(): Promise<void> {
    try {
      const [info, version, health, build, modules] = await Promise.all([
        firstValueFrom(this.platformApi.getPlatformInfo()),
        firstValueFrom(this.platformApi.getPlatformVersion()),
        firstValueFrom(this.platformApi.getPlatformHealth()),
        firstValueFrom(this.platformApi.getPlatformBuild()),
        firstValueFrom(this.platformApi.getPlatformModules()),
      ]);

      this.info.set(info.data);
      this.version.set(version.data);
      this.health.set(health.data);
      this.build.set(build.data);
      this.modules.set(modules.data ?? []);
    } catch {
      this.errorMessage.set('Unable to load platform administration data.');
    } finally {
      this.loading.set(false);
    }
  }

  statusClass(status?: string): string {
    if (status === 'UP') {
      return 'govos-status-up';
    }
    if (status === 'NOT_CONFIGURED') {
      return 'govos-status-unknown';
    }
    return 'govos-status-down';
  }
}
