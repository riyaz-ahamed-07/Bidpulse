import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/axiosConfig';

export default function RegisterPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [status, setStatus] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setStatus('');
    
    try {
      await apiClient.post('/users', { name, email, password });
      setStatus('✅ Identity verified! Redirecting to secure login...');
      
      setTimeout(() => {
        navigate('/login');
      }, 2000);

    } catch (err) {
      setStatus('❌ Registration failed. Email might already exist in the mainframe.');
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center px-4 relative">
      {/* Decorative background glows */}
      <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-neonPurple/20 rounded-full blur-[100px] pointer-events-none"></div>
      <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-neonCyan/20 rounded-full blur-[100px] pointer-events-none"></div>

      <div className="max-w-md w-full glass-card p-8 border-t-4 border-t-neonCyan relative z-10">
        <div className="text-center mb-8">
          <div className="w-12 h-12 bg-gradient-to-br from-cyan-400 to-blue-500 text-white rounded-xl mx-auto flex items-center justify-center text-xl font-black mb-4 shadow-[0_0_15px_rgba(6,182,212,0.4)]">B</div>
          <h1 className="text-3xl font-black text-white">Create <span className="text-gradient">Identity</span></h1>
          <p className="text-gray-400 text-sm mt-2">Register to access the exclusive marketplace.</p>
        </div>
        
        {status && (
          <div className={`mb-6 p-4 text-sm rounded-xl font-bold ${status.includes('✅') ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-rose-500/20 text-rose-400 border border-rose-500/30'}`} role="alert">
            {status}
          </div>
        )}

        <form onSubmit={handleRegister} className="space-y-5">
          <div>
            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">Operative Name</label>
            <input 
              type="text" 
              value={name} 
              onChange={(e) => setName(e.target.value)} 
              placeholder="John Doe" 
              required 
              className="input-field"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">Email Address</label>
            <input 
              type="email" 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              placeholder="agent@bidpulse.com" 
              required 
              className="input-field"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-1">Secure Password</label>
            <input 
              type="password" 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              placeholder="••••••••" 
              required 
              className="input-field"
            />
          </div>
          
          <button type="submit" className="w-full btn-primary" disabled={isLoading}>
            {isLoading ? 'ENCRYPTING...' : 'INITIALIZE ACCOUNT'}
          </button>
        </form>
        
        <p className="mt-8 text-center text-sm text-gray-400">
          Already authorized? <Link to="/login" className="text-neonCyan hover:text-white font-bold transition-colors">Login here</Link>
        </p>
      </div>
    </div>
  );
}