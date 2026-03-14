import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import apiClient from '../api/axiosConfig';
import Layout from '../components/Layout';

export default function SellerDashboard() {
  const navigate = useNavigate();
  
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [startingPrice, setStartingPrice] = useState('');
  const [minIncrement, setMinIncrement] = useState('1'); // NEW: Minimum Bidding Amount!
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [imageData, setImageData] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => setImageData(reader.result);
      reader.readAsDataURL(file);
    }
  };

  const handleCreateAuction = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      const payload = {
        title,
        description,
        startingPrice: Number(startingPrice),
        minIncrement: Number(minIncrement), // Now tied to the user's input!
        imageData,
        startTime: new Date(startTime).toISOString(),
        endTime: new Date(endTime).toISOString(),
      };

      const res = await apiClient.post('/auctions', payload);
      toast.success(`🚀 Auction "${res.data.title}" is now LIVE!`);
      
      setTitle(''); setDescription(''); setStartingPrice(''); setMinIncrement('1');
      setStartTime(''); setEndTime(''); setImageData('');
      
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to launch auction. Check your dates!");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Layout>
      <div className="max-w-3xl mx-auto space-y-8 animate-float" style={{ animationDuration: '10s' }}>
        <div>
          <h1 className="text-4xl font-black mb-2">Seller <span className="text-gradient">Command Center</span></h1>
          <p className="text-gray-400 text-lg">Mint and list your premium assets on the marketplace.</p>
        </div>

        <div className="glass-card p-8 border-t-4 border-t-neonPurple">
          <form onSubmit={handleCreateAuction} className="space-y-6">
            
            {/* Title & Description */}
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Asset Title</label>
                <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="e.g., Mint Condition Charizard 1st Edition" required className="input-field text-lg" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Lore / Description</label>
                <textarea value={description} onChange={(e) => setDescription(e.target.value)} placeholder="Tell the story of this asset..." required className="input-field h-24 resize-none" />
              </div>
            </div>

            {/* Image Upload */}
            <div>
              <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Holographic Preview (Image)</label>
              <div className="relative border-2 border-dashed border-white/20 rounded-xl p-8 text-center hover:border-neonPurple/50 transition-colors bg-black/20">
                <input type="file" accept="image/*" onChange={handleFileChange} className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-10" />
                {imageData ? (
                  <div className="flex flex-col items-center gap-4">
                    <img src={imageData} alt="Preview" className="h-32 object-contain rounded-lg shadow-[0_0_15px_rgba(139,92,246,0.3)]" />
                    <p className="text-neonPurple font-bold text-sm">Image Locked 🔒 (Click to change)</p>
                  </div>
                ) : (
                  <div>
                    <span className="text-4xl mb-2 block">📸</span>
                    <p className="text-gray-400 font-medium">Drag & drop or click to upload</p>
                  </div>
                )}
              </div>
            </div>

            {/* Pricing Matrix */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6 rounded-xl bg-black/30 border border-white/5">
              <div>
                <label className="block text-xs font-bold text-neonCyan uppercase tracking-widest mb-2">Starting Bid ($)</label>
                <input type="number" value={startingPrice} onChange={(e) => setStartingPrice(e.target.value)} placeholder="0.00" min="1" required className="input-field font-mono text-xl text-neonCyan" />
              </div>
              <div>
                <label className="block text-xs font-bold text-neonPink uppercase tracking-widest mb-2">Min. Increment ($)</label>
                <input type="number" value={minIncrement} onChange={(e) => setMinIncrement(e.target.value)} placeholder="1.00" min="0.5" step="0.5" required className="input-field font-mono text-xl text-neonPink" />
              </div>
            </div>

            {/* Timeline */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Ignition Time (Start)</label>
                <input type="datetime-local" value={startTime} onChange={(e) => setStartTime(e.target.value)} required className="input-field" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">Terminal Time (End)</label>
                <input type="datetime-local" value={endTime} onChange={(e) => setEndTime(e.target.value)} required className="input-field" />
              </div>
            </div>

            <button type="submit" className="w-full btn-primary py-4 text-lg mt-4" disabled={isSubmitting}>
              {isSubmitting ? 'INITIALIZING LAUNCH...' : '🚀 DEPLOY TO MARKETPLACE'}
            </button>
          </form>
        </div>
      </div>
    </Layout>
  );
}