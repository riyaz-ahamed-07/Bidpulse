import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Layout({ children }) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <div className="min-h-screen flex">
      {/* Mobile Sidebar Overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-40 md:hidden" onClick={() => setSidebarOpen(false)}></div>
      )}

      {/* Glass Sidebar */}
      <aside className={`fixed inset-y-0 left-0 z-50 w-64 glass-card rounded-none border-y-0 border-l-0 transform ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'} md:relative md:translate-x-0 transition-transform duration-300 flex flex-col`}>
        <div className="flex items-center justify-between h-20 px-6 border-b border-glassBorder">
          <span className="text-2xl font-black tracking-tight text-white flex items-center gap-2">
            <span className="w-10 h-10 rounded-xl bg-gradient-to-br from-violet-500 to-fuchsia-500 flex items-center justify-center shadow-lg shadow-violet-500/30">B</span>
            Bid<span className="text-gradient">Pulse</span>
          </span>
          <button className="md:hidden text-white/50 hover:text-white" onClick={() => setSidebarOpen(false)}>✕</button>
        </div>
        
        <nav className="p-6 flex-1 space-y-3">
          <Link to="/dashboard" className={`block px-4 py-3 rounded-xl font-medium transition-all ${isActive('/dashboard') ? 'bg-white/10 text-white shadow-inner border border-white/5' : 'text-gray-400 hover:text-white hover:bg-white/5'}`}>🚀 Live Markets</Link>
          <Link to="/wallet" className={`block px-4 py-3 rounded-xl font-medium transition-all ${isActive('/wallet') ? 'bg-white/10 text-white shadow-inner border border-white/5' : 'text-gray-400 hover:text-white hover:bg-white/5'}`}>💰 My Wallet</Link>
          
          {user?.roles?.includes('SELLER') && (
             <Link to="/seller" className={`block px-4 py-3 rounded-xl font-medium transition-all ${isActive('/seller') ? 'bg-white/10 text-white shadow-inner border border-white/5' : 'text-gray-400 hover:text-white hover:bg-white/5'}`}>📦 Seller Panel</Link>
          )}
          {user?.roles?.includes('ADMIN') && (
             <Link to="/admin" className={`block px-4 py-3 rounded-xl font-medium transition-all ${isActive('/admin') ? 'bg-rose-500/20 text-rose-300 border border-rose-500/20' : 'text-gray-400 hover:text-white hover:bg-white/5'}`}>👑 Admin Panel</Link>
          )}
        </nav>

        <div className="p-6 border-t border-glassBorder">
          <div className="flex items-center gap-3 mb-4">
            <div className="w-10 h-10 rounded-full bg-gradient-to-r from-cyan-400 to-blue-500 flex items-center justify-center font-bold text-white shadow-lg shadow-cyan-500/30">
              {user?.name?.charAt(0) || 'U'}
            </div>
            <div className="overflow-hidden">
              <p className="text-sm font-bold text-white truncate">{user?.name || 'User'}</p>
              <p className="text-xs text-gray-400 truncate">{user?.email}</p>
            </div>
          </div>
          <button onClick={handleLogout} className="w-full py-2.5 rounded-xl font-bold text-rose-400 hover:text-white hover:bg-rose-500/20 border border-transparent hover:border-rose-500/30 transition-all">
            Log Out
          </button>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 h-screen overflow-y-auto">
        <header className="h-20 flex items-center justify-between px-6 z-10 sticky top-0 bg-dark/50 backdrop-blur-md border-b border-white/5">
          <button className="md:hidden text-white/70 hover:text-white" onClick={() => setSidebarOpen(true)}>
            <svg className="w-7 h-7" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" /></svg>
          </button>
          <div className="flex-1 flex justify-end">
            <button className="relative p-2 text-gray-400 hover:text-white transition-colors">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>
              <span className="absolute top-2 right-2 w-2 h-2 bg-neonPink rounded-full shadow-[0_0_10px_#ec4899]"></span>
            </button>
          </div>
        </header>
        <main className="flex-1 p-6 md:p-8 lg:p-10">
          {children}
        </main>
      </div>
    </div>
  );
}