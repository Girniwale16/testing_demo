import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import LoginForm from '../components/LoginForm';
import ErrorBanner from '../components/ErrorBanner';
import { useAuth } from '../hooks/useAuth';
import { logger } from '../utils/logger';

function LoginPage() {
  const navigate = useNavigate();
  const { user, error, login, clearError } = useAuth();

  useEffect(() => {
    logger.info('LoginPage mounted', { event: 'login_page_mount' });
  }, []);

  useEffect(() => {
    if (user) {
      const targetRoute = '/';
      logger.info('Login successful, navigating to default route', {
        event: 'login_navigation',
        username: user.username,
        role: user.role,
        target_route: targetRoute
      });
      navigate(targetRoute);
    }
  }, [user, navigate]);

  const handleLogin = async (username: string, password: string) => {
    try {
      await login(username, password);
    } catch (err) {
      logger.error('Login failed in LoginPage', {
        event: 'login_page_error',
        error: err instanceof Error ? err.message : 'Unknown error'
      });
    }
  };

  return (
    <div style={{ 
      minHeight: '100vh', 
      display: 'flex', 
      alignItems: 'center', 
      justifyContent: 'center',
      backgroundColor: '#f5f5f5',
      padding: '1rem'
    }}>
      <div style={{ 
        width: '100%', 
        maxWidth: '400px',
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '8px',
        boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
      }}>
        <h1 style={{ marginBottom: '1.5rem', textAlign: 'center' }}>Login</h1>
        {error && (
          <ErrorBanner 
            message={error} 
            onDismiss={clearError}
          />
        )}
        <LoginForm onSubmit={handleLogin} />
      </div>
    </div>
  );
}

export default LoginPage;