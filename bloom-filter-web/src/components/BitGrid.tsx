import { motion } from 'framer-motion';

interface BitGridProps {
  bits: boolean[];
  activeIndices: number[];
  checkSuccess?: boolean;
}

export function BitGrid({ bits, activeIndices, checkSuccess }: BitGridProps) {
  return (
    <div className="grid grid-cols-8 md:grid-cols-16 gap-2 p-6 md:p-8 bg-white border-2 border-stone-900 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]">
      {bits.map((bit, idx) => {
        const isActive = activeIndices.includes(idx);
        
        // Default styling
        let bgClass = bit ? 'bg-stone-900 text-white' : 'bg-white text-stone-400 hover:bg-stone-50';
        let borderClass = bit ? 'border-stone-900' : 'border-stone-200';
        
        // Highlight active ones
        if (isActive) {
          if (checkSuccess === true) {
            bgClass = 'bg-emerald-400 text-stone-900'; 
          } else if (checkSuccess === false && !bit) {
            bgClass = 'bg-rose-500 text-white'; 
          } else {
            bgClass = 'bg-orange-500 text-stone-900'; 
          }
          borderClass = 'border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]';
        }

        return (
          <motion.div
            key={idx}
            layout
            initial={false}
            animate={{ scale: isActive ? 1.15 : 1, zIndex: isActive ? 10 : 1 }}
            transition={{ type: "spring", stiffness: 300, damping: 20 }}
            className={`w-10 h-10 md:w-12 md:h-12 flex flex-col items-center justify-center font-mono text-sm border-2 transition-all duration-200 ${bgClass} ${borderClass}`}
          >
            <span className={`text-[9px] opacity-50 mb-0.5`}>{idx}</span>
            <span className="font-bold leading-none">{bit ? '1' : '0'}</span>
          </motion.div>
        );
      })}
    </div>
  );
}
