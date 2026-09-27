import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';

import {
  Summary,
  DimensionStats,
  DistributionBucket,
  Outlier
} from '../../core/models/insights';
import { InsightsService } from '../../core/services/insights';

@Component({
  selector: 'app-insights',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './insights.html',
  styleUrl: './insights.scss'
})
export class Insights implements OnInit {

  summary: Summary | null = null;
  departmentStats: DimensionStats[] = [];
  countryStats: DimensionStats[] = [];
  distribution: DistributionBucket[] = [];
  outliers: Outlier[] = [];

  loading = true;
  error = '';

  constructor(private insightsService: InsightsService) {}

  ngOnInit(): void {
    this.loadInsights();
  }

  loadInsights(): void {
    this.loading = true;
    this.error = '';

    let completed = 0;
    const totalRequests = 5;

    const complete = (): void => {
      completed++;
      if (completed === totalRequests) {
        this.loading = false;
      }
    };

    this.insightsService.getSummary().subscribe({
      next: data => {
        this.summary = data;
        complete();
      },
      error: error => {
        console.error('Failed to load summary:', error);
        this.error = 'Unable to load salary summary.';
        complete();
      }
    });

    this.insightsService.getByDimension('department').subscribe({
      next: data => {
        this.departmentStats = data;
        complete();
      },
      error: error => {
        console.error('Failed to load department statistics:', error);
        complete();
      }
    });

    this.insightsService.getByDimension('country').subscribe({
      next: data => {
        this.countryStats = data;
        complete();
      },
      error: error => {
        console.error('Failed to load country statistics:', error);
        complete();
      }
    });

    this.insightsService.getDistribution().subscribe({
      next: data => {
        this.distribution = data;
        complete();
      },
      error: error => {
        console.error('Failed to load salary distribution:', error);
        complete();
      }
    });

    this.insightsService.getOutliers().subscribe({
      next: data => {
        this.outliers = data;
        complete();
      },
      error: error => {
        console.error('Failed to load salary outliers:', error);
        complete();
      }
    });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-US', {
      maximumFractionDigits: 0
    }).format(value);
  }

  getDistributionWidth(count: number): number {
    if (!this.distribution.length) {
      return 0;
    }

    const max = Math.max(
      ...this.distribution.map(bucket => bucket.count)
    );

    return max === 0 ? 0 : (count / max) * 100;
  }

  trackByKey(index: number, item: DimensionStats): string {
    return item.key;
  }

  trackByRange(index: number, item: DistributionBucket): number {
    return item.rangeStart;
  }

  trackByEmployee(index: number, item: Outlier): number {
    return item.employeeId;
  }
}

