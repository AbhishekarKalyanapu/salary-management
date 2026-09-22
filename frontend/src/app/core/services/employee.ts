import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  Employee,
  EmployeeCreateRequest,
  EmployeeUpdateRequest
} from '../models/employee';

import { PageResponse } from '../models/page-response';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

  private readonly apiUrl = 'http://localhost:8080/api/employees';

  constructor(private http: HttpClient) {}

  getEmployees(
    page: number = 0,
    size: number = 25,
    q?: string,
    country?: string,
    department?: number,
    jobTitle?: number,
    level?: number
  ): Observable<PageResponse<Employee>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (q) {
      params = params.set('q', q);
    }

    if (country) {
      params = params.set('country', country);
    }

    if (department !== undefined) {
      params = params.set('department', department);
    }

    if (jobTitle !== undefined) {
      params = params.set('jobTitle', jobTitle);
    }

    if (level !== undefined) {
      params = params.set('level', level);
    }

    return this.http.get<PageResponse<Employee>>(this.apiUrl, { params });
  }

  getEmployee(id: number): Observable<Employee> {
    return this.http.get<Employee>(`${this.apiUrl}/${id}`);
  }

  createEmployee(request: EmployeeCreateRequest): Observable<Employee> {
    return this.http.post<Employee>(this.apiUrl, request);
  }

  updateEmployee(
    id: number,
    request: EmployeeUpdateRequest
  ): Observable<Employee> {
    return this.http.put<Employee>(
      `${this.apiUrl}/${id}`,
      request
    );
  }

  deleteEmployee(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}