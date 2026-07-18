export interface NavItem {
  label: string;
  icon: string;
  route: string;
}

export const ADMIN_NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
];
