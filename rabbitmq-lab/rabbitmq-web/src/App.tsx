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
    <div className="min-h-screen bg-[#fdfcfb] text-stone-800 flex flex-col font-sans selection:bg-orange-100 selection:text-orange-900">
      {/* Header */}
      <header className="p-8 border-b border-stone-200 bg-white flex justify-between items-end">
        <div>
          <h1 className="text-4xl font-serif italic text-stone-900 mb-2 tracking-tight">RabbitMQ Visualizer</h1>
          <p className="text-stone-500 font-light max-w-2xl">
            Simulating the <strong>Competing Consumers</strong> pattern. Producers generate messages asynchronously, pushing them onto a central message broker queue. Consumers pull messages off the queue for processing. 
          </p>
        </div>
        <div className="text-right text-sm font-mono text-stone-400">
          <p>Total Processed: <span className="text-stone-900 font-bold text-lg">{processedCount}</span></p>
          <p>Queue Length: <span className={queue.length > 20 ? 'text-rose-500 font-bold' : 'text-stone-900'}>{queue.length}</span></p>
        </div>
      </header>

      {/* Main Board */}
      <main className="flex-1 flex flex-col lg:flex-row p-8 gap-8 overflow-hidden">
        
        {/* Left: Producers */}
        <div className="w-full lg:w-1/4 flex flex-col gap-4 border border-stone-200 bg-white p-6">
          <div className="flex justify-between items-center border-b border-stone-200 pb-4 mb-4">
            <h2 className="text-xl font-bold flex items-center gap-2"><Server size={20}/> Producers</h2>
            <div className="flex gap-2">
              <button onClick={handleRemoveProducer} className="p-2 border border-stone-200 hover:bg-stone-50 transition-colors disabled:opacity-50" disabled={producerCount === 0}><Minus size={14}/></button>
              <button onClick={handleAddProducer} className="p-2 border border-stone-200 bg-stone-900 text-white hover:bg-stone-800 transition-colors"><Plus size={14}/></button>
            </div>
          </div>
          
          <div className="flex-1 overflow-y-auto flex flex-col gap-3">
            {producersRef.current.map(p => (
              <div key={p.id} className="p-4 border border-stone-200 bg-stone-50 flex justify-between items-center">
                <span className="font-mono font-bold text-stone-700">{p.id}</span>
                <span className="text-xs text-emerald-600 flex items-center gap-1 animate-pulse"><Activity size={12}/> Publishing...</span>
              </div>
            ))}
            {producerCount === 0 && <p className="text-stone-400 text-sm italic text-center py-8">No producers active.</p>}
          </div>
        </div>

        {/* Center: The Queue */}
        <div className="flex-1 flex flex-col justify-center relative min-h-[400px]">
          <h3 className="absolute top-0 left-1/2 -translate-x-1/2 font-mono text-sm font-bold text-stone-400 tracking-widest uppercase">Message Broker Queue</h3>
          <QueueView queue={queue} />
        </div>

        {/* Right: Consumers */}
        <div className="w-full lg:w-1/4 flex flex-col gap-4 border border-stone-200 bg-white p-6">
          <div className="flex justify-between items-center border-b border-stone-200 pb-4 mb-4">
            <h2 className="text-xl font-bold flex items-center gap-2"><Settings size={20}/> Consumers</h2>
            <div className="flex gap-2">
              <button onClick={handleRemoveConsumer} className="p-2 border border-stone-200 hover:bg-stone-50 transition-colors disabled:opacity-50" disabled={consumerCount === 0}><Minus size={14}/></button>
              <button onClick={handleAddConsumer} className="p-2 border border-stone-200 bg-orange-500 text-white hover:bg-orange-600 transition-colors"><Plus size={14}/></button>
            </div>
          </div>
          
          <div className="flex-1 overflow-y-auto flex flex-col gap-3">
            {consumersRef.current.map(c => (
              <div key={c.id} className={`p-4 border ${c.processingMessage ? 'border-orange-500 bg-orange-50' : 'border-stone-200 bg-stone-50'} flex flex-col gap-2 transition-colors duration-300`}>
                <div className="flex justify-between items-center">
                  <span className="font-mono font-bold text-stone-700">{c.id}</span>
                  <span className={`text-xs font-mono px-2 py-1 ${c.processingMessage ? 'bg-orange-500 text-white' : 'bg-stone-200 text-stone-500'}`}>
                    {c.processingMessage ? 'WORKING' : 'IDLE'}
                  </span>
                </div>
                {c.processingMessage && (
                  <div className="text-sm font-mono text-stone-600 bg-white border border-orange-200 p-2 text-center">
                    Processing Msg: <span className="font-bold text-stone-900">{c.processingMessage.id}</span>
                  </div>
                )}
              </div>
            ))}
            {consumerCount === 0 && <p className="text-stone-400 text-sm italic text-center py-8">No consumers active. Queue will build up!</p>}
          </div>
        </div>

      </main>
    </div>
  );
}
