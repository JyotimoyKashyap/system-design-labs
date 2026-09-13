import { useState, useEffect, useRef } from 'react';
import { Broker, type Message } from './simulation/Broker';
import { Producer } from './simulation/Producer';
import { Consumer } from './simulation/Consumer';
import { QueueView } from './components/QueueView';
import { Plus, Minus, Server, Settings, Activity, Play, RotateCcw, Loader2 } from 'lucide-react';
import { Button } from '@repo/ui';

export default function RabbitMQVisualizer() {
  const brokerRef = useRef<Broker>(new Broker());
  const broker = brokerRef.current;
  
  const [queue, setQueue] = useState<Message[]>([]);
  const [processedCount, setProcessedCount] = useState(0);
  const [producerCount, setProducerCount] = useState(0);
  const [consumerCount, setConsumerCount] = useState(0);
  const [isRunning, setIsRunning] = useState(false);
  
  const producersRef = useRef<Producer[]>([]);
  const consumersRef = useRef<Consumer[]>([]);
  
  const [, setTick] = useState(0);

  useEffect(() => {
    broker.onStateChange = () => {
      setQueue([...broker.queue]);
      setProcessedCount(broker.processedCount);
      setTick(t => t + 1);
    };

    return () => {
      producersRef.current.forEach(p => p.stop());
      consumersRef.current.forEach(c => c.stop());
    };
  }, [broker]);

  const handleStart = () => {
    if (producersRef.current.length === 0 && consumersRef.current.length === 0) {
      const p = new Producer(`P-1`, broker);
      producersRef.current.push(p);
      setProducerCount(1);
      p.start();

      const c1 = new Consumer(`C-1`, broker);
      c1.onStateChange = () => setTick(t => t + 1);
      consumersRef.current.push(c1);
      c1.start();

      const c2 = new Consumer(`C-2`, broker);
      c2.onStateChange = () => setTick(t => t + 1);
      consumersRef.current.push(c2);
      c2.start();
      
      setConsumerCount(2);
    } else {
      producersRef.current.forEach(p => p.start());
      consumersRef.current.forEach(c => c.start());
    }
    setIsRunning(true);
  };

  const handleReset = () => {
    producersRef.current.forEach(p => p.stop());
    consumersRef.current.forEach(c => c.stop());
    producersRef.current = [];
    consumersRef.current = [];
    broker.reset();
    setProducerCount(0);
    setConsumerCount(0);
    setIsRunning(false);
    setQueue([]);
    setProcessedCount(0);
    setTick(t => t + 1);
  };

  const handleAddProducer = () => {
    const p = new Producer(`P-${producersRef.current.length + 1}`, broker);
    producersRef.current.push(p);
    if (isRunning) p.start();
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
    c.onStateChange = () => setTick(t => t + 1);
    consumersRef.current.push(c);
    if (isRunning) c.start();
    setConsumerCount(consumersRef.current.length);
  };

  const handleRemoveConsumer = () => {
    if (consumersRef.current.length === 0) return;
    const c = consumersRef.current.pop();
    c?.stop();
    setConsumerCount(consumersRef.current.length);
  };

  return (
    <div className="h-full w-full bg-[#fdfcfb] text-stone-900 flex flex-col font-sans selection:bg-orange-200 selection:text-orange-900 overflow-y-auto lg:overflow-hidden">
      
      {/* Top Banner & Telemetry */}
      <div className="p-4 sm:p-6 border-b-2 border-stone-900 bg-white flex flex-col md:flex-row justify-between md:items-center gap-4 shrink-0 shadow-sm">
        <div>
          <span className="inline-block px-2.5 py-0.5 font-mono text-[10px] font-black uppercase tracking-wider bg-purple-600 text-white border border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] mb-2">
            Message Brokers &amp; Work Queues
          </span>
          <h2 className="text-xl sm:text-2xl font-silkscreen not-italic text-stone-900 tracking-tight">
            RabbitMQ Competing Consumers
          </h2>
          <p className="text-stone-600 text-xs sm:text-sm font-medium mt-1">
            Asynchronous task queue with round-robin dispatch. Scale workers up or down to clear backlogs.
          </p>
        </div>

        <div className="flex items-center gap-4 flex-wrap">
          <div className="flex gap-2">
            <Button onClick={handleStart} variant="primary" disabled={isRunning} className="text-xs">
              {isRunning ? <Loader2 size={15} className="animate-spin" /> : <Play size={15} />}
              {isRunning ? 'Running' : 'Start Simulation'}
            </Button>
            <Button onClick={handleReset} variant="secondary" className="text-xs">
              <RotateCcw size={15} /> Reset
            </Button>
          </div>

          <div className="flex items-center gap-4 font-mono text-xs border-2 border-stone-900 px-3 py-1.5 bg-stone-50 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]">
            <div>Processed: <strong className="text-stone-900 text-sm">{processedCount}</strong></div>
            <div className="w-px h-4 bg-stone-300"></div>
            <div>Queue Depth: <strong className={queue.length > 15 ? 'text-rose-600 text-sm' : 'text-stone-900 text-sm'}>{queue.length}</strong></div>
          </div>
        </div>
      </div>

      {/* Main Simulation View: 3 Columns (Producers -> Queue -> Consumers) */}
      <div className="flex-1 flex flex-col lg:flex-row overflow-hidden min-h-[480px]">
        
        {/* Left Column: Producers */}
        <div className="w-full lg:w-1/4 lg:min-w-[280px] shrink-0 flex flex-col p-4 sm:p-6 border-b-2 lg:border-b-0 lg:border-r-2 border-stone-900 bg-[#fdfcfb]">
          <div className="flex justify-between items-center border-b-2 border-stone-300 pb-3 mb-4 gap-2">
            <h3 className="text-sm sm:text-base font-bold font-mono flex items-center gap-2">
              <Server size={18}/> Producers ({producerCount})
            </h3>
            <div className="flex gap-1.5 shrink-0">
              <Button onClick={handleRemoveProducer} variant="secondary" className="px-2 py-1 text-xs" disabled={producerCount === 0}>
                <Minus size={14}/>
              </Button>
              <Button onClick={handleAddProducer} className="px-2 py-1 text-xs">
                <Plus size={14}/>
              </Button>
            </div>
          </div>
          
          <div className="flex-1 overflow-y-auto flex flex-col gap-2.5 max-h-[300px] lg:max-h-none pr-1">
            {producersRef.current.map(p => (
              <div key={p.id} className="p-3 border-2 border-stone-900 bg-white flex justify-between items-center shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]">
                <span className="font-mono font-bold text-xs text-stone-900">{p.id}</span>
                <span className="text-[10px] font-mono text-emerald-700 flex items-center gap-1 font-bold">
                  <Activity size={12} className="animate-pulse text-emerald-500"/> Publishing
                </span>
              </div>
            ))}
            {producerCount === 0 && (
              <p className="text-stone-400 text-xs italic text-center py-6 font-mono">
                No active producers.
              </p>
            )}
          </div>
        </div>

        {/* Center: The Queue */}
        <div className="flex-1 flex flex-col justify-center relative min-h-[260px] lg:min-h-0 overflow-hidden bg-white p-4 sm:p-8 border-b-2 lg:border-b-0">
          <h4 className="absolute top-4 left-1/2 -translate-x-1/2 font-mono text-xs font-bold text-stone-400 tracking-widest uppercase">
            RabbitMQ FIFO Exchange
          </h4>
          <QueueView queue={queue} />
        </div>

        {/* Right: Consumers */}
        <div className="w-full lg:w-1/4 lg:min-w-[280px] shrink-0 flex flex-col p-4 sm:p-6 border-stone-900 lg:border-l-2 bg-[#fdfcfb]">
          <div className="flex justify-between items-center border-b-2 border-stone-300 pb-3 mb-4 gap-2">
            <h3 className="text-sm sm:text-base font-bold font-mono flex items-center gap-2">
              <Settings size={18}/> Consumers ({consumerCount})
            </h3>
            <div className="flex gap-1.5 shrink-0">
              <Button onClick={handleRemoveConsumer} variant="secondary" className="px-2 py-1 text-xs" disabled={consumerCount === 0}>
                <Minus size={14}/>
              </Button>
              <Button onClick={handleAddConsumer} variant="primary" className="px-2 py-1 text-xs">
                <Plus size={14}/>
              </Button>
            </div>
          </div>
          
          <div className="flex-1 overflow-y-auto flex flex-col gap-2.5 max-h-[300px] lg:max-h-none pr-1">
            {consumersRef.current.map(c => (
              <div key={c.id} className={`p-3 border-2 border-stone-900 ${c.processingMessage ? 'bg-orange-50 shadow-[3px_3px_0px_0px_rgba(249,115,22,0.4)]' : 'bg-white shadow-[2px_2px_0px_0px_rgba(28,25,23,1)]'} flex flex-col gap-2 transition-all duration-300`}>
                <div className="flex justify-between items-center">
                  <span className="font-mono font-bold text-xs text-stone-900">{c.id}</span>
                  <span className={`text-[9px] font-mono font-bold px-2 py-0.5 border border-stone-900 tracking-wider ${c.processingMessage ? 'bg-orange-500 text-stone-900' : 'bg-stone-200 text-stone-600'}`}>
                    {c.processingMessage ? 'WORKING' : 'IDLE'}
                  </span>
                </div>
                {c.processingMessage && (
                  <div className="text-xs font-mono text-stone-900 bg-white border border-stone-900 p-1.5 text-center">
                    Processing: <span className="font-bold text-orange-600">MSG-{c.processingMessage.id}</span>
                  </div>
                )}
              </div>
            ))}
            {consumerCount === 0 && (
              <p className="text-stone-400 text-xs italic text-center py-6 font-mono">
                No active consumers. Queue will back up!
              </p>
            )}
          </div>
        </div>

      </div>
    </div>
  );
}
