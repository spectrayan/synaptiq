/**
 * ThemeService — Phase 10 (T10.4, T10.6, T10.7, T10.9).
 *
 * Manages the full tenant theming lifecycle:
 *   - T10.4: Injects branding values as CSS custom properties
 *   - T10.6: Loads tenant branding on init via public API
 *   - T10.7: Handles ?theme= and ?lang= URL param overrides
 *   - T10.9: Persists end-user preferences to localStorage
 *
 * CSS custom properties injected:
 *   --sq-brand-primary, --sq-brand-secondary, --sq-brand-bg,
 *   --sq-brand-heading-font, --sq-brand-body-font, --sq-brand-ui-font
 */
import { Injectable, signal, effect, computed, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import {
  ConfigService,
  BrandingConfig,
  ThemePreset,
  PersonalizationConfig,
} from '@synaptiq/chat';

export type ThemeMode = 'dark' | 'light';
export type BubbleStyle = 'rounded' | 'sharp' | 'pill';

const STORAGE_KEY = 'sq-theme';
const STORAGE_FONT_KEY = 'sq-user-font';
const STORAGE_BUBBLE_KEY = 'sq-user-bubble';

/** Default branding when tenant has not configured any */
const DEFAULT_BRANDING: BrandingConfig = {
  logo_url: '',
  primary_color: '#6366F1',
  secondary_color: '#8B5CF6',
  background_style: 'dark',
  heading_font: 'Inter',
  body_font: 'Inter',
  ui_font: 'Inter',
  favicon_url: '',
  page_title: '',
  show_platform_branding: true,
};

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private readonly configService = inject(ConfigService);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));

  // ── Core signals ────────────────────────────────────────────────────
  /** Current active theme mode */
  readonly theme = signal<ThemeMode>(this._loadPreference());
  /** Whether dark mode is active */
  readonly isDark = computed(() => this.theme() === 'dark');

  /** Tenant branding config (loaded from API) */
  readonly branding = signal<BrandingConfig>(DEFAULT_BRANDING);
  /** Available theme presets from tenant */
  readonly themes = signal<ThemePreset[]>([]);
  /** Active theme preset (if any) */
  readonly activePreset = signal<ThemePreset | null>(null);
  /** Personalization toggles from admin config */
  readonly personalization = signal<PersonalizationConfig>({
    allow_theme_switch: false,
    allow_font_switch: false,
    allow_bubble_style: false,
  });

  // ── End-user preferences (T10.9) ────────────────────────────────────
  readonly userFont = signal<string>(this.isBrowser ? (localStorage.getItem(STORAGE_FONT_KEY) || '') : '');
  readonly bubbleStyle = signal<BubbleStyle>(
    this.isBrowser ? ((localStorage.getItem(STORAGE_BUBBLE_KEY) as BubbleStyle) || 'rounded') : 'rounded',
  );

  /** Whether tenant branding has been loaded */
  readonly brandingLoaded = signal(false);

  constructor() {
    if (!this.isBrowser) return;

    // Apply theme to DOM whenever signal changes
    effect(() => {
      const mode = this.theme();
      document.documentElement.setAttribute('data-theme', mode);
      localStorage.setItem(STORAGE_KEY, mode);
    });

    // Apply branding CSS vars whenever branding signal changes
    effect(() => {
      const b = this.branding();
      this._applyCSSVars(b);
    });

    // Apply user font preference
    effect(() => {
      const font = this.userFont();
      if (font) {
        document.documentElement.style.setProperty('--sq-brand-body-font', `'${font}', system-ui, sans-serif`);
        localStorage.setItem(STORAGE_FONT_KEY, font);
      }
    });

    // Apply bubble style
    effect(() => {
      const style = this.bubbleStyle();
      document.documentElement.setAttribute('data-bubble', style);
      localStorage.setItem(STORAGE_BUBBLE_KEY, style);
    });
  }

  // ── Public API ──────────────────────────────────────────────────────

  /** Toggle between dark and light mode */
  toggle(): void {
    this.theme.set(this.isDark() ? 'light' : 'dark');
  }

  /** Set a specific theme mode */
  setTheme(mode: ThemeMode): void {
    this.theme.set(mode);
  }

  /** Set end-user font preference (T10.9) */
  setUserFont(font: string): void {
    this.userFont.set(font);
  }

  /** Set end-user bubble style (T10.9) */
  setBubbleStyle(style: BubbleStyle): void {
    this.bubbleStyle.set(style);
  }

  /** Apply a named theme preset */
  applyPreset(preset: ThemePreset): void {
    this.activePreset.set(preset);
    // Override branding signals with preset values
    this.branding.update(b => ({
      ...b,
      primary_color: preset.primary_color,
      secondary_color: preset.secondary_color,
      background_style: preset.background_style,
      heading_font: preset.heading_font,
      body_font: preset.body_font,
    }));
    // Also set the theme mode
    if (preset.background_style !== 'auto') {
      this.setTheme(preset.background_style as ThemeMode);
    }
  }

  /**
   * Load tenant branding from public API (T10.6).
   *
   * Called once during app initialization. Also handles URL param
   * overrides (T10.7).
   */
  async loadTenantBranding(): Promise<void> {
    try {
      const response = await this.configService.getPublicBranding();

      this.branding.set(response.branding);
      this.themes.set(response.themes);
      this.personalization.set(response.personalization);

      // Apply background_style from branding
      const bgStyle = response.branding.background_style;
      if (bgStyle === 'auto') {
        // Respect OS preference for auto mode
        const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches;
        this.setTheme(prefersDark ? 'dark' : 'light');
      } else if (!this.isBrowser || !localStorage.getItem(STORAGE_KEY)) {
        // Only set from branding if user hasn't explicitly chosen
        this.setTheme(bgStyle as ThemeMode);
      }

      // Apply default theme preset if one exists
      if (response.default_theme) {
        this.applyPreset(response.default_theme);
      }

      // Set page title (REQ-B9)
      if (response.branding.page_title) {
        document.title = response.branding.page_title;
      }

      // Set favicon (REQ-B8)
      if (response.branding.favicon_url) {
        this._setFavicon(response.branding.favicon_url);
      }

      this.brandingLoaded.set(true);
    } catch (err) {
      // Silently fall back to defaults — don't break the app
      console.warn('Failed to load tenant branding, using defaults:', err);
      this.brandingLoaded.set(true);
    }

    // Handle URL param overrides (T10.7)
    this._handleURLParams();
  }

  // ── Private methods ─────────────────────────────────────────────────

  /** Load theme preference from localStorage or default to system/dark */
  private _loadPreference(): ThemeMode {
    if (!this.isBrowser) return 'dark';

    const stored = localStorage.getItem(STORAGE_KEY) as ThemeMode | null;
    if (stored === 'light' || stored === 'dark') return stored;

    // Respect OS preference
    if (window.matchMedia?.('(prefers-color-scheme: light)').matches) {
      return 'light';
    }
    return 'dark';
  }

  /**
   * Apply branding config as CSS custom properties (T10.4 — REQ-B12).
   *
   * These vars can be consumed by any component via `var(--sq-brand-*)`.
   */
  private _applyCSSVars(b: BrandingConfig): void {
    if (!this.isBrowser) return;
    const hexToRgb = (hex: string) => {
      if (!hex) return '179, 157, 219';
      const shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
      hex = hex.replace(shorthandRegex, (m, r, g, b) => r + r + g + g + b + b);
      const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
      return result ? `${parseInt(result[1], 16)}, ${parseInt(result[2], 16)}, ${parseInt(result[3], 16)}` : '179, 157, 219';
    };

    const root = document.documentElement;
    root.style.setProperty('--sq-brand-primary', b.primary_color);
    root.style.setProperty('--sq-brand-primary-rgb', hexToRgb(b.primary_color));
    root.style.setProperty('--sq-brand-secondary', b.secondary_color);
    root.style.setProperty('--sq-brand-secondary-rgb', hexToRgb(b.secondary_color));
    root.style.setProperty('--sq-brand-heading-font', `'${b.heading_font}', system-ui, sans-serif`);
    root.style.setProperty('--sq-brand-body-font', `'${b.body_font}', system-ui, sans-serif`);
    root.style.setProperty('--sq-brand-ui-font', `'${b.ui_font}', system-ui, sans-serif`);

    // Also inject the Google Fonts link for the configured fonts
    this._loadGoogleFont(b.heading_font);
    if (b.body_font !== b.heading_font) {
      this._loadGoogleFont(b.body_font);
    }
    if (b.ui_font !== b.heading_font && b.ui_font !== b.body_font) {
      this._loadGoogleFont(b.ui_font);
    }
  }

  /** Dynamically load a Google Font if not already loaded */
  private _loadGoogleFont(fontName: string): void {
    if (!this.isBrowser) return;
    if (!fontName || fontName === 'Inter') return; // Inter is already in the base styles

    const id = `sq-font-${fontName.toLowerCase().replace(/\s+/g, '-')}`;
    if (document.getElementById(id)) return;

    const link = document.createElement('link');
    link.id = id;
    link.rel = 'stylesheet';
    link.href = `https://fonts.googleapis.com/css2?family=${encodeURIComponent(fontName)}:wght@300;400;500;600;700&display=swap`;
    document.head.appendChild(link);
  }

  /** Set the page favicon dynamically (REQ-B8) */
  private _setFavicon(url: string): void {
    if (!this.isBrowser) return;
    let link = document.querySelector<HTMLLinkElement>("link[rel~='icon']");
    if (!link) {
      link = document.createElement('link');
      link.rel = 'icon';
      document.head.appendChild(link);
    }
    link.href = url;
  }

  /**
   * Handle `?theme=` and `?lang=` URL param overrides (T10.7 — REQ-H5).
   *
   * Example: `?theme=light&lang=es`
   */
  private _handleURLParams(): void {
    if (!this.isBrowser) return;

    const params = new URLSearchParams(window.location.search);

    // ?theme= override
    const themeParam = params.get('theme');
    if (themeParam === 'dark' || themeParam === 'light') {
      this.setTheme(themeParam);
    } else if (themeParam) {
      // Could be a preset theme_id
      const preset = this.themes().find(t => t.theme_id === themeParam);
      if (preset) {
        this.applyPreset(preset);
      }
    }

    // ?lang= override — store for consumption by i18n or AI engine
    const langParam = params.get('lang');
    if (langParam) {
      document.documentElement.setAttribute('lang', langParam);
      localStorage.setItem('sq-lang', langParam);
    }
  }
}
