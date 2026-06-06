import { useState, useEffect, useRef } from 'react';
import { Broker } from './simulation/Broker';
import { Producer } from './simulation/Producer';
import { ConsumerGroup } from './simulation/ConsumerGroup';
import { PartitionGrid } from './components/PartitionGrid';
import { Plus, Minus, Server, Users, Settings } from 'lucide-react';

export default function App() {
  const brokerRef = useRef<Broker>(new Broker(3)); // 3 Partitions
  const broker = brokerRef.current;
  
  const [producer] = useState(() => new Producer(broker));
  const [consumerGroup] = useState(() => new ConsumerGroup('CG-1', broker));
  
  const [partitions, setPartitions] = useState([...broker.partitions]);
  const [offsets, setOffsets] = useState(new Map(consumerGroup.offsets));
  const [isProducing, setIsProducing] = useState(false);
  const [consumerCount, setConsumerCount] = useState(0);
  
  // Force re-renders
  const [, setTick] = useState(0);

  useEffect(() => {
    // Listen to broker changes (new messages)
    broker.onStateChange = () => {
      setPartitions([...broker.partitions]);
      setTick(t => t + 1);
    };

    // Listen to consumer group changes (offsets moving, rebalancing)
    consumerGroup.onStateChange = () => {
      setOffsets(new Map(consumerGroup.offsets));
      setTick(t => t + 1);
    };

    return () => {
      producer.stop();
      consumerGroup.stopAll();
    };
  }, []);

  const toggleProducer = () => {
    if (isProducing) {
      producer.stop();
      setIsProducing(false);
    } else {
      producer.start();
      setIsProducing(true);
    }
  };

  const handleAddConsumer = () => {
    consumerGroup.addConsumer();
    setConsumerCount(consumerGroup.consumers.length);
  };

  const handleRemoveConsumer = () => {
    consumerGroup.removeConsumer();
    setConsumerCount(consumerGroup.consumers.length);
  };

  return (
    <div className="min-h-screen bg-[#fdfcfb] text-stone-800 flex flex-col font-sans selection:bg-orange-100 selection:text-orange-900">
      {/* Header */}
      <header className="p-8 border-b border-stone-200 bg-white flex justify-between items-end">
        <div>
          <h1 className="text-4xl font-serif italic text-stone-900 mb-2 tracking-tight">Apache Kafka Visualizer</h1>
          <p className="text-stone-500 font-light max-w-3xl">
            Simulating an <strong>Append-Only Log</strong> with <strong>Consumer Groups</strong>. Messages are immutable and evenly partitioned. Consumers track their progress independently using offsets, and instantly rebalance partition ownership when you scale the group up or down!
          </p>
        </div>
      </header>

      {/* Main Board */}
      <main className="flex-1 flex flex-col xl:flex-row p-8 gap-8 overflow-hidden">
        
        {/* Left: Controls & Logs */}
        <div className="w-full xl:w-1/3 xl:min-w-[400px] shrink-0 flex flex-col gap-6">
          
          {/* Producer Panel */}
          <div className="border border-stone-200 bg-white p-6 shrink-0">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-bold flex items-center gap-2 whitespace-nowrap"><Server size={20}/> Producer</h2>
            </div>
            <p className="text-sm text-stone-500 mb-4">Continuously publish data to the Kafka topic. The broker will round-robin assign them to partitions.</p>
            <button 
              onClick={toggleProducer} 
              className={`w-full p-4 flex items-center justify-center gap-2 border-2 transition-colors font-bold ${isProducing ? 'bg-rose-100 border-rose-500 text-rose-900' : 'bg-stone-900 border-stone-900 text-white hover:bg-stone-800'}`}
            >
              {isProducing ? 'Stop Publishing' : 'Start Publishing Stream'}
            </button>
          </div>

          {/* Consumer Group Panel */}
          <div className="flex-1 border border-stone-200 bg-white p-6 flex flex-col min-h-0">
            <div className="flex justify-between items-center border-b border-stone-200 pb-4 mb-4 gap-4">
              <h2 className="text-xl font-bold flex items-center gap-2 whitespace-nowrap truncate"><Users size={20} className="shrink-0"/> Consumer Group: CG-1</h2>
              <div className="flex gap-2 shrink-0">
                <button onClick={handleRemoveConsumer} className="p-2 border border-stone-200 hover:bg-stone-50 transition-colors disabled:opacity-50" disabled={consumerCount === 0}><Minus size={14}/></button>
                <button onClick={handleAddConsumer} className="p-2 border border-stone-200 bg-orange-500 text-white hover:bg-orange-600 transition-colors"><Plus size={14}/></button>
              </div>
            </div>
            
            <p className="text-sm text-stone-500 mb-6">Scale workers in the group. Kafka restricts 1 consumer to 1 partition to guarantee ordering. Watch the rebalance!</p>

            <div className="flex-1 overflow-y-auto flex flex-col gap-3">
              {consumerGroup.consumers.map(c => (
                <div key={c.id} className="p-4 border border-stone-200 bg-stone-50 flex flex-col gap-2">
                  <div className="flex justify-between items-center">
                    <span className="font-mono font-bold text-stone-700">{c.id}</span>
                    <span className="text-xs font-mono px-2 py-1 bg-stone-200 text-stone-600">
                      PARTITIONS: [{c.assignedPartitions.map(p => p.id).join(', ')}]
                    </span>
                  </div>
                  {c.processingMsg ? (
                    <div className="text-xs font-mono text-emerald-700 flex items-center gap-2">
                      <Settings size={12} className="animate-spin"/> Reading MSG-{c.processingMsg.payload.split('-')[1]} (Offset {c.processingMsg.offset})
                    </div>
                  ) : (
                    <div className="text-xs font-mono text-stone-400">WAITING FOR DATA...</div>
                  )}
                </div>
              ))}
              {consumerCount === 0 && <p className="text-stone-400 text-sm italic text-center py-8">No consumers in group.</p>}
            </div>
          </div>

        </div>

        {/* Right: The Partition Grid */}
        <div className="flex-1 flex flex-col min-h-[500px] bg-white border border-stone-200 p-8 shadow-sm">
          <h2 className="text-2xl font-bold text-stone-900 mb-8 border-b border-stone-200 pb-4">Topic: "events-log"</h2>
          <PartitionGrid partitions={partitions} consumerOffsets={offsets} />
        </div>
        
      </main>
    </div>
  );
}
