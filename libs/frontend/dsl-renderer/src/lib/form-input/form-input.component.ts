/**
 * FormInputComponent — inline chat form (T7.14 — REQ-FORM-1)
 *
 * Renders Material form fields inside the chat bubble for structured
 * data entry. Used for both:
 *   - End-user flows (e.g. "Add a product")
 *   - Admin schema config (e.g. "Add a warranty field")
 *
 * Angular signals API: input() / output() / computed()
 */
import { Component, computed, input, output, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormGroup, FormControl, Validators, ValidatorFn } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { FormInputSpec, FormFieldDef, VisibleWhenPredicate } from '@synaptiq/constants';
import { SuggestionBarComponent } from '../suggestion-bar/suggestion-bar.component';

/** Payload emitted when the form is submitted. */
export interface FormSubmitEvent {
  action: string;
  values: Record<string, unknown>;
}

@Component({
  selector: 'syn-form-input',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSlideToggleModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    SuggestionBarComponent,
  ],
  templateUrl: './form-input.component.html',
  styleUrl: './form-input.component.scss',
})
export class FormInputComponent implements OnInit {
  readonly spec = input.required<FormInputSpec>();
  readonly formSubmitted = output<FormSubmitEvent>();
  readonly formCancelled = output<void>();
  readonly suggestionClicked = output<string>();

  /** Sorted fields by position, falling back to array order. */
  readonly sortedFields = computed<FormFieldDef[]>(() => {
    const fields = [...this.spec().fields];
    return fields.sort((a, b) => (a.position ?? 999) - (b.position ?? 999));
  });

  /** Track submission state to disable button and show feedback. */
  readonly submitted = signal(false);

  /** Reactive form built dynamically from spec fields. */
  form!: FormGroup;

  ngOnInit() {
    this.buildForm();
    this.syncVisibility();
  }

  private buildForm() {
    const controls: Record<string, FormControl> = {};
    const initialValues = this.spec().initial_values ?? {};

    for (const field of this.spec().fields) {
      const defaultVal = initialValues[field.field] ?? field.default_value ?? this.getDefaultForType(field.type);
      const validators = this.buildValidators(field);
      controls[field.field] = new FormControl(defaultVal, validators);
    }

    this.form = new FormGroup(controls);
  }

  /**
   * Subscribe to value changes on trigger fields so we can
   * enable/disable dependent fields reactively.
   */
  private syncVisibility() {
    const triggerKeys = new Set<string>();
    for (const field of this.spec().fields) {
      if (field.visible_when) triggerKeys.add(field.visible_when.field);
    }

    for (const key of triggerKeys) {
      const ctrl = this.form.get(key);
      if (!ctrl) continue;
      ctrl.valueChanges.subscribe(() => this.updateDependentFields());
    }

    // Run once at init to set initial state.
    this.updateDependentFields();
  }

  /** Enable/disable fields based on their visible_when predicate. */
  private updateDependentFields() {
    for (const field of this.spec().fields) {
      if (!field.visible_when) continue;
      const ctrl = this.getControl(field.field);
      if (!ctrl) continue;

      if (this.evaluatePredicate(field.visible_when)) {
        ctrl.enable({ emitEvent: false });
      } else {
        ctrl.disable({ emitEvent: false });
      }
    }
  }

  /** Check whether a field should be visible based on its predicate. */
  isFieldVisible(field: FormFieldDef): boolean {
    if (!field.visible_when) return true;
    return this.evaluatePredicate(field.visible_when);
  }

  private evaluatePredicate(pred: VisibleWhenPredicate): boolean {
    const current = this.form?.get(pred.field)?.value;
    switch (pred.operator) {
      case 'eq':     return current === pred.value;
      case 'neq':    return current !== pred.value;
      case 'in':     return Array.isArray(pred.value) && pred.value.includes(current);
      case 'not_in': return Array.isArray(pred.value) && !pred.value.includes(current);
      case 'truthy': return !!current;
      case 'falsy':  return !current;
      default:       return true;
    }
  }

  private getDefaultForType(type: string): unknown {
    switch (type) {
      case 'toggle': return false;
      case 'multi_select': return [];
      case 'number':
      case 'currency': return null;
      default: return '';
    }
  }

  private buildValidators(field: FormFieldDef): ValidatorFn[] {
    const v: ValidatorFn[] = [];
    if (field.required) v.push(Validators.required);
    if (field.validation?.min != null) v.push(Validators.min(field.validation.min));
    if (field.validation?.max != null) v.push(Validators.max(field.validation.max));
    if (field.validation?.pattern) v.push(Validators.pattern(field.validation.pattern));
    return v;
  }

  getControl(fieldKey: string): FormControl {
    return this.form.get(fieldKey) as FormControl;
  }

  getErrorMessage(field: FormFieldDef): string {
    const control = this.getControl(field.field);
    if (!control || !control.errors) return '';

    if (control.hasError('required')) return `${field.label} is required`;
    if (control.hasError('min')) return `Minimum: ${field.validation?.min}`;
    if (control.hasError('max')) return `Maximum: ${field.validation?.max}`;
    if (control.hasError('pattern')) return field.validation?.message ?? 'Invalid format';
    return '';
  }

  onSubmit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitted.set(true);
    this.formSubmitted.emit({
      action: this.spec().submit_action,
      values: this.form.getRawValue(),
    });
  }

  onCancel() {
    this.formCancelled.emit();
  }

  onSuggestion(prompt: string) {
    this.suggestionClicked.emit(prompt);
  }
}
