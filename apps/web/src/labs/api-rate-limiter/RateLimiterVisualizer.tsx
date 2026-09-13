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

interface TokenPacket {
  id: string;
  clientIndex: number;
  shardIndex: number;
  isAllowed: boolean;
}

export default function RateLimiterVisualizer() {
  const [isRunning, setIsRunning] = useState(false);
  const [logs, setLogs] = useState<string[]>([]);
  const [tokens, setTokens] = useState<TokenPacket[]>([]);
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
      if (currentCount + 1 >= MAX_REQUESTS) {
        setRateLimitedIps(prev => new Set(prev).add(client.ip));
      }
    }

    setLogs(prev => [`[${client.id}] -> Shard ${shardIndex + 1} -> ${isAllowed ? 'ALLOWED (200 OK)' : 'RATE LIMITED (429 Too Many Requests)'}`, ...prev].slice(0, 15));

    setTokens(prev => [...prev, { id: tokenId, clientIndex, shardIndex, isAllowed }]);

    setTimeout(() => setActiveShardIndex(shardIndex), 700);
    setTimeout(() => setActiveShardIndex(null), 1200);

    setTimeout(() => {
      setTokens(prev => prev.filter(t => t.id !== tokenId));
    }, 2500);
  };

  // Window decay loop (resets token buckets every 10 seconds)
  useEffect(() => {
    const decayInterval = setInterval(() => {
      rateLimits.current = {};
      setRateLimitedIps(new Set());
      setLogs(prev => [`[SYSTEM] Sliding window cycle elapsed (Buckets Reset)`, ...prev].slice(0, 15));
    }, 10000);
    return () => clearInterval(decayInterval);
  }, []);

  useEffect(() => {
    let interval: ReturnType<typeof setInterval>;
    if (isRunning) {
      interval = setInterval(() => {
        const clientIndex = Math.floor(Math.random() * CLIENTS.length);
        fireEvent(clientIndex);
      }, 1400);
    }
    
    return () => clearInterval(interval);
  }, [isRunning]);

  return (
    <div className="flex flex-col lg:flex-row h-full w-full bg-[#fdfcfb] text-stone-900 font-sans overflow-y-auto lg:overflow-hidden">
      
      {/* LEFT PANEL: Controls (38%) */}
      <div className="w-full lg:w-[38%] border-b-2 lg:border-b-0 lg:border-r-2 border-stone-900 p-6 sm:p-10 flex flex-col bg-[#fdfcfb] z-20 shrink-0 justify-between">
        <div>
          <span className="inline-block px-2.5 py-0.5 font-mono text-[10px] font-black uppercase tracking-wider bg-rose-600 text-white border border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] mb-4">
            Traffic Control &amp; Abuse Prevention
          </span>
          <h2 className="text-2xl sm:text-3xl font-silkscreen not-italic tracking-tight mb-2">
            Rate Limiter
          </h2>
          <p className="text-stone-500 font-mono text-xs uppercase tracking-wider mb-6">
            Distributed Sharded Token Bucket
          </p>
          
          <div className="border-2 border-stone-900 p-5 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] mb-6 bg-white">
            <h3 className="font-bold uppercase font-mono tracking-wider text-xs mb-4 border-b-2 border-stone-200 pb-2">
              Simulation Engine
            </h3>
            <div className="flex gap-3">
              <Button 
                variant="primary" 
                className="flex-1"
                onClick={() => setIsRunning(true)}
                disabled={isRunning}
              >
                <Play size={16} /> Start Traffic
              </Button>
              <Button 
                variant="default"
                className="flex-1"
                onClick={() => setIsRunning(false)}
                disabled={!isRunning}
              >
                <Square size={16} /> Stop
              </Button>
            </div>
            <p className="mt-4 text-xs font-mono text-stone-500">
              Max threshold: <strong>{MAX_REQUESTS} reqs / 10s window</strong>. You can also click the user boxes on the canvas to spam manual requests!
            </p>
          </div>

          <div className="border-2 border-stone-900 flex flex-col overflow-hidden shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] bg-white max-h-[260px]">
            <div className="bg-stone-900 text-white font-mono font-bold p-2.5 text-xs uppercase flex justify-between">
              <span>Proxy Access Logs</span>
              <span className="text-[10px] opacity-75">Window: 10s</span>
            </div>
            <div className="flex-1 overflow-y-auto p-3 space-y-1.5 bg-stone-50">
              <AnimatePresence>
                {logs.map((log, i) => (
                  <motion.div 
                    initial={{ opacity: 0, x: -8 }}
                    animate={{ opacity: 1, x: 0 }}
                    key={i} 
                    className={`font-mono text-[11px] p-2 border-l-4 ${
                      log.includes('SYSTEM') ? 'border-blue-500 bg-blue-50 text-blue-900' :
                      log.includes('ALLOWED') ? 'border-emerald-500 bg-emerald-50 text-emerald-900' : 
                      'border-rose-500 bg-rose-50 text-rose-900'
                    }`}
                  >
                    {log}
                  </motion.div>
                ))}
              </AnimatePresence>
              {logs.length === 0 && <p className="text-stone-400 font-mono text-xs italic">Awaiting traffic generation...</p>}
            </div>
          </div>
        </div>

        <div className="mt-6 pt-4 border-t-2 border-dashed border-stone-300 font-mono text-xs text-stone-500">
          Reverse proxy hashes client IPs across distributed Redis shards to keep state decentralized.
        </div>
      </div>

      {/* RIGHT PANEL: Visualization (62%) */}
      <div className="flex-1 w-full h-full relative bg-[#fdfcfb] overflow-hidden min-h-[500px]">
        
        {/* API Rate Limiter BOUNDARY BOX */}
        <div className="absolute left-[25%] right-[15%] top-[10%] bottom-[35%] border-3 border-dashed border-stone-300 z-0 flex justify-center bg-stone-50/50">
          <div className="bg-stone-200 px-3 py-1 border-2 border-stone-300 font-mono font-bold uppercase tracking-widest text-stone-600 text-xs absolute -top-3.5 shadow-sm">
            API Gateway &amp; Shard Cluster
          </div>
        </div>

        {/* Static SVG Connecting Lines */}
        <svg className="absolute inset-0 w-full h-full pointer-events-none z-0">
          {CLIENTS.map((c, i) => (
            <line key={`c-${i}`} x1="18%" y1={c.y} x2="45%" y2="50%" stroke="#d6d3d1" strokeWidth="2" strokeDasharray="4 4" />
          ))}
          {SHARDS.map((s, i) => (
            <line key={`s-${i}`} x1="50%" y1="45%" x2={s.x} y2="25%" stroke="#d6d3d1" strokeWidth="2" strokeDasharray="4 4" />
          ))}
          <line x1="50%" y1="55%" x2="50%" y2="78%" stroke="#d6d3d1" strokeWidth="2" strokeDasharray="4 4" />
        </svg>

        {/* Clients */}
        {CLIENTS.map((c, i) => {
          const isLimited = rateLimitedIps.has(c.ip);
          return (
            <div key={i} className="absolute left-[12%] sm:left-[15%] -translate-y-1/2 flex items-center z-10" style={{ top: c.y }}>
              <button 
                onClick={() => fireEvent(i)}
                className={`w-14 h-14 sm:w-16 sm:h-16 border-2 border-stone-900 flex flex-col items-center justify-center shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] transition-all active:translate-y-[2px] active:shadow-none cursor-pointer ${isLimited ? 'bg-stone-300 text-stone-500 border-dashed' : 'bg-white hover:-translate-y-[2px] hover:shadow-[6px_6px_0px_0px_rgba(28,25,23,1)]'}`}
              >
                <span className="font-bold text-xs sm:text-sm font-mono">{c.id}</span>
              </button>
              <span className={`font-mono text-[9px] sm:text-[10px] absolute -bottom-7 w-24 text-center -left-4 sm:left-0 ${isLimited ? 'text-rose-600 font-bold' : 'text-stone-500'}`}>
                {c.ip}
                {isLimited && <span className="block text-[8px] text-rose-600 tracking-widest font-black">RATE LIMITED</span>}
              </span>
            </div>
          );
        })}

        {/* Proxy Server */}
        <div className="absolute left-[50%] top-[50%] -translate-x-1/2 -translate-y-1/2 z-10">
          <div className="w-28 sm:w-32 h-16 sm:h-20 bg-white border-2 border-stone-900 flex flex-col items-center justify-center shadow-[6px_6px_0px_0px_rgba(28,25,23,1)] relative">
            <span className="font-silkscreen not-italic font-bold text-sm sm:text-base tracking-wider uppercase">Proxy</span>
            <span className="font-mono text-[9px] text-stone-500 uppercase tracking-widest mt-0.5">Token Bucket</span>
            <AnimatePresence>
              {activeShardIndex !== null && (
                <motion.div 
                  initial={{ opacity: 0, scale: 0 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0 }}
                  className="absolute -right-2 -top-2 w-4 h-4 bg-orange-500 rounded-none border-2 border-stone-900 shadow-[1px_1px_0px_0px_rgba(0,0,0,1)]"
                />
              )}
            </AnimatePresence>
          </div>
        </div>

        {/* Redis Shards */}
        {SHARDS.map((s, i) => (
          <motion.div 
            key={i} 
            className="absolute top-[18%] w-20 sm:w-24 h-12 sm:h-14 border-2 border-red-900 flex items-center justify-center shadow-[4px_4px_0px_0px_rgba(127,29,29,1)] bg-red-50 -translate-x-1/2 -translate-y-1/2 z-10"
            style={{ left: s.x }}
            animate={activeShardIndex === i ? { scale: 1.1, backgroundColor: "#fef08a" } : { scale: 1, backgroundColor: "#fef2f2" }}
            transition={{ duration: 0.2 }}
          >
            <span className="font-bold text-red-900 text-xs font-mono">{s.id}</span>
          </motion.div>
        ))}

        {/* API Server (Below Proxy) */}
        <div className="absolute left-[50%] top-[80%] -translate-x-1/2 -translate-y-1/2 z-10">
          <div className="w-28 sm:w-32 h-18 sm:h-22 bg-emerald-50 border-2 border-emerald-900 flex items-center justify-center shadow-[6px_6px_0px_0px_rgba(6,78,59,1)]">
            <span className="font-bold text-emerald-900 uppercase font-mono tracking-widest text-center text-xs leading-tight">API Backend<br/>Target</span>
          </div>
        </div>

        {/* ANIMATING TOKENS */}
        {tokens.map(t => {
          const client = CLIENTS[t.clientIndex];
          return (
            <motion.div
              key={t.id}
              className={`absolute w-4 h-4 sm:w-5 sm:h-5 border-2 border-stone-900 rounded-none shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] z-20 ${t.isAllowed ? 'bg-stone-900' : 'bg-rose-500'}`}
              initial={{ left: "15%", top: client.y }}
              animate={{ 
                left: ["15%", "50%", "50%", t.isAllowed ? "50%" : "55%"],
                top: [client.y, "50%", "50%", t.isAllowed ? "80%" : "55%"],
                opacity: [1, 1, 1, 0],
                scale: [1, 1, 1, t.isAllowed ? 1.4 : 0.4],
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
