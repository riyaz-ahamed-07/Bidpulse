import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import apiClient from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import CountdownTimer from '../components/CountdownTimer';
import Layout from '../components/Layout';

export default function DashboardPage() {
  const [auctions, setAuctions] = useState([]);
  const navigate = useNavigate();
  const { user } = useAuth();

  const fetchAuctions = async () => {
    try {
      const response = await apiClient.get('/auctions');
      setAuctions(response.data.content || response.data);
    } catch (error) {
      if (error.response?.status === 401) navigate('/login');
    }
  };

  useEffect(() => {
    fetchAuctions();
    const intervalId = setInterval(fetchAuctions, 5000);
    return () => clearInterval(intervalId);
  }, []);

  const handleApplySeller = async () => {
    try {
      await apiClient.post('/admin/apply-seller', { reason: "Applying from dashboard." });
      toast.success("✨ Application submitted! Welcome to the big leagues.");
    } catch (err) {
      toast.error(err.response?.data?.message || "Application already pending.");
    }
  };

  return (
    <Layout>
      <div className="mb-12 flex flex-col md:flex-row md:justify-between md:items-end gap-6">
        <div>
          <h1 className="text-4xl font-black mb-2">Live <span className="text-gradient">Markets</span></h1>
          <p className="text-gray-400 text-lg">Discover and bid on exclusive, verified digital assets.</p>
        </div>
        
        {!user?.roles?.includes('SELLER') && !user?.roles?.includes('ADMIN') && (
          <button onClick={handleApplySeller} className="btn-outline border-cyan-500/50 text-cyan-300 hover:bg-cyan-500/10 shadow-[0_0_15px_rgba(6,182,212,0.15)]">
            ✨ Become a Verified Seller
          </button>
        )}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-8">
        {auctions.map((auction) => (
          <article 
            key={auction.id} 
            className="glass-card cursor-pointer group"
            onClick={() => navigate(`/auction/${auction.id}`)}
          >
            {/* Aspect-Ratio Locked Image */}
            <div className="relative aspect-[4/3] bg-black/50 overflow-hidden border-b border-white/5">
              {auction.imageData ? (
                 <img src={auction.imageData} alt={auction.title} className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110" />
              ) : (
                 <div className="w-full h-full flex items-center justify-center text-5xl opacity-30">📦</div>
              )}
              
              {/* Neon Glow Overlay on Image */}
              <div className="absolute inset-0 bg-gradient-to-t from-dark to-transparent opacity-60"></div>
              
              {/* Status Badge */}
              <div className={`absolute top-4 right-4 px-4 py-1.5 text-xs font-black rounded-full backdrop-blur-md uppercase tracking-wider ${
                  auction.status === 'RUNNING' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/50 shadow-[0_0_10px_rgba(16,185,129,0.3)]' : 
                  (auction.status === 'ENDED' ? 'bg-rose-500/20 text-rose-400 border border-rose-500/50' : 'bg-amber-500/20 text-amber-400 border border-amber-500/50')
                }`}>
                {auction.status}
              </div>
            </div>

            <div className="p-6 flex flex-col flex-1">
              <h2 className="text-xl font-bold text-white mb-6 line-clamp-2 leading-tight group-hover:text-neonPurple transition-colors">{auction.title}</h2>
              
              <div className="mt-auto flex justify-between items-end mb-6">
                <div>
                  <p className="text-xs text-gray-400 uppercase tracking-widest font-bold mb-2">Current Bid</p>
                  <p className="text-3xl font-black text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 to-cyan-400">
                    ${(auction.highestBidAmount || auction.startingPrice).toLocaleString()}
                  </p>
                </div>
              </div>

              <div className="mb-6 py-3 px-4 rounded-xl bg-black/30 border border-white/5 flex items-center justify-center">
                <CountdownTimer startTime={auction.startTime} endTime={auction.endTime} status={auction.status} />
              </div>

              {auction.status !== 'ENDED' ? (
                 <button className="w-full btn-primary group-hover:animate-pulse">
                   ENTER ROOM ⚡
                 </button>
              ) : (
                 <button disabled className="w-full bg-white/5 text-gray-500 px-4 py-3 rounded-xl font-bold cursor-not-allowed border border-white/5">
                   AUCTION CLOSED
                 </button>
              )}
            </div>
          </article>
        ))}
      </div>
    </Layout>
  );
}