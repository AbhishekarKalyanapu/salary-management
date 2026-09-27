import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';

import { Salary } from './salary';

describe('Salary', () => {
  let component: Salary;
  let fixture: ComponentFixture<Salary>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Salary],
      providers: [
        provideHttpClient()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(Salary);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});