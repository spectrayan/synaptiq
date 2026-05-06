/**
 * DataSourceService — facade for the DataSource CRUD API.
 *
 * Delegates to the generated DataSourcesService from @synaptiq/client.
 * Re-exports SDK types for backward compatibility.
 */
import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  DataSourcesService as DataSourcesApiService,
  type DataSourceResponse,
  type CreateDataSourceRequest,
  type ListDataSources200Response,
} from '@synaptiq/client';

// Re-export SDK types for existing consumers
export type {
  DataSourceResponse,
  CreateDataSourceRequest,
  ListDataSources200Response,
} from '@synaptiq/client';

/** Backward-compatible alias */
export type DataSourceListResponse = ListDataSources200Response;

@Injectable({ providedIn: 'root' })
export class DataSourceService {
  private readonly api = inject(DataSourcesApiService);

  /**
   * List all data sources for the current tenant.
   */
  async listDataSources(): Promise<DataSourceResponse[]> {
    const result = await firstValueFrom(
      this.api.listDataSources({}),
    );
    return result.dataSources ?? [];
  }

  /**
   * Create a new data source.
   */
  async createDataSource(request: CreateDataSourceRequest): Promise<DataSourceResponse> {
    return firstValueFrom(
      this.api.createDataSource({ createDataSourceRequest: request }),
    );
  }

  /**
   * Get data source by ID.
   */
  async getDataSource(dataSourceId: string): Promise<DataSourceResponse> {
    return firstValueFrom(
      this.api.getDataSource({ dataSourceId }),
    );
  }

  /**
   * Delete a data source.
   */
  async deleteDataSource(dataSourceId: string): Promise<void> {
    await firstValueFrom(
      this.api.deleteDataSource({ dataSourceId }),
    );
  }
}
