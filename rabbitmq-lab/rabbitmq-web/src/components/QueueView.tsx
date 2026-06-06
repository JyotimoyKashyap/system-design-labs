import { motion, AnimatePresence } from 'framer-motion';
import { Message } from '../simulation/Broker';

interface QueueViewProps {
  queue: Message[];
}

export function QueueView({ queue }: QueueViewProps) {
  return (
    <div className="w-full h-48 border-y-4 border-stone-800 bg-stone-100 relative overflow-hidden flex items-center px-4">
      {/* Decorative track lines */}
      <div className="absolute top-4 left-0 right-0 h-px bg-stone-300"></div>
      <div className="absolute bottom-4 left-0 right-0 h-px bg-stone-300"></div>

      <div className="flex gap-2 w-full justify-end overflow-hidden">
        <AnimatePresence mode="popLayout">
          {queue.map((msg) => (
            <motion.div
              layout
              key={msg.id}
              initial={{ opacity: 0, x: -100, scale: 0.8 }}
              animate={{ opacity: 1, x: 0, scale: 1 }}
              exit={{ opacity: 0, scale: 0.5, y: -50 }}
              transition={{ type: "spring", stiffness: 300, damping: 25 }}
              className="w-16 h-24 bg-white border-2 border-stone-900 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] flex flex-col justify-center items-center flex-shrink-0 relative"
            >
              <div className="absolute top-2 w-8 h-1 bg-stone-200"></div>
              <span className="font-mono font-bold text-xs rotate-[-90deg] whitespace-nowrap text-stone-800 tracking-wider">
                MSG-{msg.id}
              </span>
            </motion.div>
          ))}
        </AnimatePresence>
        
        {queue.length === 0 && (
          <div className="w-full flex justify-center items-center h-full absolute inset-0">
            <span className="text-stone-400 font-mono text-sm tracking-widest uppercase">Queue is empty</span>
          </div>
        )}
      </div>
    </div>
  );
}
