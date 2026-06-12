import { useState, useRef, useEffect } from 'react';
import { ConsistentHashing, type StorageNode } from './core/ConsistentHashing';
import { HashRing, type ActiveMessage } from './components/HashRing';
import { Plus, Play, Square, Trash2 } from 'lucide-react';
import { Button } from '@repo/ui';

export default function App() {
  const RING_SIZE = 16;
  const ringRef = useRef(new ConsistentHashing([], RING_SIZE));
  const ring = ringRef.current;
  const msgCounterRef = useRef(1);

  // State
  const [nodes, setNodes] = useState<StorageNode[]>([]);
  const [nodeCounter, setNodeCounter] = useState(0);
  
  const [simulating, setSimulating] = useState(false);
  const [activeMessages, setActiveMessages] = useState<ActiveMessage[]>([]);
  const [logs, setLogs] = useState<{message: string, type: 'info'|'success'|'error'}[]>([]);

  const addLog = (msg: string, type: 'info'|'success'|'error') => {
    setLogs(prev => [{message: msg, type}, ...prev].slice(0, 8));
  };

  const handleAddNode = () => {
    try {
      const letter = String.fromCharCode(65 + (nodeCounter % 26));
      const suffix = nodeCounter >= 26 ? `${Math.floor(nodeCounter / 26)}` : '';
      const nodeName = `Node ${letter}${suffix}`;
      
      const ip = `10.0.0.${nodeCounter + 1}`;
      const addedNode = ring.addNode({ name: nodeName, ip });
      
      setNodes([...ring.nodes]);
      setNodeCounter(prev => prev + 1);
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
    <div className="w-full h-screen overflow-hidden bg-[#fdfcfb] text-stone-800 font-sans selection:bg-orange-100 selection:text-orange-900 flex">
      
      {/* Left Column (40%) */}
      <div className="w-[40%] h-full flex flex-col p-12 border-r-2 border-stone-900 bg-[#fdfcfb] overflow-y-auto">
        <h1 className="text-5xl font-serif italic text-stone-900 mb-6 tracking-tight leading-tight">
          Consistent Hashing
        </h1>
        <p className="text-stone-500 text-lg leading-relaxed font-light mb-12">
          A distributed hashing scheme that operates independently of the number of servers or objects. Perfect for scaling caches or databases.
        </p>

        {/* Controls */}
        <div className="flex flex-col gap-6 mb-12 pb-12 border-b-2 border-stone-200">
          <div className="flex gap-4">
            <Button 
              type="button" 
              onClick={handleAddNode} 
              variant="default"
              className="flex-1"
            >
              <Plus size={18} /> Add Node
            </Button>
            <Button 
              type="button" 
              onClick={toggleSimulation} 
              variant="primary"
              disabled={nodes.length === 0}
              className={`flex-1 ${simulating ? '!bg-rose-500 hover:!bg-rose-600' : ''}`}
            >
              {simulating ? <><Square size={18} /> Stop Sim</> : <><Play size={18} /> Start Sim</>}
            </Button>
          </div>

          <div className="flex flex-wrap gap-2 mt-4">
            {nodes.map(node => (
              <div key={node.ip} className="flex items-center gap-2 px-3 py-1 bg-stone-100 border border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] rounded-none">
                <span className="font-mono text-sm font-bold">{node.name}</span>
                <button onClick={() => handleRemoveNode(node)} className="text-rose-600 hover:text-rose-800 cursor-pointer ml-2">
                  <Trash2 size={14} />
                </button>
              </div>
            ))}
            {nodes.length === 0 && <span className="text-sm text-stone-400 font-mono italic">No nodes in ring</span>}
          </div>
        </div>

        {/* Brutalist Event Log */}
        <div className="w-full flex flex-col gap-2">
           <h3 className="font-mono text-xs text-stone-400 uppercase tracking-widest mb-2">Event Log</h3>
           {logs.map((log, i) => (
              <div key={i} className={`p-3 border-l-4 rounded-none text-sm font-mono ${
                log.type === 'success' ? 'bg-emerald-50 border-emerald-500 text-emerald-900' :
                log.type === 'error' ? 'bg-rose-50 border-rose-500 text-rose-900' :
                'bg-stone-100 border-stone-900 text-stone-900'
              }`}>
                {log.message}
              </div>
           ))}
        </div>
      </div>

      {/* Right Column (60%) */}
      <div className="w-[60%] h-full relative flex justify-center items-center bg-[#fdfcfb]">
        <div className="absolute top-8 right-8 text-right font-mono text-sm text-stone-400">
          <p>Ring Size: {RING_SIZE}</p>
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
