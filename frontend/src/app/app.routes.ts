import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./features/dashboard/dashboard').then(
        m => m.Dashboard
      )
  },
  {
    path: 'employees',
    loadComponent: () =>
      import('./features/employees/employees').then(
        m => m.Employees
      )
  },
  {
    path: 'salary',
    loadComponent: () =>
      import('./features/salary/salary').then(
        m => m.Salary
      )
  },
  {
    path: 'insights',
    loadComponent: () =>
      import('./features/insights/insights').then(
        m => m.Insights
      )
  },
  {
    path: 'csv',
    loadComponent: () =>
      import('./features/csv/csv').then(
        m => m.Csv
      )
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];