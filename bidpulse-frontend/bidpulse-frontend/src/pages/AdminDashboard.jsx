import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';
import apiClient from '../api/axiosConfig';
import Layout from '../components/Layout';

export default function AdminDashboard() {
  const { user } = useAuth();
  const [applications, setApplications] = useState([]);

  const fetchApplications = async () => {
    try {
      const res = await apiClient.get('/admin/applications');
      setApplications(res.data);
    } catch (err) {
      toast.error("Failed to sync with mainframe.");
    }
  };

  useEffect(() => { fetchApplications(); }, []);

  const handleApprove = async (applicationId) => {
    try {
      const res = await apiClient.post(`/admin/applications/${applicationId}/approve`);
      toast.success(res.data.message || "Seller credentials granted! 👑");
      fetchApplications();
    } catch (err) {
      toast.error("Override failed.");
    }
  };

  return (
    <Layout>
      <div className="max-w-6xl mx-auto space-y-8">
        <div>
          <h1 className="text-4xl font-black mb-2 text-rose-400 drop-shadow-[0_0_15px_rgba(244,63,94,0.4)]">God Mode</h1>
          <p className="text-gray-400 text-lg">Welcome to the terminal, Administrator {user?.name}.</p>
        </div>

        <div className="glass-card overflow-hidden border-t-4 border-t-rose-500">
          <div className="p-6 border-b border-white/5 bg-black/20">
            <h2 className="text-xl font-bold text-white flex items-center gap-3">
              📥 Pending Seller Clearances
              <span className="bg-rose-500/20 text-rose-400 px-3 py-1 rounded-full text-xs border border-rose-500/50">
                {applications.length} Requests
              </span>
            </h2>
          </div>
          
          {applications.length === 0 ? (
            <div className="p-12 text-center flex flex-col items-center justify-center">
              <span className="text-6xl mb-4 opacity-50">📭</span>
              <p className="text-gray-400 text-lg font-medium">The queue is empty. Inbox zero achieved.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="bg-black/40 text-xs uppercase tracking-widest text-gray-400">
                    <th className="px-6 py-4 font-bold">App ID</th>
                    <th className="px-6 py-4 font-bold">Operative</th>
                    <th className="px-6 py-4 font-bold">Intel (Reason)</th>
                    <th className="px-6 py-4 font-bold">Status</th>
                    <th className="px-6 py-4 font-bold text-right">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/5">
                  {applications.map((app) => (
                    <tr key={app.applicationId} className="hover:bg-white/5 transition-colors">
                      <td className="px-6 py-4 text-gray-500 font-mono">#{app.applicationId}</td>
                      <td className="px-6 py-4">
                        <strong className="text-white block">{app.userName}</strong>
                        <span className="text-xs text-neonCyan">{app.userEmail}</span>
                      </td>
                      <td className="px-6 py-4 text-gray-300 max-w-xs truncate" title={app.reason}>{app.reason}</td>
                      <td className="px-6 py-4">
                        <span className="px-3 py-1 rounded-full text-xs font-bold bg-amber-500/20 text-amber-400 border border-amber-500/30 shadow-[0_0_10px_rgba(245,158,11,0.2)]">
                          PENDING
                        </span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <button className="btn-success py-2 px-4 text-sm" onClick={() => handleApprove(app.applicationId)}>
                          AUTHORIZE
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}