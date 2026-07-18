import { Component, input, output } from '@angular/core';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { NotificationPanelComponent } from '../notification-panel/notification-panel.component';
import { ProfileMenuComponent } from '../profile-menu/profile-menu.component';

@Component({
  selector: 'govos-admin-toolbar',
  standalone: true,
  imports: [MatToolbarModule, MatIconModule, MatButtonModule, NotificationPanelComponent, ProfileMenuComponent],
  template: `
    <mat-toolbar class="toolbar" color="primary">
      <button mat-icon-button type="button" aria-label="Toggle navigation" (click)="menuToggle.emit()">
        <mat-icon>menu</mat-icon>
      </button>
      <span class="title">{{ title() }}</span>
      <span class="spacer"></span>
      <govos-notification-panel />
      <govos-profile-menu [username]="username()" (logout)="logout.emit()" />
    </mat-toolbar>
  `,
  styles: `
    .toolbar {
      position: sticky;
      top: 0;
      z-index: 2;
      min-height: var(--govos-toolbar-height);
    }

    .title {
      font-weight: 600;
      margin-left: 0.5rem;
    }

    .spacer {
      flex: 1;
    }
  `,
})
export class AdminToolbarComponent {
  readonly title = input('GovOS Administration');
  readonly username = input('Administrator');
  readonly menuToggle = output<void>();
  readonly logout = output<void>();
}
