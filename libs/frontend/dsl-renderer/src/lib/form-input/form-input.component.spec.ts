/**
 * FormInputComponent — unit tests (T7.17)
 *
 * Covers: dynamic form building from spec, required validation,
 * submit event emission, and cancel behaviour.
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ComponentRef } from '@angular/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { FormInputComponent, FormSubmitEvent } from './form-input.component';
import type { FormInputSpec } from '@synaptiq/constants';

function makeSpec(overrides: Partial<FormInputSpec> = {}): FormInputSpec {
  return {
    type: 'form_input',
    title: 'Test Form',
    fields: [
      { field: 'name', label: 'Name', type: 'text', required: true },
      { field: 'email', label: 'Email', type: 'text', required: false },
    ],
    submit_action: 'test_action',
    submit_label: 'Submit',
    cancellable: true,
    ...overrides,
  };
}

describe('FormInputComponent', () => {
  let fixture: ComponentFixture<FormInputComponent>;
  let component: FormInputComponent;
  let componentRef: ComponentRef<FormInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormInputComponent, NoopAnimationsModule],
    }).compileComponents();

    fixture = TestBed.createComponent(FormInputComponent);
    component = fixture.componentInstance;
    componentRef = fixture.componentRef;
  });

  it('should create the component', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should build a reactive form with controls for each field', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();

    expect(component.form).toBeTruthy();
    expect(component.form.contains('name')).toBe(true);
    expect(component.form.contains('email')).toBe(true);
  });

  it('should mark required fields as invalid when empty', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();

    const nameCtrl = component.form.get('name');
    expect(nameCtrl).toBeTruthy();
    expect(nameCtrl!.valid).toBe(false);
    expect(nameCtrl!.errors).toEqual(expect.objectContaining({ required: true }));
  });

  it('should mark non-required fields as valid when empty', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();

    const emailCtrl = component.form.get('email');
    expect(emailCtrl).toBeTruthy();
    expect(emailCtrl!.valid).toBe(true);
  });

  it('should become valid when all required fields are filled', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();

    component.form.get('name')!.setValue('Alice');
    expect(component.form.valid).toBe(true);
  });

  it('should emit formSubmitted with action and values on submit', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();

    let emitted: FormSubmitEvent | null = null;
    component.formSubmitted.subscribe((e: FormSubmitEvent) => (emitted = e));

    component.form.get('name')!.setValue('Alice');
    component.form.get('email')!.setValue('alice@example.com');
    component.onSubmit();

    expect(emitted).not.toBeNull();
    expect(emitted!.action).toBe('test_action');
    expect(emitted!.values).toEqual(
      expect.objectContaining({ name: 'Alice', email: 'alice@example.com' }),
    );
  });

  it('should NOT emit formSubmitted if form is invalid', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();

    let emitted: FormSubmitEvent | null = null;
    component.formSubmitted.subscribe((e: FormSubmitEvent) => (emitted = e));

    // Don't fill required 'name'
    component.onSubmit();
    expect(emitted).toBeNull();
  });

  it('should emit formCancelled on cancel', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();

    let cancelled = false;
    component.formCancelled.subscribe(() => (cancelled = true));
    component.onCancel();
    expect(cancelled).toBe(true);
  });

  it('should handle spec with no fields gracefully', () => {
    componentRef.setInput('spec', makeSpec({ fields: [] }));
    fixture.detectChanges();

    expect(component.form).toBeTruthy();
    expect(Object.keys(component.form.controls)).toHaveLength(0);
  });

  it('should set default value from spec', () => {
    const spec = makeSpec({
      fields: [
        { field: 'active', label: 'Active', type: 'toggle', default_value: true },
      ],
    });
    componentRef.setInput('spec', spec);
    fixture.detectChanges();

    expect(component.form.get('active')!.value).toBe(true);
  });

  it('should return correct error messages for required fields', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();

    component.form.get('name')!.markAsTouched();
    const errorMsg = component.getErrorMessage({ field: 'name', label: 'Name', type: 'text', required: true });
    expect(errorMsg).toBe('Name is required');
  });

  it('should mark submitted state after successful submit', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();

    component.form.get('name')!.setValue('Test');
    component.onSubmit();
    expect(component.submitted()).toBe(true);
  });

  it('should NOT mark submitted state on invalid submit', () => {
    componentRef.setInput('spec', makeSpec());
    fixture.detectChanges();

    component.onSubmit();
    expect(component.submitted()).toBe(false);
  });
});
