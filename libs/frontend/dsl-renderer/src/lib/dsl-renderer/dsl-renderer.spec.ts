import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DslRendererComponent } from './dsl-renderer';

describe('DslRendererComponent', () => {
  let component: DslRendererComponent;
  let fixture: ComponentFixture<DslRendererComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DslRendererComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(DslRendererComponent);
    component = fixture.componentInstance;
    // Provide a required input spec
    fixture.componentRef.setInput('spec', { type: 'info_banner', title: 'Test', body: 'Hello', style: 'info' });
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
