/**
 * Component DSL Type Definitions (T7.1 — REQ-AI11)
 *
 * Defines the union type for all renderable component specs emitted
 * by the LLM and parsed from ```component code fences.
 */

// ---------------------------------------------------------------------------
// Base types
// ---------------------------------------------------------------------------

/** All supported DSL component types. */
export type ComponentType =
  | 'item_card'
  | 'item_grid'
  | 'item_detail'
  | 'comparison_table'
  | 'filter_summary'
  | 'result_count'
  | 'empty_state'
  | 'action_confirm'
  | 'info_banner'
  | 'data_table'
  | 'form_input';

/** Variant styles for item cards. */
export type ItemCardVariant = 'standard' | 'compact' | 'featured';

/** Banner / info styles. */
export type BannerStyle = 'info' | 'warning' | 'success' | 'error';

/** Visual variant for AI suggestion chips. */
export type SuggestionVariant = 'primary' | 'secondary' | 'outline';

/** Supported form field input types. */
export type FormFieldType =
  | 'text'
  | 'number'
  | 'select'
  | 'multi_select'
  | 'date'
  | 'textarea'
  | 'file'
  | 'toggle'
  | 'currency';

/**
 * AI-generated contextual suggestion attached to any component.
 * When clicked, `prompt` is injected into the chat input and auto-sent.
 */
export interface AISuggestion {
  /** Display label shown on the chip (e.g. "Sort by price ↑") */
  label: string;
  /** The chat prompt sent when clicked */
  prompt: string;
  /** Optional Material icon name */
  icon?: string;
  /** Visual variant — defaults to 'secondary' */
  variant?: SuggestionVariant;
}

/** A single catalog item data payload. */
export interface CatalogItemData {
  item_id: string;
  data: Record<string, unknown>;
  status?: string;
  score?: number;
}

/** A filter applied during search. */
export interface FilterEntry {
  field: string;
  value: string | number | boolean;
  op: 'eq' | 'gte' | 'lte' | 'in' | 'contains' | 'between';
  label?: string;
}

/** Column def for data_table. */
export interface DataTableColumn {
  field: string;
  label: string;
  type?: 'text' | 'number' | 'currency' | 'image' | 'badge' | 'action';
  sortable?: boolean;
  width?: string;
}

/** Row action for data_table. */
export interface DataTableAction {
  action_id: string;
  label: string;
  icon?: string;
  variant?: 'primary' | 'secondary' | 'danger';
}

// ---------------------------------------------------------------------------
// Form Field Definitions (T7.14 — REQ-FORM-1)
//
// Shared between:
//   1. TenantCatalogSchema (MongoDB) — admin configures via chat
//   2. FormInputSpec (DSL)           — rendered inline in chat for users
// ---------------------------------------------------------------------------

/** Option for select / multi_select field types. */
export interface FormFieldOption {
  label: string;
  value: string;
}

/** Validation rules for a form field. */
export interface FormFieldValidation {
  /** Minimum value (number/currency) or min length (text/textarea). */
  min?: number;
  /** Maximum value (number/currency) or max length (text/textarea). */
  max?: number;
  /** Regex pattern for text validation. */
  pattern?: string;
  /** Custom error message when validation fails. */
  message?: string;
}

/**
 * Predicate that controls conditional field visibility.
 *
 * When present on a FormFieldDef, the field is only shown when the
 * referenced `field` value satisfies the `operator` + `value` check.
 *
 * Example: Show "warranty_months" only when category == "electronics":
 * ```json
 * { "field": "category", "operator": "eq", "value": "electronics" }
 * ```
 */
export interface VisibleWhenPredicate {
  /** The field key whose value is checked. */
  field: string;
  /** Comparison operator. */
  operator: 'eq' | 'neq' | 'in' | 'not_in' | 'truthy' | 'falsy';
  /** The value to compare against (unused for truthy/falsy). */
  value?: unknown;
}

/**
 * A single field definition within a form.
 *
 * This type is the **single source of truth** used in two contexts:
 * - **Admin schema config**: The admin defines these via chat to build
 *   their tenant's catalog schema (stored in MongoDB).
 * - **End-user form fill**: The AI reads the tenant schema and emits
 *   a FormInputSpec with these fields for the user to fill in.
 */
export interface FormFieldDef {
  /** Unique key for this field (e.g. "warranty_months"). */
  field: string;
  /** Human-readable display label. */
  label: string;
  /** Input type. */
  type: FormFieldType;
  /** Whether the field must be filled before submission. */
  required?: boolean;
  /** Placeholder text shown in the input. */
  placeholder?: string;
  /** Unit suffix displayed alongside the input (e.g. "months", "kg"). */
  unit?: string;
  /** Options for select / multi_select fields. */
  options?: FormFieldOption[];
  /** Validation rules. */
  validation?: FormFieldValidation;
  /** Default value pre-filled when the form renders. */
  default_value?: unknown;
  /** Hint text shown below the field. */
  hint?: string;
  /** Display order (lower = higher). */
  position?: number;
  /** Currency code for currency fields (e.g. "USD", "INR"). */
  currency_code?: string;
  /** Accepted file types for file fields (e.g. ".csv,.xlsx"). */
  accept?: string;
  /** Conditional visibility predicate — field is hidden until this evaluates true. */
  visible_when?: VisibleWhenPredicate;
}

// ---------------------------------------------------------------------------
// Component Specs (T7.1 — one per component type)
// ---------------------------------------------------------------------------

export interface ItemCardSpec {
  type: 'item_card';
  item: CatalogItemData;
  variant?: ItemCardVariant;
  actions?: string[];
  suggestions?: AISuggestion[];
}

