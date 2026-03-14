import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [email, setEmail] = useState('seller@test.com');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const navigate = useNavigate();
  const { fetchCurrentUser } = useAuth();

  const handleLogin = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');
    
    try {
      const res = await apiClient.post('/auth/token', { email, password });
      localStorage.setItem('accessToken', res.data.accessToken);
      localStorage.setItem('refreshToken', res.data.refreshToken);
      
      const userData = await fetchCurrentUser(); 

      if (userData?.roles.includes('ADMIN')) navigate('/admin');
      else if (userData?.roles.includes('SELLER')) navigate('/seller');
      else navigate('/dashboard');

    } catch (err) {
      setError('Invalid credentials. Please verify your email and password.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#F3F4F6] px-4">
      <div className="max-w-md w-full bg-white rounded-card shadow-soft p-8 border border-gray-100">
        <div className="text-center mb-8">
          <div className="w-12 h-12 bg-primary text-white rounded-lg mx-auto flex items-center justify-center text-xl font-black mb-4">B</div>
          <h1 className="text-2xl font-bold text-[#0F1724]">Sign in to BidPulse</h1>
          <p className="text-[#4B5563] text-sm mt-2">Enter your details to access your dashboard.</p>
        </div>
        
        {error && (
          <div className="mb-6 p-3 bg-red-50 border border-red-100 text-[#E53E3E] text-sm rounded-btn" role="alert">
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-5">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-[#0F1724] mb-1">Email address</label>
            <input 
              id="email" 
              type="email" 
              required 
              className="input-field" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
            />
          </div>
          <div>
            <div className="flex justify-between items-center mb-1">
              <label htmlFor="password" className="block text-sm font-medium text-[#0F1724]">Password</label>
            </div>
            <input 
              id="password" 
              type="password" 
              required 
              className="input-field" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
            />
          </div>
          
          <button 
            type="submit" 
            className="w-full btn-primary"
            disabled={isLoading}
          >
            {isLoading ? 'Authenticating...' : 'Sign in'}
          </button>
        </form>
        
        <p className="mt-8 text-center text-sm text-[#4B5563]">
          Don't have an account? <Link to="/register" className="text-primary hover:underline font-medium focus-visible:ring-2 rounded">Register here</Link>
        </p>
      </div>
    </div>
  );
}