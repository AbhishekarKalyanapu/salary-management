import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { SalaryService } from './salary';

describe('SalaryService', () => {
  let service: SalaryService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient()
      ]
    });

    service = TestBed.inject(SalaryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});