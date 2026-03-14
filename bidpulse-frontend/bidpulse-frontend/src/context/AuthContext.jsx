import { createContext, useState, useEffect, useContext } from 'react';
import apiClient from '../api/axiosConfig';

// 1. Create the Context (The Loudspeaker)
const AuthContext = createContext();

// 2. Create a custom hook so other files can easily listen to the loudspeaker
export const useAuth = () => useContext(AuthContext);

// 3. Create the Provider (The Engine that manages the data)
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true); // Prevents the app from flashing while we check the token

  useEffect(() => {
    // When the app first loads, check if we have a saved token
    const token = localStorage.getItem('accessToken');
    if (token) {
      fetchCurrentUser();
    } else {
      setLoading(false);
    }
  }, []);

const fetchCurrentUser = async () => {
    try {
      const response = await apiClient.get('/users/me');
      setUser(response.data); 
      return response.data; // <-- ADD THIS LINE so login page can read it immediately
    } catch (error) {
      console.error("Token invalid or expired", error);
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      setUser(null);
      return null;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, setUser, fetchCurrentUser, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};