import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'govos-notification-panel',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatMenuModule],
  template: `
    <button mat-icon-button type="button" [matMenuTriggerFor]="notificationMenu" aria-label="Notifications">
      <mat-icon>notifications</mat-icon>
    </button>
    <mat-menu #notificationMenu="matMenu">
      <button mat-menu-item disabled>No new notifications</button>
    </mat-menu>
  `,
})
export class NotificationPanelComponent {}
