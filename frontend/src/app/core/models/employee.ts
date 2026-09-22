export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  countryCode: string;
  countryName: string;
  departmentId: number;
  departmentName: string;
  jobTitleId: number;
  jobTitleTitle: string;
  level: number;
  hireDate: string;
  salary: number;
  currencyCode: string;
  createdAt: string;
  updatedAt: string;
  rowVersion: string;
}

export interface EmployeeCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  countryCode: string;
  currencyCode: string;
  departmentId: number;
  jobTitleId: number;
  level: number;
  hireDate: string;
  salary: number;
}

export interface EmployeeUpdateRequest {
  firstName: string;
  lastName: string;
  email: string;
  countryCode: string;
  departmentId: number;
  jobTitleId: number;
  level: number;
  hireDate: string;
}