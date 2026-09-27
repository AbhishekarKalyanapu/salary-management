import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Country {
  code: string;
  name: string;
  currencyCode: string;
}

export interface Currency {
  code: string;
  rateToUsd: number;
  asOf: string;
}

export interface Department {
  id: number;
  name: string;
}

export interface JobTitle {
  id: number;
  title: string;
  departmentId: number;
}

@Injectable({
  providedIn: 'root'
})
export class ReferenceService {

  private readonly apiUrl = 'http://localhost:8080/api/reference';

  constructor(private http: HttpClient) {}

  getCountries(): Observable<Country[]> {
    return this.http.get<Country[]>(`${this.apiUrl}/countries`);
  }

  getCurrencies(): Observable<Currency[]> {
    return this.http.get<Currency[]>(`${this.apiUrl}/currencies`);
  }

  getDepartments(): Observable<Department[]> {
    return this.http.get<Department[]>(`${this.apiUrl}/departments`);
  }

  getJobTitles(): Observable<JobTitle[]> {
    return this.http.get<JobTitle[]>(`${this.apiUrl}/job-titles`);
  }
}