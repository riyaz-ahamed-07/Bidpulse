import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { toast } from 'react-toastify';
import apiClient from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import CountdownTimer from '../components/CountdownTimer';
import Layout from '../components/Layout';

export default function AuctionRoomPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth(); 
  const userRef = useRef(user);
  
  useEffect(() => { userRef.current = user; }, [user]); 

  const [auction, setAuction] = useState(null);
  const [currentBid, setCurrentBid] = useState(0);
  const [bidAmount, setBidAmount] = useState('');
  const [logs, setLogs] = useState([]);
  const [liveUsers, setLiveUsers] = useState(1); 
  const [isBidding, setIsBidding] = useState(false);

  useEffect(() => {
    apiClient.get(`/auctions/${id}`)
      .then(res => {
        setAuction(res.data);
        setCurrentBid(res.data.highestBidAmount || res.data.startingPrice);
      })
      .catch(err => {
        toast.error("Asset not found. Routing back to dashboard.");
        navigate('/dashboard');
      });

    const socket = new SockJS(`${import.meta.env.VITE_API_BASE_URL}/ws`);
    const stompClient = Stomp.over(socket);
    stompClient.debug = () => {}; 

    stompClient.connect({}, () => {
      setLogs(prev => [`[SYSTEM] Uplink established to Asset #${id}`, ...prev]);
      
      stompClient.subscribe(`/topic/auction.${id}`, (message) => {
        const payload = JSON.parse(message.body);
        const newBidAmount = payload.highestBidAmount || payload.amount;
        const winningBidderId = payload.highestBidderId || payload.bidderId;

        if (newBidAmount) {
          setCurrentBid(newBidAmount);
          setLogs(prev => [`[RADAR] Incoming bid detected: $${newBidAmount.toLocaleString()}`, ...prev]);

          const currentUser = userRef.current;
          
          if (currentUser && String(winningBidderId) !== String(currentUser.id)) {
            toast.error(`⚠️ WARNING: You've been outbid! New high: $${newBidAmount}`);
          } else if (currentUser && String(winningBidderId) === String(currentUser.id)) {
            toast.success(`🎯 CONFIRMED: You hold the highest bid!`);
          }
        }
      });
    });

    return () => {
      if (stompClient.connected) stompClient.disconnect();
    };
  }, [id, navigate]); 

  const handlePlaceBid = async (e) => {
    e.preventDefault();
    setIsBidding(true);
    try {
      await apiClient.post(`/auctions/${id}/bids`, { amount: Number(bidAmount) });
      setBidAmount('');
    } catch (err) {
      toast.error(err.response?.data?.message || "Bid rejected. Insufficient funds or bid too low.");
    } finally {
      setIsBidding(false);
    }
  };

  if (!auction) return (
    <Layout>
      <div className="flex items-center justify-center h-[60vh]">
        <h2 className="text-2xl font-bold text-neonCyan animate-pulse">Establishing secure connection...</h2>
      </div>
    </Layout>
  );

  return (
    <Layout>
      {/* Header Area */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <button onClick={() => navigate('/dashboard')} className="btn-outline w-max border-white/10 text-gray-400 hover:text-white px-4 py-2 text-sm">
          ← ABORT & RETURN
        </button>
        
        <div className="flex items-center gap-3 bg-emerald-500/10 border border-emerald-500/20 px-4 py-2 rounded-full shadow-[0_0_15px_rgba(16,185,129,0.1)]">
          <span className="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse shadow-[0_0_8px_#34d399]"></span>
          <span className="text-emerald-400 font-bold text-sm tracking-wider uppercase">{liveUsers} Operatives Watching</span>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* LEFT COLUMN: Asset Information */}
        <div className="lg:col-span-2 space-y-6">
          <div className="glass-card overflow-hidden">
            <div className="relative aspect-[16/9] bg-black/50 border-b border-white/5">
              {auction.imageData ? (
                 <img src={auction.imageData} alt={auction.title} className="w-full h-full object-contain p-4 drop-shadow-[0_0_20px_rgba(255,255,255,0.1)]" />
              ) : (
                 <div className="w-full h-full flex items-center justify-center text-6xl opacity-30">📦</div>
              )}
              <div className="absolute top-4 left-4 bg-black/60 backdrop-blur-md border border-white/10 px-4 py-2 rounded-xl">
                <CountdownTimer startTime={auction.startTime} endTime={auction.endTime} status={auction.status} />
              </div>
            </div>
            
            <div className="p-8">
              <h1 className="text-3xl font-black text-white mb-4">{auction.title}</h1>
              <p className="text-gray-400 leading-relaxed text-lg">{auction.description}</p>
              
              <div className="mt-8 flex gap-4 text-sm font-mono text-gray-500 bg-black/30 p-4 rounded-xl border border-white/5">
                <p>ASSET ID: <span className="text-neonCyan">#{auction.id}</span></p>
                <p>SELLER ID: <span className="text-neonPurple">#{auction.sellerId}</span></p>
              </div>
            </div>
          </div>
        </div>

        {/* RIGHT COLUMN: Trading Terminal */}
        <div className="space-y-6">
          
          {/* Bid Controls */}
          <div className="glass-card p-6 border-t-4 border-t-emerald-500 relative overflow-hidden">
            <div className="absolute -right-10 -top-10 w-32 h-32 bg-emerald-500/20 rounded-full blur-[40px] pointer-events-none"></div>
            
            <p className="text-emerald-400 text-xs font-bold uppercase tracking-[0.2em] mb-2">Live Highest Bid</p>
            <h2 className="text-5xl font-black text-white mb-8 drop-shadow-[0_0_15px_rgba(16,185,129,0.3)]">
              <span className="text-emerald-500 mr-1">$</span>{currentBid.toLocaleString()}
            </h2>

            {auction.status !== 'ENDED' ? (
              <form onSubmit={handlePlaceBid} className="space-y-4">
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <span className="text-emerald-400 font-bold">$</span>
                  </div>
                  <input 
                    type="number" 
                    value={bidAmount} 
                    onChange={(e) => setBidAmount(e.target.value)} 
                    placeholder={`> ${currentBid}`} 
                    required 
                    min={currentBid + (auction.minIncrement || 1)}
                    className="input-field pl-8 font-mono text-xl py-4 bg-black/60 focus:border-emerald-500 focus:ring-emerald-500"
                  />
                </div>
                <button type="submit" className="w-full btn-success py-4 text-lg" disabled={isBidding}>
                  {isBidding ? 'TRANSMITTING...' : 'DROP BID ⚡'}
                </button>
                <p className="text-center text-xs text-gray-500 mt-2 font-mono">Min increment: ${auction.minIncrement?.toFixed(2) || '1.00'}</p>
              </form>
            ) : (
              <div className="bg-rose-500/20 border border-rose-500/30 rounded-xl p-4 text-center">
                <p className="text-rose-400 font-bold">AUCTION TERMINATED</p>
              </div>
            )}
          </div>

          {/* Live Radar Log */}
          <div className="glass-card flex flex-col h-64 border-t-4 border-t-neonCyan">
            <div className="p-3 border-b border-white/5 bg-black/40 flex items-center justify-between">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-widest flex items-center gap-2">
                <span className="w-2 h-2 bg-neonCyan rounded-full animate-pulse"></span>
                Live Radar Log
              </h3>
            </div>
            
            <div className="flex-1 overflow-y-auto p-4 space-y-2 font-mono text-sm flex flex-col-reverse">
              {logs.map((log, index) => (
                <div 
                  key={index} 
                  className={`py-1 border-b border-white/5 last:border-0 ${index === 0 ? 'text-neonCyan font-bold drop-shadow-[0_0_5px_rgba(6,182,212,0.5)]' : 'text-gray-500'}`}
                  style={{ opacity: Math.max(0.2, 1 - (index * 0.15)) }}
                >
                  {log}
                </div>
              ))}
              {logs.length === 0 && <p className="text-gray-600 italic">Waiting for signals...</p>}
            </div>
          </div>

        </div>
      </div>
    </Layout>
  );
}