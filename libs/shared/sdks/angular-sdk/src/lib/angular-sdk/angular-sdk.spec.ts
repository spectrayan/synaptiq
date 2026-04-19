import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AngularSdk } from './angular-sdk';

describe('AngularSdk', () => {
  let component: AngularSdk;
  let fixture: ComponentFixture<AngularSdk>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AngularSdk],
    }).compileComponents();

    fixture = TestBed.createComponent(AngularSdk);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
