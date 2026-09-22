import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';

import { Employee } from '../../core/models/employee';
import { EmployeeService } from '../../core/services/employee';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './employees.html',
  styleUrl: './employees.scss'
})
export class Employees implements OnInit {

  employees: Employee[] = [];

  loading = false;
  errorMessage = '';

  constructor(private employeeService: EmployeeService) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.loading = true;
    this.errorMessage = '';

    this.employeeService.getEmployees().subscribe({
      next: (response) => {
        this.employees = response.content;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load employees:', error);
        this.errorMessage = 'Unable to load employees.';
        this.loading = false;
      }
    });
  }
}