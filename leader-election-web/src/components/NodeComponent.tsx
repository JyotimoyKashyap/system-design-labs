import { motion } from 'framer-motion';
import type { NodeState } from '../simulation/types';

interface NodeProps {
  id: string;
  state: NodeState;
  term: number;
  timer?: { duration: number; key: number };
  x: number;
  y: number;
  onKill: (id: string) => void;
  onRevive: (id: string) => void;
}

export function NodeComponent({ id, state, term, timer, x, y, onKill, onRevive }: NodeProps) {
  // Brutalist solid stark colors
  const themeColors: Record<NodeState, { bg: string, border: string, text: string, svg: string }> = {
    FOLLOWER: { bg: 'bg-white', border: 'border-stone-900', text: 'text-stone-900', svg: 'stroke-stone-300' },
    CANDIDATE: { bg: 'bg-orange-500', border: 'border-stone-900', text: 'text-stone-900', svg: 'stroke-stone-900' },
    LEADER: { bg: 'bg-emerald-400', border: 'border-stone-900', text: 'text-stone-900', svg: 'stroke-stone-900' },
    DEAD: { bg: 'bg-stone-800', border: 'border-stone-900', text: 'text-white', svg: 'stroke-stone-600' },
  };

  const theme = themeColors[state];

  return (
    <motion.div
      className={`absolute w-32 h-32 rounded-full flex flex-col items-center justify-center font-medium border-4 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] cursor-pointer transition-colors duration-500 ${theme.bg} ${theme.border} ${theme.text}`}
      style={{ left: x, top: y, x: "-50%", y: "-50%" }}
      whileHover={{ scale: 1.05 }}
      whileTap={{ scale: 0.98 }}
      onClick={() => state === 'DEAD' ? onRevive(id) : onKill(id)}
      layout
    >
      <span className="text-xl font-bold tracking-tight">{id}</span>
      <span className="text-[10px] font-bold opacity-80 uppercase tracking-[0.2em] mt-1">{state}</span>
      <span className="text-xs font-mono mt-1.5 text-stone-900 bg-white px-2 py-0.5 rounded-none border-2 border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]">Term {term}</span>
      
      {/* Pulse effect for Leader */}
      {state === 'LEADER' && (
        <motion.div
          className="absolute inset-0 rounded-full border-4 border-emerald-400"
          animate={{ scale: [1, 1.15, 1], opacity: [1, 0, 1] }}
          transition={{ duration: 1.5, repeat: Infinity, ease: "easeInOut" }}
        />
      )}

      {/* Circular Progress Bar for Election Timeout */}
      {state !== 'LEADER' && state !== 'DEAD' && timer && (
        <svg className="absolute inset-0 w-full h-full -rotate-90 pointer-events-none" viewBox="0 0 100 100">
          <motion.circle
            key={timer.key}
            cx="50"
            cy="50"
            r="48"
            fill="transparent"
            className={theme.svg}
            strokeWidth="3"
            strokeLinecap="round"
            initial={{ pathLength: 1 }}
            animate={{ pathLength: 0 }}
            transition={{ duration: timer.duration / 1000, ease: 'linear' }}
          />
        </svg>
      )}
    </motion.div>
  );
}
