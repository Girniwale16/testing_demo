import { useState, FormEvent } from 'react';
import { logger } from '../utils/logger';

interface LoginFormProps {
  onSubmit: (username: string, password: string) => Promise<void>;
}

function LoginForm({ onSubmit }: LoginFormProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [validationError, setValidationError] = useState('');

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();

    setValidationError('');

    if (!username.trim()) {
      const error = 'Username is required';
      setValidationError(error);
      logger.warn('Form validation failed', {
        event: 'form_validation_error',
        field: 'username',
        error
      });
      return;
    }

    if (!password.trim()) {
      const error = 'Password is required';
      setValidationError(error);
      logger.warn('Form validation failed', {
        event: 'form_validation_error',
        field: 'password',
        error
      });
      return;
    }

    if (loading) {
      logger.warn('Form submission prevented - already loading', {
        event: 'form_submit_prevented'
      });
      return;
    }

    setLoading(true);
    logger.info('Form submission started', {
      event: 'form_submit_start',
      username
    });

    try {
      await onSubmit(username, password);
      logger.info('Form submission successful', {
        event: 'form_submit_success',
        username
      });
    } catch (err) {
      logger.error('Form submission failed', {
        event: 'form_submit_error',
        username,
        error: err instanceof Error ? err.message : 'Unknown error'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {validationError && (
        <div
          role="alert"
          aria-live="polite"
          style={{
            padding: '0.75rem',
            marginBottom: '1rem',
            backgroundColor: '#fee',
            color: '#c33',
            borderRadius: '4px',
            border: '1px solid #fcc'
          }}
        >
          {validationError}
        </div>
      )}

      <div style={{ marginBottom: '1rem' }}>
        <label
          htmlFor="username"
          style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}
        >
          Username
        </label>
        <input
          id="username"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          disabled={loading}
          aria-required="true"
          style={{
            width: '100%',
            padding: '0.5rem',
            border: '1px solid #ccc',
            borderRadius: '4px',
            fontSize: '1rem'
          }}
        />
      </div>

      <div style={{ marginBottom: '1.5rem' }}>
        <label
          htmlFor="password"
          style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 500 }}
        >
          Password
        </label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          disabled={loading}
          aria-required="true"
          style={{
            width: '100%',
            padding: '0.5rem',
            border: '1px solid #ccc',
            borderRadius: '4px',
            fontSize: '1rem'
          }}
        />
      </div>

      <button
        type="submit"
        disabled={loading}
        style={{
          width: '100%',
          padding: '0.75rem',
          backgroundColor: loading ? '#ccc' : '#007bff',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          fontSize: '1rem',
          fontWeight: 500,
          cursor: loading ? 'not-allowed' : 'pointer'
        }}
      >
        {loading ? 'Logging in...' : 'Login'}
      </button>
    </form>
  );
}

export default LoginForm;
