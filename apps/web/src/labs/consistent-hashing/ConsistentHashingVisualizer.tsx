import { useState, useRef, useEffect } from 'react';
import { ConsistentHashing, type StorageNode } from './core/ConsistentHashing';
import { HashRing, type ActiveMessage } from './components/HashRing';
import { Plus, Play, Square, Trash2 } from 'lucide-react';
import { Button } from '@repo/ui';

export default function ConsistentHashingVisualizer() {
  const RING_SIZE = 16;
  const ringRef = useRef(new ConsistentHashing([], RING_SIZE));
  const ring = ringRef.current;
  const msgCounterRef = useRef(1);

  // State
  const [nodes, setNodes] = useState<StorageNode[]>([]);
  const nodeCounterRef = useRef(0);
  
  const [simulating, setSimulating] = useState(false);
  const [activeMessages, setActiveMessages] = useState<ActiveMessage[]>([]);
  const [logs, setLogs] = useState<{ message: string; type: 'info' | 'success' | 'error' }[]>([]);

  const addLog = (msg: string, type: 'info' | 'success' | 'error') => {
    setLogs(prev => [{ message: msg, type }, ...prev].slice(0, 8));
  };

  const handleAddNode = () => {
    try {
      const currentCount = nodeCounterRef.current++;
      const letter = String.fromCharCode(65 + (currentCount % 26));
      const suffix = currentCount >= 26 ? `${Math.floor(currentCount / 26)}` : '';
      const nodeName = `Node ${letter}${suffix}`;
      
      const ip = `10.0.0.${currentCount + 1}`;
      const addedNode = ring.addNode({ name: nodeName, ip });
      
      setNodes([...ring.nodes]);
      addLog(`Added Node: ${addedNode.name} (Hash: ${addedNode.hash})`, 'success');
    } catch (err: any) {
      addLog(err.message, 'error');
    }
  };

  const handleRemoveNode = (node: StorageNode) => {
    ring.removeNode(node);
    setNodes([...ring.nodes]);
    addLog(`Removed Node: ${node.name} (Hash: ${node.hash})`, 'info');
    
    if (ring.nodes.length === 0) {
      setSimulating(false);
    }
  };

  const toggleSimulation = () => {
    if (nodes.length === 0) return;
    setSimulating(prev => !prev);
  };

  // Simulation Loop
  useEffect(() => {
    if (!simulating) return;

    const interval = setInterval(() => {
      if (ring.nodes.length === 0) {
        setSimulating(false);
        return;
      }

      const msgId = msgCounterRef.current++;
      const dataKey = `msg_${msgId}`;
      const result = ring.getTargetNode(dataKey);

      if (result) {
        const newMsg: ActiveMessage = {
          id: dataKey,
          key: dataKey,
          hash: result.keyHash,
          targetNodeHash: result.node.hash,
          createdAt: Date.now()
        };

        setActiveMessages(prev => [...prev, newMsg]);
        addLog(`Routed '${dataKey}' (h:${result.keyHash}) -> ${result.node.name}`, 'info');
      }
    }, 1500);

    return () => clearInterval(interval);
  }, [simulating, ring]);

  // Cleanup old messages
  useEffect(() => {
    const cleanup = setInterval(() => {
      const now = Date.now();
      setActiveMessages(prev => prev.filter(msg => now - msg.createdAt < 3000));
    }, 500);
    return () => clearInterval(cleanup);
  }, []);

  return (
    <div className="w-full h-full bg-[#fdfcfb] text-stone-800 font-sans selection:bg-orange-100 selection:text-orange-900 flex flex-col lg:flex-row overflow-y-auto lg:overflow-hidden">
      
      {/* Left Column (38%) */}
      <div className="w-full lg:w-[38%] h-full flex flex-col p-6 sm:p-10 border-b-2 lg:border-b-0 lg:border-r-2 border-stone-900 bg-[#fdfcfb] overflow-y-auto shrink-0">
        <span className="inline-block px-2.5 py-0.5 font-mono text-[10px] font-black uppercase tracking-wider bg-blue-600 text-white border border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] mb-4 w-fit">
          Hash Ring Topologies
        </span>
        <h2 className="text-2xl sm:text-3xl font-silkscreen not-italic text-stone-900 mb-4 tracking-tight leading-tight">
          Consistent Hashing
        </h2>
        <p className="text-stone-600 text-sm sm:text-base leading-relaxed font-medium mb-8">
          A distributed hashing scheme that operates independently of the number of servers or objects. Essential for horizontal scale, caching layers, and minimal data migration.
        </p>

        {/* Controls */}
        <div className="flex flex-col gap-4 mb-8 pb-6 border-b-2 border-dashed border-stone-300">
          <div className="flex flex-wrap gap-3">
            <Button 
              type="button" 
              onClick={handleAddNode} 
              variant="default"
              className="flex-1"
            >
              <Plus size={16} /> Add Node
            </Button>
            <Button 
              type="button" 
              onClick={toggleSimulation} 
              variant="primary"
              disabled={nodes.length === 0}
              className={`flex-1 ${simulating ? '!bg-rose-500 hover:!bg-rose-600' : ''}`}
            >
              {simulating ? <><Square size={16} /> Stop Sim</> : <><Play size={16} /> Start Sim</>}
            </Button>
          </div>

          <div className="flex flex-wrap gap-2 mt-2">
            {nodes.map(node => (
              <div key={node.ip} className="flex items-center gap-2 px-2.5 py-1 bg-white border-2 border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] rounded-none">
                <span className="font-mono text-xs font-bold">{node.name}</span>
                <button onClick={() => handleRemoveNode(node)} className="text-rose-600 hover:text-rose-800 cursor-pointer ml-1">
                  <Trash2 size={13} />
                </button>
              </div>
            ))}
            {nodes.length === 0 && <span className="text-xs text-stone-400 font-mono italic">No nodes in ring. Click &quot;Add Node&quot; to begin.</span>}
          </div>
        </div>

        {/* Brutalist Event Log */}
        <div className="w-full flex flex-col gap-2">
           <h3 className="font-mono text-[10px] font-bold text-stone-400 uppercase tracking-widest mb-1">Live Event Log</h3>
           {logs.map((log, i) => (
              <div key={i} className={`p-2.5 border-l-4 rounded-none text-xs font-mono border-2 border-stone-900 ${
                log.type === 'success' ? 'bg-emerald-50 border-l-emerald-500 text-emerald-950' :
                log.type === 'error' ? 'bg-rose-50 border-l-rose-500 text-rose-950' :
                'bg-white border-l-stone-900 text-stone-900'
              }`}>
                {log.message}
              </div>
           ))}
        </div>
      </div>

      {/* Right Column (62%) */}
      <div className="flex-1 w-full h-full relative flex justify-center items-center bg-[#fdfcfb] min-h-[480px]">
        <div className="absolute top-6 right-6 text-right font-mono text-xs text-stone-500 font-bold bg-white px-3 py-1 border-2 border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]">
          <p>Ring Slots: {RING_SIZE}</p>
          <p>Active Nodes: {nodes.length}</p>
        </div>
        
        <HashRing 
          ringSize={RING_SIZE} 
          nodes={nodes} 
          activeMessages={activeMessages}
        />
      </div>
      
    </div>
  );
}
