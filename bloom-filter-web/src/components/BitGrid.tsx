import { motion } from 'framer-motion';

interface BitGridProps {
  bits: boolean[];
  activeIndices: number[];
  checkSuccess?: boolean;
}

export function BitGrid({ bits, activeIndices, checkSuccess }: BitGridProps) {
  return (
    <div className="grid grid-cols-8 md:grid-cols-16 gap-1.5 p-6 bg-white border border-stone-200">
      {bits.map((bit, idx) => {
        const isActive = activeIndices.includes(idx);
        
        // Default styling (0 or 1)
        let bgClass = bit ? 'bg-stone-900 text-white' : 'bg-stone-100 text-stone-400';
        let borderClass = 'border-transparent';
        
        // Highlight active ones
        if (isActive) {
          if (checkSuccess === true) {
            bgClass = 'bg-emerald-500 text-white'; // Check found it
          } else if (checkSuccess === false && !bit) {
            bgClass = 'bg-rose-500 text-white'; // Check missed it
          } else {
            bgClass = 'bg-orange-500 text-white'; // Just adding / hashing
          }
          borderClass = 'border-orange-200';
        }

        return (
          <motion.div
            key={idx}
            layout
            initial={false}
            animate={{ scale: isActive ? 1.15 : 1 }}
            transition={{ type: "spring", stiffness: 300, damping: 20 }}
            className={`w-10 h-10 flex flex-col items-center justify-center font-mono text-sm border-2 transition-colors duration-300 ${bgClass} ${borderClass}`}
          >
            <span className="text-[9px] opacity-50 mb-0.5">{idx}</span>
            <span className="font-bold leading-none">{bit ? '1' : '0'}</span>
          </motion.div>
        );
      })}
    </div>
  );
}
