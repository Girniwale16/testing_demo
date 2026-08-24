import { ReactNode } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { logger } from '../utils/logger';

interface ProtectedRouteProps {
  children: ReactNode;
}

function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        minHeight: '100vh' 
      }}>
        <p>Loading...</p>
      </div>
    );
  }

  if (!user) {
    logger.warn('Unauthenticated access attempt to protected route', {
      event: 'protected_route_redirect',
      target_route: location.pathname,
      authenticated: false
    });
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  logger.info('Authenticated access to protected route', {
    event: 'protected_route_access',
    target_route: location.pathname,
    authenticated: true,
    username: user.username
  });

  return <>{children}</>;
}

export default ProtectedRoute;