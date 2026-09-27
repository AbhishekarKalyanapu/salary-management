import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  CsvService,
  ImportResult
} from '../../core/services/csv';

@Component({
  selector: 'app-csv',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './csv.html',
  styleUrl: './csv.scss',
})
export class Csv {

  selectedFile: File | null = null;
  importResult: ImportResult | null = null;

  search = '';
  country = '';

  exporting = false;
  importing = false;

  error = '';
  success = '';

  constructor(private csvService: CsvService) {}

  onFileSelected(event: Event): void {
    this.error = '';
    this.success = '';
    this.importResult = null;

    const input = event.target as HTMLInputElement;

    if (!input.files || input.files.length === 0) {
      this.selectedFile = null;
      return;
    }

    const file = input.files[0];

    if (!file.name.toLowerCase().endsWith('.csv')) {
      this.error = 'Please select a CSV file.';
      this.selectedFile = null;
      input.value = '';
      return;
    }

    this.selectedFile = file;
  }

  exportEmployees(): void {
    this.error = '';
    this.success = '';
    this.exporting = true;

    const search = this.search.trim() || undefined;
    const country = this.country.trim() || undefined;

    this.csvService
      .exportEmployees(search, country)
      .subscribe({
        next: blob => {
          const url = window.URL.createObjectURL(blob);
          const link = document.createElement('a');

          link.href = url;
          link.download = `employees-${new Date()
            .toISOString()
            .slice(0, 10)}.csv`;

          link.click();

          window.URL.revokeObjectURL(url);

          this.success = 'Employee CSV exported successfully.';
          this.exporting = false;
        },
        error: error => {
          console.error('CSV export failed:', error);
          this.error = 'Unable to export employee data.';
          this.exporting = false;
        }
      });
  }

  importEmployees(): void {
    this.error = '';
    this.success = '';
    this.importResult = null;

    if (!this.selectedFile) {
      this.error = 'Please select a CSV file first.';
      return;
    }

    this.importing = true;

    this.csvService.importEmployees(this.selectedFile).subscribe({
      next: result => {
        this.importResult = result;
        this.importing = false;

        if (result.errorCount === 0) {
          this.success =
            `${result.successCount} employee record(s) imported successfully.`;
        } else {
          this.success =
            `${result.successCount} employee record(s) imported successfully. ` +
            `${result.errorCount} row(s) contain errors.`;
        }
      },
      error: error => {
        console.error('CSV import failed:', error);

        this.error =
          error?.error?.message ||
          'Unable to import the CSV file. Please check the file format.';

        this.importing = false;
      }
    });
  }

  clearImport(): void {
    this.selectedFile = null;
    this.importResult = null;
    this.error = '';
    this.success = '';
  }
}
