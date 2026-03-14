import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ children, allowedRoles = [] }) => {
  const { user } = useAuth();

  // If they aren't logged in at all, kick them to the login page
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // If this page requires specific roles, check if the user has them
  if (allowedRoles.length > 0) {
    const hasRequiredRole = allowedRoles.some(role => user.roles.includes(role));
    
    if (!hasRequiredRole) {
      // They are logged in, but don't have permission. Send them to the generic dashboard.
      return <Navigate to="/dashboard" replace />; 
    }
  }

  // If they pass all checks, render the page!
  return children;
};

export default ProtectedRoute;