export interface SalaryChangeRequest {
  newSalary: number;
  effectiveDate: string;
  reason: string;
}

export interface SalaryHistory {
  id: number;
  oldSalary: number | null;
  newSalary: number;
  currencyCode: string;
  effectiveDate: string;
  reason: string;
  createdAt: string;
  percentChange: number | null;
}