import { useState, useMemo } from 'react';
import { BloomFilter } from './core/BloomFilter';
import { BitGrid } from './components/BitGrid';
import { Plus, Search, RotateCcw } from 'lucide-react';
import { Button, Input } from '@repo/ui';

export default function BloomFilterVisualizer() {
  const [resetKey, setResetKey] = useState(0);
  const filter = useMemo(() => new BloomFilter(128), [resetKey]);
  const [bits, setBits] = useState<boolean[]>([...filter.bitArray]);
  const [activeIndices, setActiveIndices] = useState<number[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [checkSuccess, setCheckSuccess] = useState<boolean | undefined>(undefined);
  const [log, setLog] = useState<{ message: string; type: 'info' | 'success' | 'error' } | null>(null);

  const handleAdd = (e?: React.FormEvent) => {
    e?.preventDefault();
    if (!inputValue.trim()) return;
    const indices = filter.add(inputValue.trim());
    setBits([...filter.bitArray]);
    setActiveIndices(indices);
    setCheckSuccess(undefined);
    setLog({ message: `Added "${inputValue.trim()}" to the set (Hashed to bits: [${indices.join(', ')}]).`, type: 'info' });
    setInputValue('');
  };

  const handleCheck = () => {
    if (!inputValue.trim()) return;
    const result = filter.check(inputValue.trim());
    setActiveIndices(result.indices);
    setCheckSuccess(result.present);
    if (result.present) {
      setLog({ message: `"${inputValue.trim()}" is PROBABLY present in the filter (Bits [${result.indices.join(', ')}] are 1).`, type: 'success' });
    } else {
      setLog({ message: `"${inputValue.trim()}" is DEFINITELY NOT present in the filter (Found zero bits at [${result.indices.join(', ')}]).`, type: 'error' });
    }
  };

  const handleReset = () => {
    filter.reset();
    setBits([...filter.bitArray]);
    setActiveIndices([]);
    setCheckSuccess(undefined);
    setLog(null);
    setInputValue('');
    setResetKey(k => k + 1);
  };

  return (
    <div className="w-full h-full bg-[#fdfcfb] text-stone-800 font-sans selection:bg-orange-100 selection:text-orange-900 flex flex-col lg:flex-row overflow-y-auto lg:overflow-hidden">
      
      {/* Left Column: Controls (38%) */}
      <div className="w-full lg:w-[38%] h-full flex flex-col p-6 sm:p-10 border-b-2 lg:border-b-0 lg:border-r-2 border-stone-900 bg-[#fdfcfb] overflow-y-auto shrink-0 justify-between">
        <div>
          <span className="inline-block px-2.5 py-0.5 font-mono text-[10px] font-black uppercase tracking-wider bg-emerald-600 text-white border border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] mb-4">
            Probabilistic Data Structures
          </span>
          <h2 className="text-2xl sm:text-3xl font-silkscreen not-italic text-stone-900 mb-4 tracking-tight leading-tight">
            Bloom Filter
          </h2>
          <p className="text-stone-600 text-sm sm:text-base leading-relaxed font-medium mb-8">
            A space-efficient probabilistic data structure. It guarantees that false negatives are impossible: an element is either <strong>definitely not</strong> in the set, or <strong>probably</strong> in the set.
          </p>

          <form onSubmit={handleAdd} className="w-full flex flex-col gap-4 mb-6">
            <Input 
              type="text" 
              value={inputValue}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setInputValue(e.target.value)}
              placeholder="Enter a key (e.g. user_101)"
              className="text-base font-mono border-2 border-stone-900 shadow-[3px_3px_0px_0px_rgba(28,25,23,1)]"
            />
            
            <div className="flex gap-3">
              <Button type="button" onClick={handleAdd} className="flex-1" variant="default">
                <Plus size={16} /> Add Key
              </Button>
              <Button type="button" onClick={handleCheck} variant="primary" className="flex-1">
                <Search size={16} /> Query Set
              </Button>
            </div>
          </form>

          <Button onClick={handleReset} variant="secondary" size="sm">
            <RotateCcw size={14} /> Clear Bit Array
          </Button>

          {/* Status Log */}
          {log && (
            <div className={`mt-6 w-full p-4 border-2 border-stone-900 border-l-4 shadow-[3px_3px_0px_0px_rgba(28,25,23,1)] ${
              log.type === 'success' ? 'bg-emerald-50 border-l-emerald-600 text-emerald-950' :
              log.type === 'error' ? 'bg-rose-50 border-l-rose-600 text-rose-950' :
              'bg-blue-50 border-l-blue-600 text-blue-950'
            }`}>
              <p className="font-mono text-xs font-bold">{log.message}</p>
            </div>
          )}
        </div>
        
        <div className="mt-8 pt-4 border-t-2 border-dashed border-stone-300 text-xs font-mono text-stone-500 space-y-1">
          <p>• Bit Array Capacity: <strong>{filter.size} bits</strong></p>
          <p>• Independent Hashes (k): <strong>3 (FNV, DJB2, SDBM)</strong></p>
          <p>• Total Elements Inserted: <strong>{filter.itemsAdded}</strong></p>
        </div>
      </div>

      {/* Right Column: Bit Array Grid (62%) */}
      <div className="flex-1 w-full h-full relative flex justify-center items-center bg-[#fdfcfb] p-6 sm:p-12 overflow-y-auto min-h-[480px]">
        <BitGrid bits={bits} activeIndices={activeIndices} checkSuccess={checkSuccess} />
      </div>
      
    </div>
  );
}
