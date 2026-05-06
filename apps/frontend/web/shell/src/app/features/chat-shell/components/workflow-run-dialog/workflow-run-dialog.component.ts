import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { WorkflowInput } from '@synaptiq/chat';

export interface WorkflowRunDialogData {
  inputs: WorkflowInput[];
}

@Component({
  selector: 'synaptiq-workflow-run-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './workflow-run-dialog.component.html',
  styleUrls: ['./workflow-run-dialog.component.scss']
})
export class WorkflowRunDialogComponent {
  form: FormGroup;
  fileNames: Record<string, string> = {};

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<WorkflowRunDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: WorkflowRunDialogData
  ) {
    const group: Record<string, any> = {};
    for (const input of data.inputs || []) {
      const validators = input.required !== false ? [Validators.required] : [];
      // If type is number, we might want to add a pattern or custom validation,
      // but Angular handles type="number" binding. Let's just use the default.
      group[input.name] = [input.defaultValue || '', validators];
    }
    this.form = this.fb.group(group);
  }

  async onFileSelected(event: Event, controlName: string): Promise<void> {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      // 10MB file size limit
      const MAX_FILE_SIZE = 10 * 1024 * 1024;
      if (file.size > MAX_FILE_SIZE) {
        console.error('File exceeds 10MB size limit');
        this.form.get(controlName)?.setErrors({ fileSizeError: true });
        this.form.get(controlName)?.markAsTouched();
        return;
      }

      this.fileNames[controlName] = file.name;
      try {
        const base64 = await this.fileToBase64(file);
        this.form.get(controlName)?.setValue(base64);
        this.form.get(controlName)?.setErrors(null);
        this.form.get(controlName)?.markAsTouched();
        this.form.get(controlName)?.updateValueAndValidity();
      } catch (err) {
        console.error('Failed to read file', err);
        this.form.get(controlName)?.setErrors({ fileReadError: true });
      }
    } else {
      delete this.fileNames[controlName];
      this.form.get(controlName)?.setValue(null);
    }
  }

  private fileToBase64(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result as string);
      reader.onerror = error => reject(error);
    });
  }

  onSubmit(): void {
    if (this.form.valid) {
      // For number inputs, ensure the value is parsed as a float so backend gets actual numbers
      const value = { ...this.form.value };
      for (const input of this.data.inputs || []) {
        if (input.type === 'number' && value[input.name] !== null && value[input.name] !== '') {
          value[input.name] = parseFloat(value[input.name]);
        }
      }
      this.dialogRef.close(value);
    }
  }
}
