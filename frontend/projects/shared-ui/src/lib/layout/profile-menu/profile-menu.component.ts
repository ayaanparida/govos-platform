import { Component, input, output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'govos-profile-menu',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatMenuModule],
  template: `
    <button mat-button type="button" [matMenuTriggerFor]="profileMenu" class="profile-trigger">
      <mat-icon>account_circle</mat-icon>
      <span>{{ username() }}</span>
    </button>
    <mat-menu #profileMenu="matMenu">
      <button mat-menu-item disabled>Profile</button>
      <button mat-menu-item disabled>Settings</button>
      <button mat-menu-item (click)="logout.emit()">
        <mat-icon>logout</mat-icon>
        <span>Logout</span>
      </button>
    </mat-menu>
  `,
  styles: `
    .profile-trigger {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
    }
  `,
})
export class ProfileMenuComponent {
  readonly username = input('Administrator');
  readonly logout = output<void>();
}
