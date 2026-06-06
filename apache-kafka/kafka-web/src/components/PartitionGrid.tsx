import { motion, AnimatePresence } from 'framer-motion';
import { Partition } from '../simulation/Broker';

interface PartitionGridProps {
  partitions: Partition[];
  consumerOffsets: Map<number, number>; // Partition ID -> Offset
}

export function PartitionGrid({ partitions, consumerOffsets }: PartitionGridProps) {
  return (
    <div className="flex flex-col gap-6 w-full">
      {partitions.map((p) => {
        const currentOffset = consumerOffsets.get(p.id) ?? -1;

        return (
          <div key={p.id} className="relative flex flex-col gap-2">
            <h3 className="font-mono text-sm font-bold text-stone-500 uppercase tracking-widest">
              Partition {p.id}
            </h3>
            
            <div className="w-full h-20 bg-stone-100 border border-stone-300 relative flex items-center px-4 overflow-hidden">
              {/* Immutable log track */}
              <div className="absolute top-1/2 left-0 right-0 h-px bg-stone-300 -translate-y-1/2"></div>
              
              <div className="flex gap-1 w-full relative h-full items-center">
                <AnimatePresence mode="popLayout">
                  {p.messages.map((msg) => {
                    // Is this message currently being pointed to by the consumer offset?
                    const isRead = msg.offset <= currentOffset;
                    
                    return (
                      <motion.div
                        layout
                        key={msg.id}
                        initial={{ opacity: 0, scale: 0.8, x: -20 }}
                        animate={{ opacity: 1, scale: 1, x: 0 }}
                        exit={{ opacity: 0, scale: 0, x: -50 }}
                        transition={{ type: "spring", stiffness: 300, damping: 25 }}
                        className={`h-12 w-10 border-2 flex flex-col justify-center items-center shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] relative transition-colors duration-500 ${
                          isRead ? 'bg-stone-800 border-stone-900 text-white' : 'bg-white border-stone-900 text-stone-800'
                        }`}
                      >
                        <span className="text-[10px] font-mono opacity-50 absolute top-1">{msg.offset}</span>
                        <span className="text-xs font-bold mt-2">{msg.payload.split('-')[1]}</span>
                      </motion.div>
                    );
                  })}
                </AnimatePresence>

                {p.messages.length === 0 && (
                  <span className="text-stone-400 font-mono text-xs w-full text-center tracking-widest uppercase">
                    Log is Empty
                  </span>
                )}
              </div>
            </div>
            
            {/* The Pointer Track Below the Partition */}
            <div className="h-4 relative w-full px-4 flex gap-1">
              {p.messages.map((msg) => {
                const isPointerHere = msg.offset === currentOffset;
                return (
                  <div key={`ptr-${msg.id}`} className="w-10 flex justify-center">
                    <AnimatePresence>
                      {isPointerHere && (
                        <motion.div 
                          layoutId={`pointer-${p.id}`}
                          initial={{ opacity: 0, y: 10 }}
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
