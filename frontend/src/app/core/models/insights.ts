export interface Summary {
  headcount: number;
  totalPayrollUsd: number;
  medianSalaryUsd: number;
}

export interface DimensionStats {
  key: string;
  label: string;
  currencyCode: string;
  count: number;
  min: number;
  median: number;
  mean: number;
  p90: number;
  max: number;
}

export interface DistributionBucket {
  rangeStart: number;
  rangeEnd: number;
  count: number;
}

export interface Outlier {
  employeeId: number;
  firstName: string;
  lastName: string;
  countryCode: string;
  jobTitleTitle: string;
  currencyCode: string;
  salary: number;
  groupMedian: number;
  deviationPct: number;
}