import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  SalaryChangeRequest,
  SalaryHistory
} from '../models/salary-history';

@Injectable({
  providedIn: 'root'
})
export class SalaryService {

  private readonly apiUrl = 'http://localhost:8080/api/employees';

  constructor(private http: HttpClient) {}

  changeSalary(
    employeeId: number,
    request: SalaryChangeRequest
  ): Observable<SalaryHistory> {
    return this.http.post<SalaryHistory>(
      `${this.apiUrl}/${employeeId}/salary`,
      request
    );
  }

  getSalaryHistory(
    employeeId: number
  ): Observable<SalaryHistory[]> {
    return this.http.get<SalaryHistory[]>(
      `${this.apiUrl}/${employeeId}/salary-history`
    );
  }
}