import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import ProtectedRoute from './components/ProtectedRoute';
import { logger } from './utils/logger';
import { useEffect } from 'react';

function App() {
  useEffect(() => {
    logger.info('App mounted', { event: 'app_mount' });
  }, []);

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/*"
          element={
            <ProtectedRoute>
              <div style={{ padding: '2rem' }}>
                <h1>Protected Content</h1>
                <p>You are authenticated. Protected routes will be added here.</p>
              </div>
            </ProtectedRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;