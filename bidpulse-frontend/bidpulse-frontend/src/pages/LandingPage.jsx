import { useNavigate, Link } from 'react-router-dom';

export default function LandingPage() {
    const navigate = useNavigate();

    return (
        <div className="min-h-screen relative overflow-hidden bg-black text-white selection:bg-neonCyan/30">
            {/* Ambient Background Elements */}
            <div className="absolute top-0 left-1/2 -translate-x-1/2 w-full h-full pointer-events-none">
                <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-neonPurple/20 rounded-full blur-[120px] animate-pulse"></div>
                <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-neonCyan/20 rounded-full blur-[120px] animate-pulse"></div>
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-px h-full bg-gradient-to-b from-transparent via-white/10 to-transparent"></div>
            </div>

            {/* Navigation */}
            <nav className="relative z-50 flex items-center justify-between px-6 py-8 max-w-7xl mx-auto">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gradient-to-br from-cyan-400 to-blue-500 rounded-xl flex items-center justify-center text-xl font-black shadow-[0_0_20px_rgba(6,182,212,0.4)]">
                        B
                    </div>
                    <span className="text-2xl font-black tracking-tighter uppercase">BidPulse</span>
                </div>
                <div className="flex items-center gap-6">
                    <Link to="/login" className="text-sm font-bold text-gray-400 hover:text-white transition-colors uppercase tracking-widest">Login</Link>
                    <button 
                        onClick={() => navigate('/register')}
                        className="btn-primary py-2 px-6 text-sm"
                    >
                        GET AUTHORIZED
                    </button>
                </div>
            </nav>

            {/* Hero Section */}
            <main className="relative z-10 max-w-7xl mx-auto px-6 pt-20 pb-32 flex flex-col items-center text-center">
                <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/5 border border-white/10 mb-8 backdrop-blur-md">
                    <span className="relative flex h-2 w-2">
                        <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-neonCyan opacity-75"></span>
                        <span className="relative inline-flex rounded-full h-2 w-2 bg-neonCyan"></span>
                    </span>
                    <span className="text-[10px] font-black uppercase tracking-[0.2em] text-cyan-400">Live Infrastructure Online</span>
                </div>

                <h1 className="text-6xl md:text-8xl font-black tracking-tighter mb-8 leading-[0.9]">
                    THE FUTURE OF <br />
                    <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 via-blue-500 to-purple-600">
                        ELITE BIDDING
                    </span>
                </h1>

                <p className="max-w-2xl text-gray-400 text-lg md:text-xl leading-relaxed mb-12 font-medium">
                    Access the world's most exclusive digital marketplace. Real-time updates, 
                    secure transactions, and high-stakes auctions powered by next-gen infrastructure.
                </p>

                <div className="flex flex-col sm:flex-row gap-4 items-center justify-center">
                    <button 
                        onClick={() => navigate('/register')}
                        className="btn-primary text-lg px-12 py-5 group"
                    >
                        START INITIALIZATION
                        <span className="inline-block ml-2 transition-transform group-hover:translate-x-1">→</span>
                    </button>
                    <button 
                        onClick={() => navigate('/login')}
                        className="btn-outline border-white/10 hover:border-white/20 text-white text-lg px-12 py-5"
                    >
                        ACCESS TERMINAL
                    </button>
                </div>

                {/* Grid Preview / Stats */}
                <div className="mt-32 grid grid-cols-1 md:grid-cols-3 gap-8 w-full max-w-5xl">
                    <div className="glass-card p-8 border-t-2 border-t-neonCyan">
                        <div className="text-3xl font-black text-white mb-2">99.9%</div>
                        <div className="text-xs font-bold text-gray-500 uppercase tracking-widest">Uptime Guaranteed</div>
                    </div>
                    <div className="glass-card p-8 border-t-2 border-t-neonPurple">
                        <div className="text-3xl font-black text-white mb-2">$2.4M+</div>
                        <div className="text-xs font-bold text-gray-500 uppercase tracking-widest">Total Trade Vol</div>
                    </div>
                    <div className="glass-card p-8 border-t-2 border-t-emerald-500">
                        <div className="text-3xl font-black text-white mb-2">15ms</div>
                        <div className="text-xs font-bold text-gray-500 uppercase tracking-widest">Latency Threshold</div>
                    </div>
                </div>
            </main>

            {/* Footer Accent */}
            <div className="absolute bottom-0 left-0 w-full h-px bg-gradient-to-r from-transparent via-white/10 to-transparent"></div>
        </div>
    );
}
