/**
 * validateComponentSpec — unit tests (T7.17)
 *
 * Covers all 11 component types with valid, invalid, and edge-case inputs.
 */
import { validateComponentSpec, ComponentType } from './dsl-types';

describe('validateComponentSpec', () => {
  // ── Null / invalid inputs ────────────────────────────────────────

  it('should return null for null input', () => {
    expect(validateComponentSpec(null)).toBeNull();
  });

  it('should return null for undefined input', () => {
    expect(validateComponentSpec(undefined)).toBeNull();
  });

  it('should return null for a string', () => {
    expect(validateComponentSpec('not an object')).toBeNull();
  });

  it('should return null for an array', () => {
    expect(validateComponentSpec([1, 2, 3])).toBeNull();
  });

  it('should return null for a number', () => {
    expect(validateComponentSpec(42)).toBeNull();
  });

  it('should return null for an empty object', () => {
    expect(validateComponentSpec({})).toBeNull();
  });

  it('should return null for an object with missing type', () => {
    expect(validateComponentSpec({ title: 'hello' })).toBeNull();
  });

  it('should return null for an unknown type', () => {
    expect(validateComponentSpec({ type: 'unknown_component' })).toBeNull();
  });

  it('should return null for a numeric type field', () => {
    expect(validateComponentSpec({ type: 123 })).toBeNull();
  });

  // ── item_card ────────────────────────────────────────────────────

  describe('item_card', () => {
    it('should validate a valid item_card spec', () => {
      const spec = {
        type: 'item_card',
        item: { item_id: 'sku-001', data: { name: 'Widget' } },
      };
      const result = validateComponentSpec(spec);
      expect(result).not.toBeNull();
      expect(result!.type).toBe('item_card');
    });

    it('should reject item_card without item', () => {
      expect(validateComponentSpec({ type: 'item_card' })).toBeNull();
    });

    it('should reject item_card with non-object item', () => {
      expect(validateComponentSpec({ type: 'item_card', item: 'not-object' })).toBeNull();
    });
  });

  // ── item_grid ────────────────────────────────────────────────────

  describe('item_grid', () => {
    it('should validate a valid item_grid spec', () => {
      const spec = {
        type: 'item_grid',
        items: [{ item_id: 'a', data: {} }],
        columns: 3,
      };
      expect(validateComponentSpec(spec)).not.toBeNull();
    });

    it('should reject item_grid without items array', () => {
      expect(validateComponentSpec({ type: 'item_grid' })).toBeNull();
    });

    it('should accept item_grid with empty items array', () => {
      expect(validateComponentSpec({ type: 'item_grid', items: [] })).not.toBeNull();
    });
  });

  // ── item_detail ──────────────────────────────────────────────────

  describe('item_detail', () => {
    it('should validate a valid item_detail spec', () => {
      const spec = {
        type: 'item_detail',
        item: { item_id: 'x', data: { name: 'Test' } },
        visible_fields: ['name'],
      };
      expect(validateComponentSpec(spec)).not.toBeNull();
    });

    it('should reject item_detail without item', () => {
      expect(validateComponentSpec({ type: 'item_detail' })).toBeNull();
    });
  });

  // ── comparison_table ─────────────────────────────────────────────

  describe('comparison_table', () => {
    it('should validate a valid comparison_table spec', () => {
      const spec = {
        type: 'comparison_table',
        items: [
          { item_id: 'a', data: { price: 10 } },
          { item_id: 'b', data: { price: 20 } },
        ],
        fields: ['price'],
      };
      expect(validateComponentSpec(spec)).not.toBeNull();
    });

    it('should reject comparison_table without items', () => {
      expect(validateComponentSpec({ type: 'comparison_table', fields: ['a'] })).toBeNull();
    });
  });

  // ── filter_summary ───────────────────────────────────────────────

  describe('filter_summary', () => {
    it('should validate a valid filter_summary spec', () => {
      const spec = {
        type: 'filter_summary',
        filters: [{ field: 'category', label: 'Electronics', value: 'elec' }],
      };
      expect(validateComponentSpec(spec)).not.toBeNull();
    });

    it('should reject filter_summary without filters', () => {
      expect(validateComponentSpec({ type: 'filter_summary' })).toBeNull();
    });
  });

  // ── result_count ─────────────────────────────────────────────────

  describe('result_count', () => {
    it('should validate a valid result_count spec', () => {
      const spec = { type: 'result_count', shown: 10, total: 42 };
      expect(validateComponentSpec(spec)).not.toBeNull();
    });

    it('should reject result_count with string shown', () => {
      expect(validateComponentSpec({ type: 'result_count', shown: '10', total: 42 })).toBeNull();
    });

    it('should reject result_count without total', () => {
      expect(validateComponentSpec({ type: 'result_count', shown: 10 })).toBeNull();
    });

    it('should accept result_count with zero values', () => {
      expect(validateComponentSpec({ type: 'result_count', shown: 0, total: 0 })).not.toBeNull();
    });
  });

  // ── empty_state ──────────────────────────────────────────────────

  describe('empty_state', () => {
    it('should validate a valid empty_state spec', () => {
      const spec = { type: 'empty_state', message: 'No results found', icon: 'search_off' };
      expect(validateComponentSpec(spec)).not.toBeNull();
    });

    it('should reject empty_state without message', () => {
      expect(validateComponentSpec({ type: 'empty_state' })).toBeNull();
    });
  });

  // ── action_confirm ───────────────────────────────────────────────

  describe('action_confirm', () => {
    it('should validate a valid action_confirm spec', () => {
      const spec = {
        type: 'action_confirm',
        action: 'create_item',
        message: 'Item created!',
        confirm_label: 'OK',
      };
      expect(validateComponentSpec(spec)).not.toBeNull();
    });

    it('should reject action_confirm without action', () => {
      expect(validateComponentSpec({ type: 'action_confirm', message: 'Done' })).toBeNull();
    });

    it('should reject action_confirm without message', () => {
      expect(validateComponentSpec({ type: 'action_confirm', action: 'x' })).toBeNull();
    });
  });

  // ── info_banner ──────────────────────────────────────────────────

  describe('info_banner', () => {
    it('should validate a valid info_banner spec', () => {
      const spec = {
        type: 'info_banner',
        title: 'Note',
        body: 'This is important.',
        style: 'info',
      };
      expect(validateComponentSpec(spec)).not.toBeNull();
    });

    it('should reject info_banner without title', () => {
      expect(validateComponentSpec({ type: 'info_banner', body: 'x' })).toBeNull();
    });

    it('should reject info_banner without body', () => {
      expect(validateComponentSpec({ type: 'info_banner', title: 'x' })).toBeNull();
    });
  });

  // ── data_table ───────────────────────────────────────────────────

  describe('data_table', () => {
    it('should validate a valid data_table spec', () => {
      const spec = {
        type: 'data_table',
        columns: [{ field: 'name', label: 'Name' }],
        rows: [{ name: 'Alice' }],
      };
      expect(validateComponentSpec(spec)).not.toBeNull();
    });

    it('should reject data_table without columns', () => {
      expect(validateComponentSpec({ type: 'data_table', rows: [] })).toBeNull();
    });

    it('should reject data_table without rows', () => {
      expect(validateComponentSpec({ type: 'data_table', columns: [] })).toBeNull();
    });
  });

  // ── form_input ───────────────────────────────────────────────────

  describe('form_input', () => {
    it('should validate a valid form_input spec', () => {
      const spec = {
        type: 'form_input',
        fields: [{ field: 'name', label: 'Name', type: 'text', required: true }],
        submit_action: 'create_item',
        submit_label: 'Create',
      };
      expect(validateComponentSpec(spec)).not.toBeNull();
    });

    it('should reject form_input without fields', () => {
      expect(validateComponentSpec({ type: 'form_input', submit_action: 'x' })).toBeNull();
    });

    it('should reject form_input without submit_action', () => {
      expect(
        validateComponentSpec({ type: 'form_input', fields: [{ field: 'a' }] }),
      ).toBeNull();
    });

    it('should accept form_input with empty fields array', () => {
      expect(
        validateComponentSpec({ type: 'form_input', fields: [], submit_action: 'noop' }),
      ).not.toBeNull();
    });
  });

  // ── Suggestions pass-through ─────────────────────────────────────

  it('should preserve suggestions array on valid spec', () => {
    const spec = {
      type: 'result_count',
      shown: 5,
      total: 100,
      suggestions: [{ label: 'More', prompt: 'Show more' }],
    };
    const result = validateComponentSpec(spec);
    expect(result).not.toBeNull();
    expect((result as any).suggestions).toHaveLength(1);
  });

  // ── Round-trip: all valid types ──────────────────────────────────

  const validSpecs: Array<{ type: ComponentType; [k: string]: unknown }> = [
    { type: 'item_card', item: { item_id: 'a', data: {} } },
    { type: 'item_grid', items: [] },
    { type: 'item_detail', item: { item_id: 'b', data: {} } },
    { type: 'comparison_table', items: [] },
    { type: 'filter_summary', filters: [] },
    { type: 'result_count', shown: 0, total: 0 },
    { type: 'empty_state', message: 'empty' },
    { type: 'action_confirm', action: 'a', message: 'm' },
    { type: 'info_banner', title: 't', body: 'b' },
    { type: 'data_table', columns: [], rows: [] },
    { type: 'form_input', fields: [], submit_action: 'x' },
  ];

  validSpecs.forEach((spec) => {
    it(`should validate a minimal "${spec.type}" spec`, () => {
      expect(validateComponentSpec(spec)).not.toBeNull();
    });
  });
});
