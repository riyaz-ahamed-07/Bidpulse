import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import apiClient from '../api/axiosConfig';
import Layout from '../components/Layout';

export default function WalletPage() {
  const [balance, setBalance] = useState(0);
  const [reserved, setReserved] = useState(0);
  const [depositAmount, setDepositAmount] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const fetchWallet = async () => {
    try {
      const res = await apiClient.get('/wallets/me');
      setBalance(res.data.balance);
      setReserved(res.data.reserved);
    } catch (err) {
      console.error("Failed to fetch wallet", err);
    }
  };

  useEffect(() => { fetchWallet(); }, []);

  const handleDeposit = async (e) => {
    e.preventDefault();
    if (!depositAmount || isNaN(depositAmount) || depositAmount <= 0) return;
    
    setIsLoading(true);
    try {
      await apiClient.post(`/wallets/deposit?amount=${depositAmount}`);
      toast.success(`🤑 Successfully deposited $${depositAmount}!`);
      setDepositAmount('');
      fetchWallet(); 
    } catch (err) {
      toast.error('Deposit failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Layout>
      <div className="max-w-5xl mx-auto space-y-8 animate-float" style={{ animationDuration: '8s' }}>
        <div>
          <h1 className="text-4xl font-black mb-2">Digital <span className="text-gradient">Vault</span></h1>
          <p className="text-gray-400 text-lg">Manage your assets and buying power.</p>
        </div>
        
        {/* Neon Money Card */}
        <div className="glass-card p-10 bg-gradient-to-br from-violet-900/40 to-fuchsia-900/20 border-violet-500/30 shadow-[0_0_40px_rgba(139,92,246,0.15)] relative overflow-hidden">
          {/* Decorative glowing orb */}
          <div className="absolute top-0 right-0 w-64 h-64 bg-fuchsia-500/20 rounded-full blur-[80px] -mr-20 -mt-20"></div>
          
          <div className="relative z-10">
            <p className="text-violet-300 text-sm font-bold uppercase tracking-[0.2em] mb-2">Available Buying Power</p>
            <h2 className="text-6xl md:text-7xl font-black tracking-tight text-white mb-4">
              <span className="text-violet-400">$</span>{balance.toFixed(2)}
            </h2>
            
            {reserved > 0 && (
              <div className="inline-flex items-center gap-3 bg-black/40 border border-amber-500/30 px-4 py-2 rounded-xl mt-4">
                <span className="w-2.5 h-2.5 rounded-full bg-amber-400 shadow-[0_0_10px_#fbbf24] animate-pulse"></span> 
                <span className="text-amber-200 font-medium">${reserved.toFixed(2)} locked in active bids</span>
              </div>
            )}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="glass-card p-8">
            <h3 className="font-bold text-xl text-white mb-6 flex items-center gap-3">
              <span className="text-2xl">⚡</span> Quick Deposit
            </h3>
            <form onSubmit={handleDeposit} className="space-y-6">
              <div>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <span className="text-neonPurple font-bold text-xl">$</span>
                  </div>
                  <input 
                    type="number" 
                    value={depositAmount} 
                    onChange={(e) => setDepositAmount(e.target.value)} 
                    placeholder="0.00" 
                    min="1"
                    required 
                    className="input-field pl-10 text-lg py-4"
                  />
                </div>
              </div>
              <button type="submit" className="w-full btn-success py-4 text-lg" disabled={isLoading}>
                {isLoading ? 'Encrypting Transfer...' : 'ADD FUNDS'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </Layout>
  );
}