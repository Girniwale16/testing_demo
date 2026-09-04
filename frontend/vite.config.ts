import { describe, it, expect, beforeEach } from 'vitest';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';
import viteConfig from '../vite.config';

describe('Vite Configuration Tests', () => {
  describe('CSS Preprocessing Configuration', () => {
    it('should include SCSS preprocessor options', () => {
      expect(viteConfig.css).toBeDefined();
      expect(viteConfig.css.preprocessorOptions).toBeDefined();
      expect(viteConfig.css.preprocessorOptions.scss).toBeDefined();
    });

    it('should support responsive breakpoint media query for mobile (≤768px)', () => {
      const additionalData = viteConfig.css.preprocessorOptions.scss.additionalData;
      expect(additionalData).toContain('@media (max-width: 768px)');
    });

    it('should support responsive breakpoint media query for desktop (≥769px)', () => {
      const additionalData = viteConfig.css.preprocessorOptions.scss.additionalData;
      expect(additionalData).toContain('@media (min-width: 769px)');
    });

    it('should configure CSS modules for component-scoped styles', () => {
      expect(viteConfig.css.modules).toBeDefined();
      expect(viteConfig.css.modules.localsConvention).toBe('camelCase');
      expect(viteConfig.css.modules.scopeBehaviour).toBe('local');
    });
  });

  describe('Alias Configuration', () => {
    it('should define @components alias', () => {
      expect(viteConfig.resolve).toBeDefined();
      expect(viteConfig.resolve.alias).toBeDefined();
      expect(viteConfig.resolve.alias['@components']).toBeDefined();
    });

    it('should define @hooks alias', () => {
      expect(viteConfig.resolve.alias['@hooks']).toBeDefined();
    });

    it('should define @utils alias', () => {
      expect(viteConfig.resolve.alias['@utils']).toBeDefined();
    });

    it('should resolve @components to correct path', () => {
      const expectedPath = path.resolve(__dirname, '../src/components');
      expect(viteConfig.resolve.alias['@components']).toBe(expectedPath);
    });

    it('should resolve @hooks to correct path', () => {
      const expectedPath = path.resolve(__dirname, '../src/hooks');
      expect(viteConfig.resolve.alias['@hooks']).toBe(expectedPath);
    });

    it('should resolve @utils to correct path', () => {
      const expectedPath = path.resolve(__dirname, '../src/utils');
      expect(viteConfig.resolve.alias['@utils']).toBe(expectedPath);
    });
  });

  describe('Development Server Configuration', () => {
    it('should configure server with correct port', () => {
      expect(viteConfig.server).toBeDefined();
      expect(viteConfig.server.port).toBe(3000);
    });

    it('should enable host for network access', () => {
      expect(viteConfig.server.host).toBe(true);
    });

    it('should support hot module replacement (HMR)', () => {
      expect(viteConfig.server.hmr).toBeDefined();
    });

    it('should enable HMR overlay for error display', () => {
      expect(viteConfig.server.hmr.overlay).toBe(true);
    });
  });

  describe('React Plugin Configuration', () => {
    it('should include React plugin', () => {
      expect(viteConfig.plugins).toBeDefined();
      expect(Array.isArray(viteConfig.plugins)).toBe(true);
      expect(viteConfig.plugins.length).toBeGreaterThan(0);
    });
  });

  describe('Test Configuration', () => {
    it('should configure test environment as jsdom', () => {
      expect(viteConfig.test).toBeDefined();
      expect(viteConfig.test.environment).toBe('jsdom');
    });

    it('should enable globals for test utilities', () => {
      expect(viteConfig.test.globals).toBe(true);
    });

    it('should define setup files for test initialization', () => {
      expect(viteConfig.test.setupFiles).toBe('./src/tests/setup.ts');
    });
  });

  describe('Build Configuration Compatibility', () => {
    it('should maintain existing configuration structure', () => {
      expect(viteConfig).toHaveProperty('plugins');
      expect(viteConfig).toHaveProperty('css');
      expect(viteConfig).toHaveProperty('resolve');
      expect(viteConfig).toHaveProperty('server');
      expect(viteConfig).toHaveProperty('test');
    });

    it('should not introduce breaking changes to plugin configuration', () => {
      expect(viteConfig.plugins).toBeDefined();
      expect(Array.isArray(viteConfig.plugins)).toBe(true);
    });

    it('should preserve CSS configuration structure', () => {
      expect(viteConfig.css).toHaveProperty('preprocessorOptions');
      expect(viteConfig.css).toHaveProperty('modules');
    });

    it('should preserve resolve configuration structure', () => {
      expect(viteConfig.resolve).toHaveProperty('alias');
    });

    it('should preserve server configuration structure', () => {
      expect(viteConfig.server).toHaveProperty('port');
      expect(viteConfig.server).toHaveProperty('host');
      expect(viteConfig.server).toHaveProperty('hmr');
    });
  });

  describe('Responsive Design Support', () => {
    it('should support both mobile and desktop breakpoints simultaneously', () => {
      const additionalData = viteConfig.css.preprocessorOptions.scss.additionalData;
      expect(additionalData).toContain('@media (max-width: 768px)');
      expect(additionalData).toContain('@media (min-width: 769px)');
    });

    it('should use correct breakpoint values for mobile-first design', () => {
      const additionalData = viteConfig.css.preprocessorOptions.scss.additionalData;
      expect(additionalData).toMatch(/768px/);
      expect(additionalData).toMatch(/769px/);
    });
  });

  describe('Import Path Simplification', () => {
    it('should provide all three required aliases for simplified imports', () => {
      const aliases = Object.keys(viteConfig.resolve.alias);
      expect(aliases).toContain('@components');
      expect(aliases).toContain('@hooks');
      expect(aliases).toContain('@utils');
    });

    it('should have exactly three aliases configured', () => {
      const aliases = Object.keys(viteConfig.resolve.alias);
      expect(aliases.length).toBe(3);
    });
  });
});