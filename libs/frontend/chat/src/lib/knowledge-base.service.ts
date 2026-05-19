/**
 * KnowledgeBaseStateService — manages Knowledge Base state for the chat UI.
 *
 * Wraps the generated SDK's `KnowledgeBaseService` with Angular signals
 * for reactive state management. Handles categories, documents, and
 * KB attachment selection for RAG-grounded conversations.
 */
import { inject, Injectable, signal, computed } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ENVIRONMENT } from '@synaptiq/utils';
import {
  KnowledgeBaseService,
  type KnowledgeCategoryResponse,
  type KnowledgeDocumentResponse,
  type KnowledgeSearchResponse,
} from '@synaptiq/client';

@Injectable({ providedIn: 'root' })
export class KnowledgeBaseStateService {
  private readonly kbApi = inject(KnowledgeBaseService);
  private readonly env = inject(ENVIRONMENT);

  // ── Reactive State ──────────────────────────────────────────────────────

  /** All categories for the current tenant. */
  readonly categories = signal<KnowledgeCategoryResponse[]>([]);

  /** Documents for the currently selected category. */
  readonly documents = signal<KnowledgeDocumentResponse[]>([]);

  /** Currently expanded category in the sidebar. */
  readonly activeCategory = signal<string | null>(null);

  /** Category IDs attached to the current chat conversation for RAG grounding. */
  readonly selectedKbIds = signal<string[]>([]);

  /** Loading indicator for async operations. */
  readonly isLoading = signal(false);

  /** Attached KB category objects (derived from selectedKbIds + categories). */
  readonly attachedKbs = computed(() => {
    const cats = this.categories();
    const ids = this.selectedKbIds();
    return cats.filter(c => ids.includes(c.id));
  });

  // ── Tenant Resolution ───────────────────────────────────────────────────

  private get tenantId(): string | undefined {
    return this.env.tenantId ?? undefined;
  }

  // ── Category Operations ─────────────────────────────────────────────────

  /** Load all knowledge categories for the current tenant. */
  async loadCategories(): Promise<void> {
    this.isLoading.set(true);
    try {
      // The generated SDK uses X-Tenant-ID header for tenant isolation
      const response = await firstValueFrom(
        this.kbApi.getKnowledgeBaseStatus({ xTenantID: this.tenantId })
      );
      // Status endpoint returns categories — but we need the category list.
      // Since the generated SDK doesn't have a dedicated listCategories,
      // we extract from the status or use the documents endpoint to infer.
      // For now, store any categories that come from the status response.
      if ((response as any)?.categories) {
        this.categories.set((response as any).categories);
      }
    } catch (err) {
      console.warn('[KB] Failed to load categories:', err);
    } finally {
      this.isLoading.set(false);
    }
  }

  /** Create a new knowledge category. */
  async createCategory(name: string, description?: string): Promise<KnowledgeCategoryResponse | null> {
    this.isLoading.set(true);
    try {
      const created = await firstValueFrom(
        this.kbApi.createKnowledgeCategory({
          xTenantID: this.tenantId,
          createCategoryRequest: { name, description },
        })
      );
      // Add to local state
      this.categories.update(cats => [...cats, created]);
      return created;
    } catch (err) {
      console.error('[KB] Failed to create category:', err);
      return null;
    } finally {
      this.isLoading.set(false);
    }
  }

  // ── Document Operations ─────────────────────────────────────────────────

  /** Load documents, optionally filtered by category. */
  async loadDocuments(categoryId?: string): Promise<void> {
    this.isLoading.set(true);
    this.activeCategory.set(categoryId ?? null);
    try {
      const response = await firstValueFrom(
        this.kbApi.listKnowledgeDocuments({
          xTenantID: this.tenantId,
          categoryId,
        })
      );
      this.documents.set(response?.documents ?? []);
    } catch (err) {
      console.warn('[KB] Failed to load documents:', err);
      this.documents.set([]);
    } finally {
      this.isLoading.set(false);
    }
  }

  /** Upload a document to a category. */
  async uploadDocument(
    file: File,
    categoryId?: string,
    tags?: string[],
  ): Promise<KnowledgeDocumentResponse | null> {
    try {
      const doc = await firstValueFrom(
        this.kbApi.uploadKnowledgeDocument({
          xTenantID: this.tenantId,
          file: file as any,
          categoryId,
          tags,
        })
      );
      // Refresh document list if viewing the same category
      if (this.activeCategory() === categoryId) {
        this.documents.update(docs => [...docs, doc]);
      }
      return doc;
    } catch (err) {
      console.error('[KB] Failed to upload document:', err);
      return null;
    }
  }

  /** Delete a document by ID. */
  async deleteDocument(docId: string): Promise<boolean> {
    try {
      await firstValueFrom(
        this.kbApi.deleteKnowledgeDocument({
          docId,
          xTenantID: this.tenantId,
        })
      );
      this.documents.update(docs => docs.filter(d => d.id !== docId));
      return true;
    } catch (err) {
      console.error('[KB] Failed to delete document:', err);
      return false;
    }
  }

  // ── Search ──────────────────────────────────────────────────────────────

  /** Search the knowledge base. */
  async search(
    query: string,
    categoryId?: string,
    tags?: string[],
  ): Promise<KnowledgeSearchResponse | null> {
    try {
      return await firstValueFrom(
        this.kbApi.searchKnowledgeBase({
          xTenantID: this.tenantId,
          knowledgeSearchRequest: { query, categoryId, tags },
        })
      );
    } catch (err) {
      console.error('[KB] Search failed:', err);
      return null;
    }
  }

  // ── KB Attachment (Chat Context) ────────────────────────────────────────

  /** Toggle a category's attachment to the current chat session. */
  toggleKbAttachment(categoryId: string): void {
    this.selectedKbIds.update(ids =>
      ids.includes(categoryId)
        ? ids.filter(id => id !== categoryId)
        : [...ids, categoryId]
    );
  }

  /** Detach a specific category from the chat. */
  detachKb(categoryId: string): void {
    this.selectedKbIds.update(ids => ids.filter(id => id !== categoryId));
  }

  /** Clear all KB attachments (e.g., on new conversation). */
  clearAttachments(): void {
    this.selectedKbIds.set([]);
  }
}
