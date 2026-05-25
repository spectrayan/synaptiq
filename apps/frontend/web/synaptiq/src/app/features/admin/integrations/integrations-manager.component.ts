import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { IntegrationFacade } from './integration.facade';

/**
 * Integrations Manager — admin component for managing tenant integrations.
 *
 * This component contains ZERO business logic. All state and operations
 * are delegated to {@link IntegrationFacade}. The component is responsible
 * only for:
 *  - Rendering the template using facade signals
 *  - Forwarding user events to facade methods
 *  - Holding local form binding fields (name, description, credentialRef)
 */
@Component({
  selector: 'sq-integrations-manager',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './integrations-manager.component.html',
  styleUrl: './integrations-manager.component.scss',
})
export class IntegrationsManagerComponent implements OnInit {
  readonly facade = inject(IntegrationFacade);

  // ── Local form bindings (view-only state, no logic) ────
  formName = '';
  formDescription = '';
  formCredentialRef = '';

  ngOnInit(): void {
    this.facade.loadIntegrations();
    this.facade.loadTemplates();
  }

  /** Collects form fields and delegates to facade. */
  onCreateClick(): void {
    this.facade.createIntegration(
      this.formName,
      this.formDescription,
      this.formCredentialRef,
    );
    this.resetForm();
  }

  private resetForm(): void {
    this.formName = '';
    this.formDescription = '';
    this.formCredentialRef = '';
  }
}
