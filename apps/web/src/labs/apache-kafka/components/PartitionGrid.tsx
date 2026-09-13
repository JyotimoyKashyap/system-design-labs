import { motion, AnimatePresence } from 'framer-motion';
import { Partition } from '../simulation/Broker';

interface PartitionGridProps {
  partitions: Partition[];
  consumerOffsets: Map<number, number>;
}

export function PartitionGrid({ partitions, consumerOffsets }: PartitionGridProps) {
  return (
    <div className="flex flex-col gap-6 w-full">
      {partitions.map((p) => {
        const currentOffset = consumerOffsets.get(p.id) ?? -1;
        const visibleMessages = p.messages.filter(m => m.offset >= currentOffset - 2);

        return (
          <div key={p.id} className="relative flex flex-col gap-2">
            <div className="flex justify-between items-center">
              <h3 className="font-mono text-xs font-bold text-stone-700 uppercase tracking-wider">
                Partition-{p.id}
              </h3>
              <span className="font-mono text-[10px] text-stone-500 font-bold bg-stone-100 px-2 py-0.5 border border-stone-300">
                Committed Offset: {currentOffset}
              </span>
            </div>
            
            <div className="w-full h-20 bg-stone-50 border-2 border-stone-900 shadow-[3px_3px_0px_0px_rgba(28,25,23,1)] relative flex items-center px-4 overflow-hidden">
              <div className="absolute top-1/2 left-0 right-0 h-[2px] bg-stone-200 -translate-y-1/2"></div>
              
              <div className="flex gap-1.5 w-full relative h-full items-center overflow-x-auto">
                <AnimatePresence mode="popLayout">
                  {visibleMessages.map((msg) => {
                    const isRead = msg.offset <= currentOffset;
                    
                    return (
                      <motion.div
                        layout
                        key={msg.id}
                        initial={{ opacity: 0, scale: 0.8, x: -20 }}
                        animate={{ opacity: 1, scale: 1, x: 0 }}
                        exit={{ opacity: 0, scale: 0, x: -50 }}
                        transition={{ type: "spring", stiffness: 300, damping: 25 }}
                        className={`h-12 w-11 shrink-0 border-2 border-stone-900 flex flex-col justify-center items-center shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] relative transition-colors duration-300 ${
                          isRead ? 'bg-stone-900 text-white' : 'bg-white text-stone-900'
                        }`}
                      >
                        <span className="text-[9px] font-mono opacity-60 absolute top-0.5">{msg.offset}</span>
                        <span className="text-xs font-mono font-bold mt-2">{msg.payload.split('-')[1]}</span>
                      </motion.div>
                    );
                  })}
                </AnimatePresence>

                {visibleMessages.length === 0 && (
                  <span className="text-stone-400 font-mono text-xs w-full text-center tracking-widest uppercase">
                    Log Buffer Empty
                  </span>
                )}
              </div>
            </div>
            
            {/* Pointer Track */}
            <div className="h-4 relative w-full px-4 flex gap-1.5 overflow-hidden">
              {visibleMessages.map((msg) => {
                const isPointerHere = msg.offset === currentOffset;
                return (
                  <div key={`ptr-${msg.id}`} className="w-11 shrink-0 flex justify-center">
                    <AnimatePresence>
                      {isPointerHere && (
                        <motion.div 
                          layoutId={`pointer-${p.id}`}
                          initial={{ opacity: 0, y: 8 }}
                          animate={{ opacity: 1, y: 0 }}
                          className="w-0 h-0 border-l-[6px] border-l-transparent border-r-[6px] border-r-transparent border-b-[8px] border-b-orange-500"
                        />
                      )}
                    </AnimatePresence>
                  </div>
                );
              })}
            </div>
          </div>
        );
      })}
    </div>
  );
}
