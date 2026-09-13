import { useEffect, useMemo, useState } from 'react';
import { ClusterManager } from './simulation/ClusterManager';
import type { NodeState } from './simulation/types';
import { ClusterView } from './components/ClusterView';
import { Play, RotateCcw, Loader2 } from 'lucide-react';
import { Button } from '@repo/ui';

export default function RaftVisualizer() {
  const clusterSize = 5;
  const [resetKey, setResetKey] = useState(0);
  const cluster = useMemo(() => new ClusterManager(clusterSize), [resetKey]);
  
  const [nodeStates, setNodeStates] = useState<Record<string, NodeState>>({});
  const [nodeTerms, setNodeTerms] = useState<Record<string, number>>({});
  const [nodeTimers, setNodeTimers] = useState<Record<string, { duration: number; key: number }>>({});
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
    cluster.nodes.forEach(node => node.kill());
    setNodeStates({});
    setNodeTerms({});
    setNodeTimers({});
    setIsRunning(false);
    setResetKey(k => k + 1);
  };

  return (
    <div className="h-full w-full bg-white text-stone-900 flex flex-col lg:flex-row font-sans selection:bg-orange-200 selection:text-orange-900 overflow-y-auto lg:overflow-hidden">
      
      {/* Left Column: Text & Controls (35%) */}
      <div className="w-full lg:w-[38%] bg-[#fdfcfb] border-b-2 lg:border-b-0 lg:border-r-2 border-stone-900 p-6 sm:p-10 flex flex-col justify-between relative z-20 overflow-y-auto shrink-0">
        <div>
          <span className="inline-block px-2.5 py-0.5 font-mono text-[10px] font-black uppercase tracking-wider bg-orange-500 text-white border border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] mb-4">
            Consensus State Machine
          </span>
          <h2 className="text-2xl sm:text-3xl font-silkscreen not-italic text-stone-900 mb-4 tracking-tight leading-tight">
            Raft Consensus
          </h2>
          <p className="text-stone-600 text-sm sm:text-base leading-relaxed font-medium mb-8">
            Watch distributed leader election in real-time. Nodes timeout asynchronously to become candidates. Click on any node to kill or revive it and observe fault-tolerant cluster failover!
          </p>

          <div className="flex flex-wrap gap-3 mb-8">
            <Button onClick={handleStart} variant="primary" disabled={isRunning}>
              {isRunning ? <Loader2 size={16} className="animate-spin" /> : <Play size={16} />}
              {isRunning ? 'Simulation Running' : 'Start Simulation'}
            </Button>
            <Button onClick={handleReset} variant="secondary">
              <RotateCcw size={16} /> Reset
            </Button>
          </div>
        </div>

        {/* Legends */}
        <div className="flex flex-col sm:flex-row gap-6 mt-6 border-t-2 border-dashed border-stone-300 pt-6">
          <div className="flex flex-col gap-2">
            <h3 className="text-[10px] font-mono font-bold text-stone-400 uppercase tracking-widest mb-1">Node State</h3>
            <div className="flex items-center gap-2 text-xs font-bold font-mono"><div className="w-3.5 h-3.5 bg-emerald-400 border-2 border-stone-900"></div> Leader</div>
            <div className="flex items-center gap-2 text-xs font-bold font-mono"><div className="w-3.5 h-3.5 bg-orange-500 border-2 border-stone-900"></div> Candidate</div>
            <div className="flex items-center gap-2 text-xs font-bold font-mono"><div className="w-3.5 h-3.5 bg-white border-2 border-stone-900"></div> Follower</div>
            <div className="flex items-center gap-2 text-xs font-bold font-mono"><div className="w-3.5 h-3.5 bg-stone-800 border-2 border-stone-900"></div> Dead</div>
          </div>

          <div className="flex flex-col gap-2">
            <h3 className="text-[10px] font-mono font-bold text-stone-400 uppercase tracking-widest mb-1">Network Packets</h3>
            <div className="flex items-center gap-2 text-xs font-bold font-mono"><div className="w-3.5 h-3.5 bg-orange-500 border-2 border-stone-900"></div> Request Vote</div>
            <div className="flex items-center gap-2 text-xs font-bold font-mono"><div className="w-3.5 h-3.5 bg-emerald-400 border-2 border-stone-900"></div> Vote Granted</div>
            <div className="flex items-center gap-2 text-xs font-bold font-mono"><div className="w-3.5 h-3.5 bg-rose-500 border-2 border-stone-900"></div> Vote Denied</div>
            <div className="flex items-center gap-2 text-xs font-bold font-mono"><div className="w-3.5 h-3.5 bg-stone-100 border-2 border-stone-900"></div> Heartbeat</div>
          </div>
        </div>
      </div>

      {/* Right Column: The Simulation Canvas (62%) */}
      <div className="flex-1 w-full h-full relative flex justify-center items-center bg-[#fdfcfb] z-10 overflow-hidden min-h-[480px]">
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
  );
}
