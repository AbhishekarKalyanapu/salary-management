import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { Insights } from './insights';

describe('Insights', () => {
  let component: Insights;
  let fixture: ComponentFixture<Insights>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Insights],
      providers: [
        provideHttpClient()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Insights);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});