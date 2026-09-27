import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { CsvService } from './csv';

describe('CsvService', () => {
  let service: CsvService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient()
      ]
    });

    service = TestBed.inject(CsvService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});