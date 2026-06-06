import { useState, useEffect, useRef } from 'react';
import { Broker, type Message } from './simulation/Broker';
import { Producer } from './simulation/Producer';
import { Consumer } from './simulation/Consumer';
import { QueueView } from './components/QueueView';
import { Plus, Minus, Server, Settings, Activity } from 'lucide-react';

export default function App() {
  const brokerRef = useRef<Broker>(new Broker());
  const broker = brokerRef.current;
  
  const [queue, setQueue] = useState<Message[]>([]);
  const [processedCount, setProcessedCount] = useState(0);
  const [producerCount, setProducerCount] = useState(0);
  const [consumerCount, setConsumerCount] = useState(0);
  
  const producersRef = useRef<Producer[]>([]);
  const consumersRef = useRef<Consumer[]>([]);
  
  // Force re-renders for nested object state
  const [, setTick] = useState(0);

  useEffect(() => {
    broker.onStateChange = () => {
      setQueue([...broker.queue]);
      setProcessedCount(broker.processedCount);
      setTick(t => t + 1);
    };

    // Initial setup: 1 Producer, 2 Consumers
    handleAddProducer();
    handleAddConsumer();
    handleAddConsumer();

    return () => {
      producersRef.current.forEach(p => p.stop());
      consumersRef.current.forEach(c => c.stop());
    };
  }, []);

  const handleAddProducer = () => {
    const p = new Producer(`P-${producersRef.current.length + 1}`, broker);
    producersRef.current.push(p);
    p.start();
    setProducerCount(producersRef.current.length);
  };

  const handleRemoveProducer = () => {
    if (producersRef.current.length === 0) return;
    const p = producersRef.current.pop();
    p?.stop();
    setProducerCount(producersRef.current.length);
  };

  const handleAddConsumer = () => {
    const c = new Consumer(`C-${consumersRef.current.length + 1}`, broker);
    // Bind consumer state changes to react re-renders
    c.onStateChange = () => setTick(t => t + 1);
    consumersRef.current.push(c);
    c.start();
    setConsumerCount(consumersRef.current.length);
  };

  const handleRemoveConsumer = () => {
    if (consumersRef.current.length === 0) return;
    const c = consumersRef.current.pop();
    c?.stop();
    setConsumerCount(consumersRef.current.length);
  };

  return (
    <div className="min-h-screen bg-white text-stone-900 flex flex-col font-sans selection:bg-orange-200 selection:text-orange-900">
      {/* Header */}
      <header className="p-6 lg:p-8 border-b border-stone-300 bg-[#fdfcfb] flex flex-col lg:flex-row justify-between lg:items-end gap-6 z-10">
        <div>
          <h1 className="text-4xl font-serif italic text-stone-900 mb-2 tracking-tight">RabbitMQ Visualizer</h1>
          <p className="text-stone-600 font-medium max-w-2xl">
            Simulating the <strong>Competing Consumers</strong> pattern. Producers generate messages asynchronously, pushing them onto a central message broker queue. Consumers pull messages off the queue for processing. 
          </p>
        </div>
        <div className="text-left lg:text-right text-sm font-mono text-stone-500">
          <p>Total Processed: <span className="text-stone-900 font-bold text-lg">{processedCount}</span></p>
          <p>Queue Length: <span className={queue.length > 20 ? 'text-rose-500 font-bold' : 'text-stone-900'}>{queue.length}</span></p>
        </div>
      </header>

      {/* Main Board */}
      <main className="flex-1 flex flex-col lg:flex-row overflow-hidden bg-white">
        
        {/* Left: Producers */}
        <div className="w-full lg:w-1/4 lg:min-w-[320px] shrink-0 flex flex-col p-6 lg:p-8 border-b lg:border-b-0 lg:border-r border-stone-300 bg-[#fdfcfb]">
          <div className="flex justify-between items-center border-b border-stone-300 pb-4 mb-6 gap-4">
            <h2 className="text-xl font-bold flex items-center gap-2 whitespace-nowrap"><Server size={20}/> Producers</h2>
            <div className="flex gap-2 shrink-0">
              <button onClick={handleRemoveProducer} className="p-2 border-2 border-stone-900 hover:bg-stone-100 transition-all disabled:opacity-50 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] hover:translate-y-[1px] hover:shadow-[1px_1px_0px_0px_rgba(28,25,23,1)] active:translate-y-[2px] active:shadow-none" disabled={producerCount === 0}><Minus size={14}/></button>
              <button onClick={handleAddProducer} className="p-2 border-2 border-stone-900 bg-stone-900 text-white hover:bg-stone-800 transition-all shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] hover:translate-y-[1px] hover:shadow-[1px_1px_0px_0px_rgba(28,25,23,1)] active:translate-y-[2px] active:shadow-none"><Plus size={14}/></button>
            </div>
          </div>
          
          <div className="flex-1 overflow-y-auto flex flex-col gap-4 min-h-[300px] pr-2">
            {producersRef.current.map(p => (
              <div key={p.id} className="p-4 border-2 border-stone-900 bg-white flex justify-between items-center shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]">
                <span className="font-mono font-bold text-stone-900">{p.id}</span>
                <span className="text-xs text-emerald-600 flex items-center gap-1 animate-pulse font-bold"><Activity size={12}/> Publishing</span>
              </div>
            ))}
            {producerCount === 0 && <p className="text-stone-400 text-sm italic text-center py-8 font-serif">No producers active.</p>}
          </div>
        </div>

        {/* Center: The Queue */}
        <div className="flex-1 flex flex-col justify-center relative min-h-[400px] min-w-0 overflow-hidden bg-white p-8">
          <h3 className="absolute top-8 left-1/2 -translate-x-1/2 font-mono text-sm font-bold text-stone-400 tracking-widest uppercase">Message Broker Queue</h3>
          <QueueView queue={queue} />
        </div>

        {/* Right: Consumers */}
        <div className="w-full lg:w-1/4 lg:min-w-[320px] shrink-0 flex flex-col p-6 lg:p-8 border-t lg:border-t-0 lg:border-l border-stone-300 bg-[#fdfcfb]">
          <div className="flex justify-between items-center border-b border-stone-300 pb-4 mb-6 gap-4">
            <h2 className="text-xl font-bold flex items-center gap-2 whitespace-nowrap"><Settings size={20}/> Consumers</h2>
            <div className="flex gap-2 shrink-0">
              <button onClick={handleRemoveConsumer} className="p-2 border-2 border-stone-900 hover:bg-stone-100 transition-all disabled:opacity-50 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] hover:translate-y-[1px] hover:shadow-[1px_1px_0px_0px_rgba(28,25,23,1)] active:translate-y-[2px] active:shadow-none" disabled={consumerCount === 0}><Minus size={14}/></button>
              <button onClick={handleAddConsumer} className="p-2 border-2 border-stone-900 bg-orange-500 text-white hover:bg-orange-600 transition-all shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] hover:translate-y-[1px] hover:shadow-[1px_1px_0px_0px_rgba(28,25,23,1)] active:translate-y-[2px] active:shadow-none"><Plus size={14}/></button>
            </div>
          </div>
          
          <div className="flex-1 overflow-y-auto flex flex-col gap-4 pr-2">
            {consumersRef.current.map(c => (
              <div key={c.id} className={`p-4 border-2 border-stone-900 ${c.processingMessage ? 'bg-orange-50 shadow-[4px_4px_0px_0px_rgba(249,115,22,0.4)]' : 'bg-white shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]'} flex flex-col gap-3 transition-all duration-300`}>
                <div className="flex justify-between items-center">
                  <span className="font-mono font-bold text-stone-900">{c.id}</span>
                  <span className={`text-[10px] font-bold px-2 py-1 border-2 border-stone-900 tracking-wider ${c.processingMessage ? 'bg-orange-500 text-stone-900' : 'bg-stone-200 text-stone-500'}`}>
                    {c.processingMessage ? 'WORKING' : 'IDLE'}
                  </span>
                </div>
                {c.processingMessage && (
                  <div className="text-sm font-mono text-stone-900 bg-white border-2 border-stone-900 p-2 text-center">
                    Msg: <span className="font-bold text-orange-600">{c.processingMessage.id}</span>
                  </div>
                )}
              </div>
            ))}
            {consumerCount === 0 && <p className="text-stone-400 text-sm italic text-center py-8 font-serif">No consumers active. Queue will build up!</p>}
          </div>
        </div>

      </main>
    </div>
  );
}
