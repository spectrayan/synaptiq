import { Injectable, inject, signal } from '@angular/core';
import { catchError, EMPTY } from 'rxjs';
import {
  IntegrationsService,
  IntegrationResponse,
  TemplateDescriptorResponse,
  CreateIntegrationRequest,
  ConnectionTestResponse,
} from '@synaptiq/client';

export type ViewMode = 'list' | 'templates' | 'create';

/** Toast notification payload */
export interface Toast {
  message: string;
  isError: boolean;
}

/**
 * Integration Facade Service — centralizes all state and business logic
 * for the integration management feature.
 *
 * The component should only read signals and call facade methods;
 * no SDK calls or business logic belong in the component layer.
 */
@Injectable({ providedIn: 'root' })
export class IntegrationFacade {
  private readonly api = inject(IntegrationsService);

  // ── State (private writable) ──────────────────────────
  private readonly _integrations = signal<IntegrationResponse[]>([]);
  private readonly _templates = signal<TemplateDescriptorResponse[]>([]);
  private readonly _selectedTemplate = signal<TemplateDescriptorResponse | null>(null);
  private readonly _viewMode = signal<ViewMode>('list');
  private readonly _loading = signal(false);
  private readonly _actionLoading = signal(false);
  private readonly _toast = signal<Toast | null>(null);
  private readonly _formParams = signal<Record<string, string>>({});

  // ── Public selectors (readonly) ───────────────────────
  readonly integrations = this._integrations.asReadonly();
  readonly templates = this._templates.asReadonly();
  readonly selectedTemplate = this._selectedTemplate.asReadonly();
  readonly viewMode = this._viewMode.asReadonly();
  readonly loading = this._loading.asReadonly();
  readonly actionLoading = this._actionLoading.asReadonly();
  readonly toast = this._toast.asReadonly();
  readonly formParams = this._formParams.asReadonly();

  // ═══════════════════════════════════════════════════════
  //  Navigation
  // ═══════════════════════════════════════════════════════

  navigateTo(mode: ViewMode): void {
    this._viewMode.set(mode);
  }

  // ═══════════════════════════════════════════════════════
  //  Data Loading
  // ═══════════════════════════════════════════════════════

  loadIntegrations(): void {
    this._loading.set(true);
    this.api.listIntegrations({}).pipe(
      catchError(() => {
        this._loading.set(false);
        this.showToast('Failed to load integrations', true);
        return EMPTY;
      }),
    ).subscribe((resp) => {
      this._integrations.set(resp.integrations || []);
      this._loading.set(false);
    });
  }

  loadTemplates(): void {
    this.api.listIntegrationTemplates().pipe(
      catchError(() => EMPTY),
    ).subscribe((resp) => {
      this._templates.set(resp.templates || []);
    });
  }

  // ═══════════════════════════════════════════════════════
  //  Template Selection & Form
  // ═══════════════════════════════════════════════════════

  selectTemplate(template: TemplateDescriptorResponse): void {
    this._selectedTemplate.set(template);

    // Pre-fill default values from template parameter definitions
    const defaults: Record<string, string> = {};
    (template.parameters || []).forEach((p) => {
      if (p.defaultValue) defaults[p.name!] = p.defaultValue;
    });
    this._formParams.set(defaults);

    this._viewMode.set('create');
  }

  setFormParam(name: string, value: string): void {
    this._formParams.update((p) => ({ ...p, [name]: value }));
  }

  // ═══════════════════════════════════════════════════════
  //  CRUD Operations
  // ═══════════════════════════════════════════════════════

  createIntegration(name: string, description: string, credentialRef: string): void {
    const template = this._selectedTemplate();
    if (!template || !name) return;

    this._actionLoading.set(true);
    const req: CreateIntegrationRequest = {
      name,
      connectorType: template.connectorType as any,
      description: description || undefined,
      templateId: template.templateId,
      parameters: this._formParams(),
      credentialRef: credentialRef || undefined,
    };

    this.api.createIntegration({ createIntegrationRequest: req }).pipe(
      catchError(() => {
        this._actionLoading.set(false);
        this.showToast('Failed to create integration', true);
        return EMPTY;
      }),
    ).subscribe((result) => {
      this._actionLoading.set(false);
      this._integrations.update((list) => [...list, result]);
      this._viewMode.set('list');
      this.showToast(`Created "${result.name}"`);
    });
  }

  activate(routeConfigId: string): void {
    this._actionLoading.set(true);
    this.api.activateIntegration({ routeConfigId }).pipe(
      catchError(() => {
        this._actionLoading.set(false);
        this.showToast('Activation failed', true);
        return EMPTY;
      }),
    ).subscribe((updated) => {
      this._actionLoading.set(false);
      this.patchIntegration(updated);
      this.showToast(`Activated "${updated.name}"`);
    });
  }

  deactivate(routeConfigId: string): void {
    this._actionLoading.set(true);
    this.api.deactivateIntegration({ routeConfigId }).pipe(
      catchError(() => {
        this._actionLoading.set(false);
        this.showToast('Deactivation failed', true);
        return EMPTY;
      }),
    ).subscribe((updated) => {
      this._actionLoading.set(false);
      this.patchIntegration(updated);
      this.showToast(`Deactivated "${updated.name}"`);
    });
  }

  testConnection(routeConfigId: string): void {
    this._actionLoading.set(true);
    this.api.testIntegrationConnection({ routeConfigId }).pipe(
      catchError(() => {
        this._actionLoading.set(false);
        this.showToast('Connection test failed', true);
        return EMPTY;
      }),
    ).subscribe((result) => {
      this._actionLoading.set(false);
      this.showToast(
        result.success
          ? `Connection OK (${result.durationMs}ms)`
          : `Test failed: ${result.message}`,
        !result.success,
      );
    });
  }

  deleteIntegration(routeConfigId: string): void {
    this._actionLoading.set(true);
    this.api.deleteIntegration({ routeConfigId }).pipe(
      catchError(() => {
        this._actionLoading.set(false);
        this.showToast('Delete failed', true);
        return EMPTY;
      }),
    ).subscribe(() => {
      this._actionLoading.set(false);
      this._integrations.update((list) =>
        list.filter((i) => i.routeConfigId !== routeConfigId),
      );
      this.showToast('Integration deleted');
    });
  }

  // ═══════════════════════════════════════════════════════
  //  Utility
  // ═══════════════════════════════════════════════════════

  /** Map connector type to emoji icon. */
  getConnectorIcon(type?: string): string {
    const icons: Record<string, string> = {
      REST_API: '🌐',
      WEBHOOK: '🪝',
      DATABASE: '🗄️',
      SLACK: '💬',
      EMAIL: '📧',
      MCP_SERVER: '🤖',
      MESSAGE_QUEUE: '📨',
      FILE_STORAGE: '📁',
      CUSTOM_YAML: '⚙️',
    };
    return icons[type || ''] || '🔌';
  }

  // ── Private ─────────────────────────────────────────

  private patchIntegration(updated: IntegrationResponse): void {
    this._integrations.update((list) =>
      list.map((i) => (i.routeConfigId === updated.routeConfigId ? updated : i)),
    );
  }

  private showToast(message: string, isError = false): void {
    this._toast.set({ message, isError });
    setTimeout(() => this._toast.set(null), 4000);
  }
}
