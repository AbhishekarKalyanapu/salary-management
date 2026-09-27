import { Component, OnInit } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import {
  Employee,
  EmployeeCreateRequest
} from '../../core/models/employee';

import { EmployeeService } from '../../core/services/employee';

import {
  ReferenceService,
  Country,
  Currency,
  Department,
  JobTitle
} from '../../core/services/reference';

@Component({
  selector: 'app-employees',
  standalone: true,
  imports: [CommonModule, DecimalPipe, FormsModule],
  templateUrl: './employees.html',
  styleUrl: './employees.scss'
})
export class Employees implements OnInit {

  employees: Employee[] = [];

  loading = false;
  errorMessage = '';

  search = '';
  country = '';

  currentPage = 0;
  pageSize = 25;
  totalElements = 0;
  totalPages = 0;

  countries: Country[] = [];
  currencies: Currency[] = [];
  departments: Department[] = [];
  jobTitles: JobTitle[] = [];

 showAddForm = false;
editingEmployee: Employee | null = null;
saving = false;
formError = '';

  newEmployee = {
    firstName: '',
    lastName: '',
    email: '',
    countryCode: '',
    currencyCode: '',
    departmentId: undefined as number | undefined,
    jobTitleId: undefined as number | undefined,
    level: 1,
    hireDate: '',
    salary: 0
  };

  constructor(
    private employeeService: EmployeeService,
    private referenceService: ReferenceService
  ) {}

  ngOnInit(): void {
    this.loadEmployees();
    this.loadReferenceData();
  }

  loadReferenceData(): void {
    this.referenceService.getCountries().subscribe({
      next: data => this.countries = data,
      error: error => console.error('Failed to load countries:', error)
    });

    this.referenceService.getCurrencies().subscribe({
      next: data => this.currencies = data,
      error: error => console.error('Failed to load currencies:', error)
    });

    this.referenceService.getDepartments().subscribe({
      next: data => this.departments = data,
      error: error => console.error('Failed to load departments:', error)
    });

    this.referenceService.getJobTitles().subscribe({
      next: data => this.jobTitles = data,
      error: error => console.error('Failed to load job titles:', error)
    });
  }

  loadEmployees(): void {
    this.loading = true;
    this.errorMessage = '';

    this.employeeService.getEmployees(
      this.currentPage,
      this.pageSize,
      this.search.trim() || undefined,
      this.country.trim() || undefined
    ).subscribe({
      next: response => {
        this.employees = response.content;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.loading = false;
      },
      error: error => {
        console.error('Failed to load employees:', error);
        this.errorMessage = 'Unable to load employees.';
        this.loading = false;
      }
    });
  }

  searchEmployees(): void {
    this.currentPage = 0;
    this.loadEmployees();
  }

  clearFilters(): void {
    this.search = '';
    this.country = '';
    this.currentPage = 0;
    this.loadEmployees();
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadEmployees();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadEmployees();
    }
  }

  createEmployee(): void {
  this.saving = true;
  this.formError = '';

  if (
    !this.newEmployee.firstName.trim() ||
    !this.newEmployee.lastName.trim() ||
    !this.newEmployee.email.trim() ||
    !this.newEmployee.countryCode ||
    this.newEmployee.departmentId === undefined ||
    this.newEmployee.jobTitleId === undefined ||
    !this.newEmployee.hireDate ||
    !this.newEmployee.currencyCode ||
    this.newEmployee.salary <= 0
  ) {
    this.formError = 'Please fill in all required fields.';
    this.saving = false;
    return;
  }

  if (this.editingEmployee) {
    const request = {
      firstName: this.newEmployee.firstName.trim(),
      lastName: this.newEmployee.lastName.trim(),
      email: this.newEmployee.email.trim(),
      countryCode: this.newEmployee.countryCode,
      departmentId: this.newEmployee.departmentId,
      jobTitleId: this.newEmployee.jobTitleId,
      level: this.newEmployee.level,
      hireDate: this.newEmployee.hireDate
    };

    this.employeeService
      .updateEmployee(this.editingEmployee.id, request)
      .subscribe({
        next: () => {
          this.saving = false;
          this.showAddForm = false;
          this.editingEmployee = null;
          this.loadEmployees();
        },
        error: error => {
          console.error('Failed to update employee:', error);

          this.formError =
            error?.error?.detail ||
            error?.error?.message ||
            'Unable to update employee.';

          this.saving = false;
        }
      });

    return;
  }

  const request: EmployeeCreateRequest = {
    firstName: this.newEmployee.firstName.trim(),
    lastName: this.newEmployee.lastName.trim(),
    email: this.newEmployee.email.trim(),
    countryCode: this.newEmployee.countryCode,
    currencyCode: this.newEmployee.currencyCode,
    departmentId: this.newEmployee.departmentId,
    jobTitleId: this.newEmployee.jobTitleId,
    level: this.newEmployee.level,
    hireDate: this.newEmployee.hireDate,
    salary: this.newEmployee.salary
  };

  this.employeeService.createEmployee(request).subscribe({
    next: () => {
      this.saving = false;
      this.showAddForm = false;
      this.editingEmployee = null;

      this.newEmployee = {
        firstName: '',
        lastName: '',
        email: '',
        countryCode: '',
        currencyCode: '',
        departmentId: undefined,
        jobTitleId: undefined,
        level: 1,
        hireDate: '',
        salary: 0
      };

      this.currentPage = 0;
      this.loadEmployees();
    },

    error: error => {
      console.error('Failed to create employee:', error);

      this.formError =
        error?.error?.detail ||
        error?.error?.message ||
        'Unable to create employee.';

      this.saving = false;
    }
  });
}
  editEmployee(employee: Employee): void {
  this.editingEmployee = employee;

  this.newEmployee = {
    firstName: employee.firstName,
    lastName: employee.lastName,
    email: employee.email,
    countryCode: employee.countryCode,
    currencyCode: employee.currencyCode,
    departmentId: employee.departmentId,
    jobTitleId: employee.jobTitleId,
    level: employee.level,
    hireDate: employee.hireDate,
    salary: employee.salary
  };

  this.formError = '';
  this.showAddForm = true;
}

deleteEmployee(employee: Employee): void {
  const confirmed = window.confirm(
    `Are you sure you want to delete ${employee.firstName} ${employee.lastName}?`
  );

  if (!confirmed) {
    return;
  }

  this.employeeService.deleteEmployee(employee.id).subscribe({
    next: () => {
      this.loadEmployees();
    },
    error: error => {
      console.error('Failed to delete employee:', error);

      this.errorMessage =
        error?.error?.detail ||
        error?.error?.message ||
        'Unable to delete employee.';
    }
  });
}

  get showingFrom(): number {
    return this.totalElements === 0
      ? 0
      : this.currentPage * this.pageSize + 1;
  }

  get showingTo(): number {
    return Math.min(
      (this.currentPage + 1) * this.pageSize,
      this.totalElements
    );
  }
}