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
    <div className="h-screen bg-white text-stone-900 flex flex-col lg:flex-row font-sans selection:bg-orange-200 selection:text-orange-900 overflow-hidden">
      
      {/* Left Column: Text & Controls (40%) */}
      <div className="w-full lg:w-[40%] bg-[#fdfcfb] border-b-2 lg:border-b-0 lg:border-r-2 border-stone-900 p-8 lg:p-16 flex flex-col justify-center relative z-20">
        <h1 className="text-5xl lg:text-6xl font-serif italic text-stone-900 mb-8 tracking-tight leading-tight">
          Raft Consensus Visualizer
        </h1>
        <p className="text-stone-600 text-lg leading-relaxed font-medium mb-12">
          Watch leader election in real-time. Nodes timeout asynchronously to become candidates. Click on any node to kill/revive it and watch the cluster recover!
        </p>

        <div className="flex gap-4 mb-12">
          {!isRunning && (
            <button onClick={handleStart} className="flex items-center gap-2 px-8 py-4 bg-stone-900 text-white hover:bg-stone-800 border-2 border-stone-900 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] rounded-none font-bold transition-all hover:translate-y-[2px] hover:shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] active:translate-y-[4px] active:shadow-none">
              <Play size={18} /> Start Simulation
            </button>
          )}
          <button onClick={handleReset} className="flex items-center gap-2 px-8 py-4 bg-white text-stone-900 border-2 border-stone-900 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] hover:bg-stone-50 rounded-none font-bold transition-all hover:translate-y-[2px] hover:shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] active:translate-y-[4px] active:shadow-none">
            <RotateCcw size={18} /> Reset
          </button>
        </div>

        {/* Legends moved to the left panel to keep simulation clean */}
        <div className="flex flex-col xl:flex-row gap-12 mt-auto border-t-2 border-stone-900 pt-8">
            <div className="flex flex-col gap-3">
              <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-1">Node State</h3>
              <div className="flex items-center gap-3 text-sm font-bold"><div className="w-4 h-4 bg-emerald-400 border-2 border-stone-900"></div> Leader</div>
              <div className="flex items-center gap-3 text-sm font-bold"><div className="w-4 h-4 bg-orange-500 border-2 border-stone-900"></div> Candidate</div>
              <div className="flex items-center gap-3 text-sm font-bold"><div className="w-4 h-4 bg-white border-2 border-stone-900"></div> Follower</div>
              <div className="flex items-center gap-3 text-sm font-bold"><div className="w-4 h-4 bg-stone-800 border-2 border-stone-900"></div> Dead</div>
            </div>

            <div className="flex flex-col gap-3">
              <h3 className="text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-1">Network Packets</h3>
              <div className="flex items-center gap-3 text-sm font-bold"><div className="w-4 h-4 bg-orange-500 border-2 border-stone-900"></div> Request Vote</div>
              <div className="flex items-center gap-3 text-sm font-bold"><div className="w-4 h-4 bg-emerald-400 border-2 border-stone-900"></div> Vote Granted</div>
              <div className="flex items-center gap-3 text-sm font-bold"><div className="w-4 h-4 bg-rose-500 border-2 border-stone-900"></div> Vote Denied</div>
              <div className="flex items-center gap-3 text-sm font-bold"><div className="w-4 h-4 bg-stone-100 border-2 border-stone-900"></div> Heartbeat</div>
            </div>
        </div>
      </div>

      {/* Right Column: The Simulation (60%) */}
      <div className="w-full lg:w-[60%] relative flex justify-center items-center bg-white z-10 overflow-hidden">
        {/* Transparent wrapper to maintain coordinates */}
        <div className="w-[900px] h-[650px] relative">
          <ClusterView 
            cluster={cluster} 
            nodeStates={nodeStates} 
            nodeTerms={nodeTerms}
            nodeTimers={nodeTimers}
            onKill={(id: string) => cluster.killNode(id)}
            onRevive={(id: string) => cluster.reviveNode(id)}
          />
        </div>
      </div>
    </div>
  );
}

export default App;
