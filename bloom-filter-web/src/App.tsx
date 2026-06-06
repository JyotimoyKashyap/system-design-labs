import { useState, useMemo } from 'react';
import { BloomFilter } from './core/BloomFilter';
import { BitGrid } from './components/BitGrid';
import { Plus, Search, RotateCcw } from 'lucide-react';

export default function App() {
  const filter = useMemo(() => new BloomFilter(128), []);
  const [bits, setBits] = useState<boolean[]>([...filter.bitArray]);
  const [activeIndices, setActiveIndices] = useState<number[]>([]);
  const [inputValue, setInputValue] = useState('');
  const [checkSuccess, setCheckSuccess] = useState<boolean | undefined>(undefined);
  const [log, setLog] = useState<{message: string, type: 'info'|'success'|'error'} | null>(null);

  const handleAdd = (e?: React.FormEvent) => {
    e?.preventDefault();
    if (!inputValue.trim()) return;
    const indices = filter.add(inputValue.trim());
    setBits([...filter.bitArray]);
    setActiveIndices(indices);
    setCheckSuccess(undefined);
    setLog({ message: `Added "${inputValue.trim()}" to the set.`, type: 'info' });
    setInputValue('');
  };

  const handleCheck = () => {
    if (!inputValue.trim()) return;
    const result = filter.check(inputValue.trim());
    setActiveIndices(result.indices);
    setCheckSuccess(result.present);
    if (result.present) {
      setLog({ message: `"${inputValue.trim()}" is PROBABLY present.`, type: 'success' });
    } else {
      setLog({ message: `"${inputValue.trim()}" is DEFINITELY NOT present.`, type: 'error' });
    }
  };

  const handleReset = () => {
    filter.reset();
    setBits([...filter.bitArray]);
    setActiveIndices([]);
    setCheckSuccess(undefined);
    setLog(null);
    setInputValue('');
  };

  return (
    <div className="min-h-screen bg-[#fdfcfb] text-stone-800 flex items-center justify-center p-8 font-sans selection:bg-orange-100 selection:text-orange-900">
      <div className="max-w-[1600px] w-full flex flex-col lg:flex-row gap-12 lg:gap-24 items-center justify-between">
        
        {/* Left Column: Text & Controls */}
        <div className="flex-1 flex flex-col items-start text-left max-w-xl">
          <h1 className="text-5xl font-serif italic text-stone-900 mb-6 tracking-tight leading-tight">
            Bloom Filter
          </h1>
          <p className="text-stone-500 text-lg leading-relaxed font-light mb-10">
            A space-efficient probabilistic data structure. It can tell you if an element is <strong>definitely not</strong> in the set, or <strong>probably</strong> in the set.
          </p>

          <form onSubmit={handleAdd} className="w-full flex flex-col gap-4 mb-8">
            <input 
              type="text" 
              value={inputValue}
              onChange={e => setInputValue(e.target.value)}
              placeholder="Enter a word (e.g. apple)"
              className="w-full px-6 py-4 bg-white border border-stone-200 text-stone-800 placeholder-stone-400 focus:outline-none focus:border-stone-400 focus:ring-0 rounded-none text-lg"
            />
            
            <div className="flex gap-4">
              <button type="button" onClick={handleAdd} className="flex-1 flex items-center justify-center gap-2 px-6 py-4 bg-stone-900 text-white hover:bg-stone-800 rounded-none font-medium transition-colors">
                <Plus size={18} /> Add
              </button>
              <button type="button" onClick={handleCheck} className="flex-1 flex items-center justify-center gap-2 px-6 py-4 bg-orange-500 text-white hover:bg-orange-600 rounded-none font-medium transition-colors">
                <Search size={18} /> Check
              </button>
            </div>
          </form>

          <button onClick={handleReset} className="flex items-center gap-2 px-6 py-3 bg-white text-stone-700 border border-stone-200 hover:bg-stone-50 rounded-none font-medium transition-colors text-sm">
            <RotateCcw size={16} /> Reset Filter
          </button>

          {/* Status Log */}
          {log && (
            <div className={`mt-8 w-full p-4 border-l-4 rounded-none ${
              log.type === 'success' ? 'bg-emerald-50 border-emerald-500 text-emerald-800' :
              log.type === 'error' ? 'bg-rose-50 border-rose-500 text-rose-800' :
              'bg-stone-100 border-stone-500 text-stone-800'
            }`}>
              <p className="font-mono text-sm">{log.message}</p>
            </div>
          )}
          
          <div className="mt-8 text-sm font-mono text-stone-400">
            Array Size: {filter.size} bits <br/>
            Hash Functions (k): 3 <br/>
            Items Added: {filter.itemsAdded}
          </div>
        </div>

        {/* Right Column: The Bit Array Grid */}
        <div className="flex-[1.5] relative flex justify-center items-center w-full">
          <BitGrid bits={bits} activeIndices={activeIndices} checkSuccess={checkSuccess} />
        </div>
        
      </div>
    </div>
  );
}
