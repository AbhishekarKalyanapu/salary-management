import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  Summary,
  DimensionStats,
  DistributionBucket,
  Outlier
} from '../models/insights';

@Injectable({
  providedIn: 'root'
})
export class InsightsService {

  private readonly apiUrl = 'http://localhost:8080/api/insights';

  constructor(private http: HttpClient) {}

  getSummary(): Observable<Summary> {
    return this.http.get<Summary>(
      `${this.apiUrl}/summary`
    );
  }

  getByDimension(
    dimension: string
  ): Observable<DimensionStats[]> {
    return this.http.get<DimensionStats[]>(
      `${this.apiUrl}/by/${dimension}`
    );
  }

  getDistribution(): Observable<DistributionBucket[]> {
    return this.http.get<DistributionBucket[]>(
      `${this.apiUrl}/distribution`
    );
  }

  getOutliers(
    thresholdPct: number = 20,
    minGroupSize: number = 5
  ): Observable<Outlier[]> {

    const params = new HttpParams()
      .set('thresholdPct', thresholdPct)
      .set('minGroupSize', minGroupSize);

    return this.http.get<Outlier[]>(
      `${this.apiUrl}/outliers`,
      { params }
    );
  }
}