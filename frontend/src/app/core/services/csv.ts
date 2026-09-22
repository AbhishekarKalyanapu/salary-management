import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ImportRowError {
  rowNumber: number;
  errors: string[];
}

export interface ImportResult {
  totalRows: number;
  successCount: number;
  errorCount: number;
  createdEmployeeIds: number[];
  errors: ImportRowError[];
}

@Injectable({
  providedIn: 'root'
})
export class CsvService {

  private readonly apiUrl = 'http://localhost:8080/api/employees';

  constructor(private http: HttpClient) {}

  exportEmployees(
    q?: string,
    country?: string,
    department?: number,
    jobTitle?: number,
    level?: number
  ): Observable<Blob> {

    let params = new HttpParams();

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

    return this.http.get(
      `${this.apiUrl}/export`,
      {
        params,
        responseType: 'blob'
      }
    );
  }

  importEmployees(file: File): Observable<ImportResult> {

    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<ImportResult>(
      `${this.apiUrl}/import`,
      formData
    );
  }
}