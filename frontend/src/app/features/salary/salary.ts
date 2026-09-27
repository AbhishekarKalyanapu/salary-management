import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Employee } from '../../core/models/employee';
import { EmployeeService } from '../../core/services/employee';
import {
  SalaryChangeRequest,
  SalaryHistory
} from '../../core/models/salary-history';
import { SalaryService } from '../../core/services/salary';

@Component({
  selector: 'app-salary',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './salary.html',
  styleUrl: './salary.scss',
})
export class Salary implements OnInit {

  employees: Employee[] = [];
  selectedEmployee: Employee | null = null;
  salaryHistory: SalaryHistory[] = [];

  employeeId: number | null = null;
  newSalary: number | null = null;
  effectiveDate = new Date().toISOString().split('T')[0];
  reason = '';

  loadingEmployees = true;
  loadingHistory = false;
  saving = false;

  error = '';
  success = '';

  constructor(
    private employeeService: EmployeeService,
    private salaryService: SalaryService
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
  }

  loadEmployees(): void {
    this.loadingEmployees = true;
    this.error = '';

    this.employeeService.getEmployees(0, 10000).subscribe({
      next: response => {
        this.employees = response.content ?? [];
        this.loadingEmployees = false;
      },
      error: error => {
        console.error('Failed to load employees:', error);
        this.error = 'Unable to load employees.';
        this.loadingEmployees = false;
      }
    });
  }

  onEmployeeChange(): void {
    this.success = '';
    this.error = '';
    this.salaryHistory = [];

    if (this.employeeId === null) {
      this.selectedEmployee = null;
      return;
    }

    this.selectedEmployee =
      this.employees.find(
        employee => employee.id === Number(this.employeeId)
      ) ?? null;

    if (this.selectedEmployee) {
      this.newSalary = this.selectedEmployee.salary;
      this.loadSalaryHistory(this.selectedEmployee.id);
    }
  }

  loadSalaryHistory(employeeId: number): void {
    this.loadingHistory = true;

    this.salaryService.getSalaryHistory(employeeId).subscribe({
      next: history => {
        this.salaryHistory = history;
        this.loadingHistory = false;
      },
      error: error => {
        console.error('Failed to load salary history:', error);
        this.error = 'Unable to load salary history.';
        this.loadingHistory = false;
      }
    });
  }

  submitSalaryChange(): void {
    this.error = '';
    this.success = '';

    if (!this.selectedEmployee) {
      this.error = 'Please select an employee.';
      return;
    }

    if (this.newSalary === null || this.newSalary <= 0) {
      this.error = 'Please enter a valid salary.';
      return;
    }

    if (!this.effectiveDate) {
      this.error = 'Please select an effective date.';
      return;
    }

    if (!this.reason.trim()) {
      this.error = 'Please enter a reason for the salary change.';
      return;
    }

    const request: SalaryChangeRequest = {
      newSalary: Number(this.newSalary),
      effectiveDate: this.effectiveDate,
      reason: this.reason.trim()
    };

    this.saving = true;

    this.salaryService
      .changeSalary(this.selectedEmployee.id, request)
      .subscribe({
        next: history => {
          this.success = 'Salary updated successfully.';
          this.saving = false;

          this.selectedEmployee = {
            ...this.selectedEmployee!,
            salary: history.newSalary
          };

          const index = this.employees.findIndex(
            employee => employee.id === this.selectedEmployee!.id
          );

          if (index !== -1) {
            this.employees[index] = this.selectedEmployee;
          }

          this.newSalary = history.newSalary;
          this.reason = '';

          this.loadSalaryHistory(this.selectedEmployee.id);
        },
        error: error => {
          console.error('Failed to update salary:', error);

          this.error =
            error?.error?.message ||
            'Unable to update salary. Please check the values and try again.';

          this.saving = false;
        }
      });
  }

  formatDate(value: string): string {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    }).format(new Date(value));
  }

  formatDateTime(value: string): string {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(new Date(value));
  }
}

