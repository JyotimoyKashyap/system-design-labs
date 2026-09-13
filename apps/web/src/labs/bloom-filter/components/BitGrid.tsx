import { motion } from 'framer-motion';

interface BitGridProps {
  bits: boolean[];
  activeIndices: number[];
  checkSuccess?: boolean;
}

export function BitGrid({ bits, activeIndices, checkSuccess }: BitGridProps) {
  return (
    <div className="grid grid-cols-8 sm:grid-cols-16 gap-1.5 sm:gap-2 p-4 sm:p-6 bg-white border-3 border-stone-900 shadow-[6px_6px_0px_0px_rgba(28,25,23,1)] max-w-full overflow-x-auto">
      {bits.map((bit, idx) => {
        const isActive = activeIndices.includes(idx);
        
        let bgClass = bit ? 'bg-stone-900 text-white' : 'bg-stone-50 text-stone-400 hover:bg-stone-100';
        let borderClass = bit ? 'border-stone-900' : 'border-stone-200';
        
        if (isActive) {
          if (checkSuccess === true) {
            bgClass = 'bg-emerald-400 text-stone-900 font-black'; 
          } else if (checkSuccess === false && !bit) {
            bgClass = 'bg-rose-500 text-white font-black'; 
          } else {
            bgClass = 'bg-orange-500 text-stone-900 font-black'; 
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
            className={`w-8 h-8 sm:w-10 sm:h-10 md:w-11 md:h-11 flex flex-col items-center justify-center font-mono text-xs border-2 transition-all duration-200 ${bgClass} ${borderClass}`}
          >
            <span className={`text-[8px] opacity-60 leading-none`}>{idx}</span>
            <span className="font-bold leading-tight">{bit ? '1' : '0'}</span>
          </motion.div>
        );
      })}
    </div>
  );
}
