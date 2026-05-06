/**
 * ApplicationService — facade for the Application CRUD API.
 *
 * Delegates to the generated ApplicationsService from @synaptiq/client.
 * Re-exports SDK types for backward compatibility.
 */
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  ApplicationsService as ApplicationsApiService,
  type ApplicationResponse,
  type CreateApplicationRequest,
  type ListApplications200Response,
} from '@synaptiq/client';

// Re-export SDK types for existing consumers
export type {
  ApplicationResponse,
  CreateApplicationRequest,
  ListApplications200Response,
} from '@synaptiq/client';

/** Backward-compatible alias */
export type ApplicationListResponse = ListApplications200Response;

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private readonly api = inject(ApplicationsApiService);

  /**
   * List all applications for the current tenant.
   */
  async listApplications(): Promise<ApplicationResponse[]> {
    const result = await firstValueFrom(
      this.api.listApplications({}),
    );
    return result.applications ?? [];
  }

  /**
   * Create a new application.
   */
  async createApplication(request: CreateApplicationRequest): Promise<ApplicationResponse> {
    return firstValueFrom(
      this.api.createApplication({ createApplicationRequest: request }),
    );
  }

  /**
   * Get application by ID.
   */
  async getApplication(appId: string): Promise<ApplicationResponse> {
    return firstValueFrom(
      this.api.getApplication({ appId }),
    );
  }

  /**
   * Delete an application.
   */
  async deleteApplication(appId: string): Promise<void> {
    await firstValueFrom(
      this.api.deleteApplication({ appId }),
    );
  }
}