export interface ItemGridSpec {
  type: 'item_grid';
  items: CatalogItemData[];
  columns?: 2 | 3 | 4 | 5 | 6;
  suggestions?: AISuggestion[];
}

export interface ItemDetailSpec {
  type: 'item_detail';
  item: CatalogItemData;
  fields?: string[];
  actions?: string[];
  suggestions?: AISuggestion[];
}

export interface ComparisonTableSpec {
  type: 'comparison_table';
  items: CatalogItemData[];
  fields?: string[];
  suggestions?: AISuggestion[];
}

export interface FilterSummarySpec {
  type: 'filter_summary';
  filters: FilterEntry[];
  suggestions?: AISuggestion[];
}

export interface ResultCountSpec {
  type: 'result_count';
  shown: number;
  total: number;
  suggestions?: AISuggestion[];
}

export interface EmptyStateSpec {
  type: 'empty_state';
  message: string;
  /** @deprecated Use AISuggestion[] `ai_suggestions` instead for richer chips */
  suggestions?: string[];
  icon?: string;
  ai_suggestions?: AISuggestion[];
}

export interface ActionConfirmSpec {
  type: 'action_confirm';
  action: string;
  item_id?: string;
  message: string;
  confirm_label?: string;
  cancel_label?: string;
  suggestions?: AISuggestion[];
}

export interface InfoBannerSpec {
  type: 'info_banner';
  title: string;
  body: string;
  style?: BannerStyle;
  dismissible?: boolean;
  suggestions?: AISuggestion[];
}

export interface DataTableSpec {
  type: 'data_table';
  columns: DataTableColumn[];
  rows: Record<string, unknown>[];
  selectable?: boolean;
  actions?: DataTableAction[];
  title?: string;
  suggestions?: AISuggestion[];
}

/**
 * Form rendered inline in the chat conversation (T7.14 — REQ-FORM-1).
 *
 * Emitted by the AI engine when structured data entry is required.
 * Fields are defined by the tenant's catalog schema or dynamically
 * by the AI for admin configuration flows.
 */
export interface FormInputSpec {
  type: 'form_input';
  /** Form title displayed above the fields. */
  title?: string;
  /** Optional description / instruction text. */
  description?: string;
  /** The form fields to render (order follows `position` then array index). */
  fields: FormFieldDef[];
  /** Label for the submit button. */
  submit_label?: string;
  /** Action ID emitted on submission (e.g. "create_item", "update_schema"). */
  submit_action: string;
  /** If true, show a cancel button. */
  cancellable?: boolean;
  /** Pre-filled values keyed by field key. */
  initial_values?: Record<string, unknown>;
  /** AI suggestion chips rendered below the form. */
  suggestions?: AISuggestion[];
}

// ---------------------------------------------------------------------------
// Union type
// ---------------------------------------------------------------------------

/** Discriminated union of all component specs. */
export type ComponentSpec =
  | ItemCardSpec
  | ItemGridSpec
  | ItemDetailSpec
  | ComparisonTableSpec
  | FilterSummarySpec
  | ResultCountSpec
  | EmptyStateSpec
  | ActionConfirmSpec
  | InfoBannerSpec
  | DataTableSpec
  | FormInputSpec;

// ---------------------------------------------------------------------------
// T7.3 — Validation utility
// ---------------------------------------------------------------------------

const VALID_TYPES: ReadonlySet<string> = new Set<ComponentType>([
  'item_card', 'item_grid', 'item_detail', 'comparison_table',
  'filter_summary', 'result_count', 'empty_state', 'action_confirm',
  'info_banner', 'data_table', 'form_input',
]);

/**
 * Validate a raw JSON object as a ComponentSpec (T7.3 — REQ-AI15).
 *
 * Returns the typed spec if valid, or null if invalid.
 */
export function validateComponentSpec(raw: unknown): ComponentSpec | null {
  if (!raw || typeof raw !== 'object' || Array.isArray(raw)) {
    return null;
  }

  const obj = raw as Record<string, unknown>;

  if (!obj['type'] || typeof obj['type'] !== 'string') {
    return null;
  }

  if (!VALID_TYPES.has(obj['type'])) {
    return null;
  }

  // Type-specific required field checks
  switch (obj['type']) {
    case 'item_card':
      if (!obj['item'] || typeof obj['item'] !== 'object') return null;
      break;
    case 'item_grid':
      if (!Array.isArray(obj['items'])) return null;
      break;
    case 'item_detail':
      if (!obj['item'] || typeof obj['item'] !== 'object') return null;
      break;
    case 'comparison_table':
      if (!Array.isArray(obj['items'])) return null;
      break;
    case 'filter_summary':
      if (!Array.isArray(obj['filters'])) return null;
      break;
    case 'result_count':
      if (typeof obj['shown'] !== 'number' || typeof obj['total'] !== 'number') return null;
      break;
    case 'empty_state':
      if (typeof obj['message'] !== 'string') return null;
      break;
    case 'action_confirm':
      if (typeof obj['action'] !== 'string' || typeof obj['message'] !== 'string') return null;
      break;
    case 'info_banner':
      if (typeof obj['title'] !== 'string' || typeof obj['body'] !== 'string') return null;
      break;
    case 'data_table':
      if (!Array.isArray(obj['columns']) || !Array.isArray(obj['rows'])) return null;
      break;
    case 'form_input':
      if (!Array.isArray(obj['fields']) || typeof obj['submit_action'] !== 'string') return null;
      break;
    default:
      return null;
  }

  return obj as unknown as ComponentSpec;
}
