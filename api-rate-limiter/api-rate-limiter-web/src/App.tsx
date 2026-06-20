import { useState, useEffect, useRef } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Button } from "@repo/ui";
import { Play, Square } from "lucide-react";

// Predefined Clients
const CLIENTS = [
  { id: "User A", ip: "192.168.1.50", y: "20%" },
  { id: "User B", ip: "10.0.0.99", y: "50%" },
  { id: "User C", ip: "172.16.0.10", y: "80%" },
];

const SHARDS = [
  { id: "Shard 1", x: "40%" },
  { id: "Shard 2", x: "50%" },
  { id: "Shard 3", x: "60%" },
];

const getShardForIp = (ip: string) => {
  if (ip.includes("50")) return 0;
  if (ip.includes("99")) return 1;
  return 2;
};

export default function App() {
  const [isRunning, setIsRunning] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);
  const [tokens, setTokens] = useState<any[]>([]);
  const [activeShardIndex, setActiveShardIndex] = useState<number | null>(null);
  const [rateLimitedIps, setRateLimitedIps] = useState<Set<string>>(new Set());
  
  const rateLimits = useRef<Record<string, number>>({});
  const MAX_REQUESTS = 3;

  const fireEvent = (clientIndex: number) => {
    const client = CLIENTS[clientIndex];
    const shardIndex = getShardForIp(client.ip);
    
    const tokenId = `msg_${Date.now()}_${Math.random()}`;
    const currentCount = rateLimits.current[client.ip] || 0;
    const isAllowed = currentCount < MAX_REQUESTS;
    
    if (isAllowed) {
        rateLimits.current[client.ip] = currentCount + 1;
        // Check if they JUST hit the limit
        if (currentCount + 1 >= MAX_REQUESTS) {
            setRateLimitedIps(prev => new Set(prev).add(client.ip));
        }
    }

    setLogs(prev => [`[${client.id}] -> Shard ${shardIndex + 1} -> ${isAllowed ? 'ALLOWED' : 'BLOCKED'}`, ...prev].slice(0, 15));

    setTokens(prev => [...prev, { id: tokenId, clientIndex, shardIndex, isAllowed }]);

    // Briefly flash the queried shard to show Proxy -> Shard communication
    setTimeout(() => setActiveShardIndex(shardIndex), 800);
    setTimeout(() => setActiveShardIndex(null), 1200);

    setTimeout(() => {
      setTokens(prev => prev.filter(t => t.id !== tokenId));
    }, 3000);
  };

  // Window decay loop (resets token buckets every 10 seconds)
  useEffect(() => {
    const decayInterval = setInterval(() => {
        rateLimits.current = {};
        setRateLimitedIps(new Set()); // Reset visual limit states
        setLogs(prev => [`[SYSTEM] Sliding window decayed (Reset)`, ...prev].slice(0, 15));
    }, 10000);
    return () => clearInterval(decayInterval);
  }, []);

  useEffect(() => {
    let interval: ReturnType<typeof setInterval>;
    if (isRunning) {
      interval = setInterval(() => {
        const clientIndex = Math.floor(Math.random() * CLIENTS.length);
        fireEvent(clientIndex);
      }, 1500);
    }
    
    return () => clearInterval(interval);
  }, [isRunning]);

  return (
    <div className="flex h-screen w-full bg-[#fdfcfb] text-stone-900 font-sans overflow-hidden">
      
      {/* LEFT PANEL: Controls (40%) */}
      <div className="w-[40%] border-r-2 border-stone-900 p-8 flex flex-col bg-white z-20 shadow-[4px_0_15px_-3px_rgba(0,0,0,0.1)]">
        <h1 className="text-4xl font-serif italic tracking-tight mb-2">Rate Limiter</h1>
        <p className="text-stone-500 font-mono text-sm mb-8">Distributed Token Bucket</p>
        
        <div className="border-2 border-stone-900 p-6 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] mb-8 bg-[#fdfcfb]">
          <h2 className="font-bold uppercase tracking-wider text-sm mb-6 border-b-2 border-stone-900 pb-2">Simulation Engine</h2>
          <div className="flex gap-4">
            <Button 
              variant="primary" 
              className="flex-1"
              onClick={() => setIsRunning(true)}
              disabled={isRunning}
            >
              <Play size={18} /> Start Traffic
            </Button>
            <Button 
              variant="default"
              className="flex-1"
              onClick={() => setIsRunning(false)}
              disabled={!isRunning}
            >
              <Square size={18} /> Stop
            </Button>
          </div>
          <p className="mt-6 text-xs font-mono text-stone-500">Max limit: {MAX_REQUESTS} reqs/window. Click user boxes manually to spam traffic!</p>
        </div>

        <div className="flex-1 border-2 border-stone-900 flex flex-col overflow-hidden shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] bg-[#fdfcfb]">
          <div className="bg-stone-900 text-white font-bold p-3 text-sm uppercase flex justify-between">
            <span>Proxy Access Logs</span>
            <span className="text-xs font-normal opacity-70">Window: 10s</span>
          </div>
          <div className="flex-1 overflow-y-auto p-4 space-y-2 bg-stone-50">
            <AnimatePresence>
              {logs.map((log, i) => (
                <motion.div 
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  key={i} 
                  className={`font-mono text-xs p-2 border-l-4 ${
                    log.includes('SYSTEM') ? 'border-blue-500 bg-blue-50 text-blue-900' :
                    log.includes('ALLOWED') ? 'border-green-500 bg-green-50 text-green-900' : 
                    'border-red-500 bg-red-50 text-red-900'
                  }`}
                >
                  {log}
                </motion.div>
              ))}
            </AnimatePresence>
            {logs.length === 0 && <p className="text-stone-400 font-mono text-xs italic">Awaiting traffic...</p>}
          </div>
        </div>
      </div>

      {/* RIGHT PANEL: Visualization (60%) */}
      <div className="w-[60%] relative bg-[#fdfcfb] overflow-hidden">
        
        {/* API Rate Limiter BOUNDARY BOX (Only encloses Shards and Proxy) */}
        <div className="absolute left-[25%] right-[15%] top-[10%] bottom-[40%] border-4 border-dashed border-stone-300 z-0 flex justify-center bg-stone-50/50">
            <div className="bg-stone-200 px-4 py-1 border-2 border-stone-300 font-bold uppercase tracking-widest text-stone-600 text-sm absolute -top-4">
                API Rate Limiter Cluster
            </div>
        </div>

        {/* Static SVG Connecting Lines */}
        <svg className="absolute inset-0 w-full h-full pointer-events-none z-0">
          {/* Client to Proxy Lines */}
          {CLIENTS.map((c, i) => (
            <line key={`c-${i}`} x1="18%" y1={c.y} x2="45%" y2="50%" stroke="#d6d3d1" strokeWidth="2" strokeDasharray="4 4" />
          ))}
          {/* Proxy to Shards Lines (Proxy is at 50%,50%, Shards are at their x, 20%) */}
          {SHARDS.map((s, i) => (
            <line key={`s-${i}`} x1="50%" y1="45%" x2={s.x} y2="28%" stroke="#d6d3d1" strokeWidth="2" strokeDasharray="4 4" />
          ))}
          {/* Proxy to API Server Line */}
          <line x1="50%" y1="55%" x2="50%" y2="75%" stroke="#d6d3d1" strokeWidth="2" strokeDasharray="4 4" />
        </svg>

        {/* Clients */}
        {CLIENTS.map((c, i) => {
          const isLimited = rateLimitedIps.has(c.ip);
          return (
          <div key={i} className="absolute left-[15%] -translate-y-1/2 flex items-center z-10" style={{ top: c.y }}>
            <button 
              onClick={() => fireEvent(i)}
              className={`w-16 h-16 border-2 border-stone-900 flex flex-col items-center justify-center shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] transition-all active:translate-y-[4px] active:shadow-none cursor-pointer ${isLimited ? 'bg-stone-300 text-stone-500 border-dashed' : 'bg-white hover:-translate-y-[2px] hover:shadow-[6px_6px_0px_0px_rgba(28,25,23,1)]'}`}
            >
              <span className="font-bold text-sm">{c.id}</span>
            </button>
            <span className={`font-mono text-[10px] absolute -bottom-8 w-full text-center ${isLimited ? 'text-red-600 font-bold' : 'text-stone-500'}`}>
                {c.ip}
                {isLimited && <span className="block text-[8px] text-red-600 tracking-widest mt-1">RATE LIMITED</span>}
            </span>
          </div>
        )})}

        {/* Proxy Server */}
        <div className="absolute left-[50%] top-[50%] -translate-x-1/2 -translate-y-1/2 z-10">
          <div className="w-32 h-20 bg-white border-2 border-stone-900 flex flex-col items-center justify-center shadow-[6px_6px_0px_0px_rgba(28,25,23,1)] relative">
            <span className="font-bold text-lg tracking-widest uppercase">Proxy</span>
            {/* Flashing indicator for Proxy -> Redis communication */}
            <AnimatePresence>
                {activeShardIndex !== null && (
                    <motion.div 
                        initial={{ opacity: 0, scale: 0 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0 }}
                        className="absolute -right-2 -top-2 w-4 h-4 bg-orange-500 rounded-full border-2 border-stone-900"
                    />
                )}
            </AnimatePresence>
          </div>
        </div>

        {/* Redis Shards (Arranged horizontally above Proxy) */}
        {SHARDS.map((s, i) => (
            <motion.div 
                key={i} 
                className="absolute top-[20%] w-24 h-16 border-2 border-red-900 flex items-center justify-center shadow-[4px_4px_0px_0px_rgba(127,29,29,1)] bg-red-50 -translate-x-1/2 -translate-y-1/2 z-10"
                style={{ left: s.x }}
                animate={activeShardIndex === i ? { scale: 1.1, backgroundColor: "#fef08a" } : { scale: 1, backgroundColor: "#fef2f2" }}
                transition={{ duration: 0.2 }}
            >
                <span className="font-bold text-red-900 text-sm font-mono">{s.id}</span>
            </motion.div>
        ))}

        {/* API Server (Below Proxy) */}
        <div className="absolute left-[50%] top-[80%] -translate-x-1/2 -translate-y-1/2 z-10">
          <div className="w-32 h-24 bg-green-50 border-2 border-green-900 flex items-center justify-center shadow-[6px_6px_0px_0px_rgba(20,83,45,1)]">
            <span className="font-bold text-green-900 uppercase tracking-widest text-center leading-tight">API<br/>Server</span>
          </div>
        </div>

        {/* ANIMATING TOKENS */}
        {tokens.map(t => {
            const client = CLIENTS[t.clientIndex];
            
            return (
                <motion.div
                    key={t.id}
                    className={`absolute w-5 h-5 border-2 border-stone-900 rounded-none shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] z-20 ${t.isAllowed ? 'bg-stone-800' : 'bg-red-500'}`}
                    initial={{ left: "15%", top: client.y }}
                    animate={{ 
                        // Sequence: 1. Move to Proxy. 2. Pause while Proxy asks Redis. 3. If allowed, move down to API server. If blocked, drop right.
                        left: ["15%", "50%", "50%", t.isAllowed ? "50%" : "55%"],
                        top: [client.y, "50%", "50%", t.isAllowed ? "80%" : "55%"],
                        opacity: [1, 1, 1, t.isAllowed ? 0 : 0],
                        scale: [1, 1, 1, t.isAllowed ? 1.5 : 0.5],
                        rotate: [0, 90, 90, 180]
                    }}
                    transition={{ duration: 2.2, times: [0, 0.4, 0.6, 1], ease: "easeInOut" }}
                />
            );
        })}

      </div>
    </div>
  );
}
