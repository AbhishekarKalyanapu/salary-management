import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  Summary,
  DistributionBucket,
  DimensionStats,
  Outlier
} from '../../core/models/insights';
import { InsightsService } from '../../core/services/insights';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class Dashboard implements OnInit {

  summary: Summary | null = null;

  loading = false;
  errorMessage = '';
  distribution: DistributionBucket[] = [];
  departmentStats: DimensionStats[] = [];
  outliers: Outlier[] = [];
  constructor(private insightsService: InsightsService) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
  this.loading = true;
  this.errorMessage = '';

  this.insightsService.getSummary().subscribe({
    next: (response) => {
      this.summary = response;

      this.insightsService.getDistribution().subscribe({
  next: (distributionResponse) => {
    this.distribution = distributionResponse;

   this.insightsService.getByDimension('department').subscribe({
  next: (departmentResponse) => {
    this.departmentStats = departmentResponse;

    this.insightsService.getOutliers().subscribe({
      next: (outlierResponse) => {
        this.outliers = outlierResponse;
        this.loading = false;
      },
      error: (error) => {
        console.error('Failed to load salary outliers:', error);
        this.loading = false;
      }
    });
  },
  error: (error) => {
    console.error('Failed to load department statistics:', error);
    this.loading = false;
  }
});
  },
  error: (error) => {
    console.error('Failed to load salary distribution:', error);
    this.loading = false;
  }
});
    },
    error: (error) => {
      console.error('Failed to load dashboard summary:', error);
      this.errorMessage = 'Unable to load dashboard data.';
      this.loading = false;
    }
  });
  }
}