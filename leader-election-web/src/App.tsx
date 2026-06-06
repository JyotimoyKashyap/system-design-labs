import { useEffect, useMemo, useState } from 'react';
import { ClusterManager } from './simulation/ClusterManager';
import type { NodeState } from './simulation/types';
import { ClusterView } from './components/ClusterView';
import { Play, RotateCcw } from 'lucide-react';

function App() {
  const clusterSize = 5;
  const cluster = useMemo(() => new ClusterManager(clusterSize), []);
  
  const [nodeStates, setNodeStates] = useState<Record<string, NodeState>>({});
  const [nodeTerms, setNodeTerms] = useState<Record<string, number>>({});
  const [nodeTimers, setNodeTimers] = useState<Record<string, {duration: number, key: number}>>({});
  const [isRunning, setIsRunning] = useState(false);

  useEffect(() => {
    cluster.onNodeStateChange = (id, state, term) => {
      setNodeStates(prev => ({ ...prev, [id]: state }));
      setNodeTerms(prev => ({ ...prev, [id]: term }));
    };
    
    cluster.onNodeTimerReset = (id, duration) => {
      setNodeTimers(prev => ({
        ...prev,
        [id]: { duration, key: (prev[id]?.key || 0) + 1 }
      }));
    };
  }, [cluster]);

  const handleStart = () => {
    cluster.start();
    setIsRunning(true);
  };

  const handleReset = () => {
    window.location.reload();
  };

  return (
    <div className="h-screen bg-[#fdfcfb] text-stone-800 flex items-center justify-center p-8 font-sans selection:bg-orange-100 selection:text-orange-900 overflow-hidden">
      
      <div className="max-w-[1600px] w-full flex flex-col lg:flex-row gap-12 lg:gap-24 items-center justify-between">
        
        {/* Left Column: Text & Controls */}
        <div className="flex-1 flex flex-col items-start text-left max-w-xl">
          <h1 className="text-5xl font-serif italic text-stone-900 mb-6 tracking-tight leading-tight">
            Raft Consensus Visualizer
          </h1>
          <p className="text-stone-500 text-lg leading-relaxed font-light mb-10">
            Watch leader election in real-time. Nodes timeout asynchronously to become candidates. Click on any node to kill/revive it and watch the cluster recover!
          </p>

          <div className="flex gap-4">
            {!isRunning && (
              <button onClick={handleStart} className="flex items-center gap-2 px-8 py-3 bg-stone-900 text-white hover:bg-stone-800 rounded-none font-medium transition-all hover:scale-105 active:scale-95">
                <Play size={18} /> Start Simulation
              </button>
            )}
            <button onClick={handleReset} className="flex items-center gap-2 px-8 py-3 bg-white text-stone-700 border border-stone-200 hover:bg-stone-50 hover:border-stone-300 rounded-none font-medium transition-all hover:scale-105 active:scale-95">
              <RotateCcw size={18} /> Reset
            </button>
          </div>
        </div>

        {/* Right Column: The Simulation */}
        <div className="flex-[1.5] relative flex justify-center items-center w-full">
          <div className="bg-white p-6 rounded-none border border-stone-200 relative overflow-hidden flex items-center justify-center w-[900px] h-[650px]">
            <ClusterView 
              cluster={cluster} 
              nodeStates={nodeStates} 
              nodeTerms={nodeTerms}
              nodeTimers={nodeTimers}
              onKill={(id: string) => cluster.killNode(id)}
              onRevive={(id: string) => cluster.reviveNode(id)}
            />
            
            {/* Legend inside simulation */}
            <div className="absolute bottom-6 left-6 flex flex-col gap-3 bg-white/90 p-4 rounded-none border border-stone-100 backdrop-blur-md">
              <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-0.5">Node State</h3>
              <div className="flex items-center gap-2 text-sm font-medium text-stone-600"><div className="w-2.5 h-2.5 rounded-none bg-emerald-100 border border-emerald-200"></div> Leader</div>
              <div className="flex items-center gap-2 text-sm font-medium text-stone-600"><div className="w-2.5 h-2.5 rounded-none bg-orange-100 border border-orange-200"></div> Candidate</div>
              <div className="flex items-center gap-2 text-sm font-medium text-stone-600"><div className="w-2.5 h-2.5 rounded-none bg-slate-100 border border-slate-200"></div> Follower</div>
              <div className="flex items-center gap-2 text-sm font-medium text-stone-600"><div className="w-2.5 h-2.5 rounded-none bg-rose-50 border border-rose-200"></div> Dead</div>
            </div>

            {/* Message Legend inside simulation */}
            <div className="absolute top-6 right-6 flex flex-col gap-3 bg-white/90 p-4 rounded-none border border-stone-100 backdrop-blur-md">
              <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-0.5">Network Packets</h3>
              <div className="flex items-center gap-2 text-sm font-medium text-stone-600"><div className="w-2 h-2 rounded-none bg-orange-400"></div> Request Vote</div>
              <div className="flex items-center gap-2 text-sm font-medium text-stone-600"><div className="w-2 h-2 rounded-none bg-emerald-400"></div> Vote Granted</div>
              <div className="flex items-center gap-2 text-sm font-medium text-stone-600"><div className="w-2 h-2 rounded-none bg-slate-300"></div> Heartbeat</div>
            </div>
          </div>
        </div>
      </div>

    </div>
  );
}

export default App;
