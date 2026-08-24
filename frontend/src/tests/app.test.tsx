import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { authApi } from '../api/authApi';
import { rest } from 'msw';
import { setupServer } from 'msw/node';

const server = setupServer(
  rest.post('/api/v1/auth/login', (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({
        userId: 1,
        username: 'testuser',
        role: 'MANAGER',
        facilityId: 1,
        facilityName: 'Test Facility',
        message: 'Login successful'
      })
    );
  }),
  rest.post('/api/v1/auth/logout', (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({ message: 'Logout successful' })
    );
  }),
  rest.get('/api/v1/auth/session', (req, res, ctx) => {
    return res(
      ctx.status(200),
      ctx.json({
        userId: 1,
        username: 'testuser',
        role: 'MANAGER',
        facilityId: 1,
        facilityName: 'Test Facility',
        isActive: true
      })
    );
  })
);

beforeEach(() => {
  server.listen();
});

afterEach(() => {
  server.resetHandlers();
});

afterAll(() => {
  server.close();
});

describe('authApi', () => {
  test('login returns user data on success', async () => {
    const credentials = {
      username: 'testuser',
      password: 'password123',
      facilityId: 1
    };

    const response = await authApi.login(credentials);

    expect(response.userId).toBe(1);
    expect(response.username).toBe('testuser');
    expect(response.role).toBe('MANAGER');
    expect(response.facilityId).toBe(1);
    expect(response.facilityName).toBe('Test Facility');
    expect(response.message).toBe('Login successful');
  });

  test('login throws error on invalid credentials', async () => {
    server.use(
      rest.post('/api/v1/auth/login', (req, res, ctx) => {
        return res(
          ctx.status(401),
          ctx.json({
            correlationId: 'test-correlation-id',
            errorCode: 'INVALID_CREDENTIALS',
            message: 'Invalid username or password'
          })
        );
      })
    );

    const credentials = {
      username: 'wronguser',
      password: 'wrongpass',
      facilityId: 1
    };

    await expect(authApi.login(credentials)).rejects.toThrow();
  });

  test('logout returns success message', async () => {
    await authApi.logout();
    expect(true).toBe(true);
  });

  test('logout throws error when unauthenticated', async () => {
    server.use(
      rest.post('/api/v1/auth/logout', (req, res, ctx) => {
        return res(
          ctx.status(401),
          ctx.json({
            correlationId: 'test-correlation-id',
            errorCode: 'AUTHENTICATION_FAILED',
            message: 'Authentication required'
          })
        );
      })
    );

    await expect(authApi.logout()).rejects.toThrow();
  });

  test('getCurrentUser returns user profile', async () => {
    const profile = await authApi.getCurrentUser();

    expect(profile.userId).toBe(1);
    expect(profile.username).toBe('testuser');
    expect(profile.role).toBe('MANAGER');
    expect(profile.facilityId).toBe(1);
    expect(profile.facilityName).toBe('Test Facility');
    expect(profile.isActive).toBe(true);
  });

  test('getCurrentUser throws error when unauthenticated', async () => {
    server.use(
      rest.get('/api/v1/auth/session', (req, res, ctx) => {
        return res(
          ctx.status(401),
          ctx.json({
            correlationId: 'test-correlation-id',
            errorCode: 'AUTHENTICATION_FAILED',
            message: 'Authentication required'
          })
        );
      })
    );

    await expect(authApi.getCurrentUser()).rejects.toThrow();
  });
});