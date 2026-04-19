import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DslRenderer } from './dsl-renderer';

describe('DslRenderer', () => {
  let component: DslRenderer;
  let fixture: ComponentFixture<DslRenderer>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DslRenderer],
    }).compileComponents();

    fixture = TestBed.createComponent(DslRenderer);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
