import { motion, AnimatePresence } from 'framer-motion';
import { type Message } from '../simulation/Broker';

interface QueueViewProps {
  queue: Message[];
}

export function QueueView({ queue }: QueueViewProps) {
  return (
    <div className="w-full h-44 border-y-3 border-stone-900 bg-stone-100 relative overflow-hidden flex items-center px-4 shadow-[inset_0_2px_4px_rgba(0,0,0,0.06)]">
      {/* Decorative track lines */}
      <div className="absolute top-4 left-0 right-0 h-[2px] bg-stone-300"></div>
      <div className="absolute bottom-4 left-0 right-0 h-[2px] bg-stone-300"></div>

      <div className="flex gap-2 w-full justify-end overflow-x-auto py-2">
        <AnimatePresence mode="popLayout">
          {queue.slice().reverse().map((msg) => (
            <motion.div
              layout
              key={msg.id}
              initial={{ opacity: 0, x: -80, scale: 0.8 }}
              animate={{ opacity: 1, x: 0, scale: 1 }}
              exit={{ opacity: 0, scale: 0.5, y: -40 }}
              transition={{ type: "spring", stiffness: 300, damping: 25 }}
              className="w-14 h-22 sm:w-16 sm:h-24 shrink-0 bg-white border-2 border-stone-900 shadow-[3px_3px_0px_0px_rgba(28,25,23,1)] flex flex-col justify-center items-center relative"
            >
              <div className="absolute top-2 w-6 sm:w-8 h-1 bg-stone-200"></div>
              <span className="font-mono font-bold text-xs rotate-[-90deg] whitespace-nowrap text-stone-900 tracking-wider">
                MSG-{msg.id}
              </span>
            </motion.div>
          ))}
        </AnimatePresence>
        
        {queue.length === 0 && (
          <div className="w-full flex justify-center items-center h-full absolute inset-0">
            <span className="text-stone-400 font-mono text-xs sm:text-sm tracking-widest uppercase">Queue buffer empty</span>
          </div>
        )}
      </div>
    </div>
  );
}
