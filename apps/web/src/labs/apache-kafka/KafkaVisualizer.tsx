import { useState, useEffect, useRef } from 'react';
import { Broker } from './simulation/Broker';
import { Producer } from './simulation/Producer';
import { ConsumerGroup } from './simulation/ConsumerGroup';
import { PartitionGrid } from './components/PartitionGrid';
import { Plus, Minus, Server, Users, Settings } from 'lucide-react';
import { Button } from '@repo/ui';

export default function KafkaVisualizer() {
  const brokerRef = useRef<Broker>(new Broker(3));
  const broker = brokerRef.current;
  
  const [producer] = useState(() => new Producer(broker));
  const [consumerGroup] = useState(() => new ConsumerGroup('CG-1', broker));
  
  const [partitions, setPartitions] = useState([...broker.partitions]);
  const [offsets, setOffsets] = useState(new Map(consumerGroup.offsets));
  const [isProducing, setIsProducing] = useState(false);
  const [consumerCount, setConsumerCount] = useState(0);
  
  const [, setTick] = useState(0);

  useEffect(() => {
    broker.onStateChange = () => {
      setPartitions([...broker.partitions]);
      setTick(t => t + 1);
    };

    consumerGroup.onStateChange = () => {
      setOffsets(new Map(consumerGroup.offsets));
      setTick(t => t + 1);
    };

    return () => {
      producer.stop();
      consumerGroup.stopAll();
    };
  }, [broker, consumerGroup, producer]);

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
    <div className="w-full h-full bg-[#fdfcfb] text-stone-800 font-sans selection:bg-orange-100 selection:text-orange-900 flex flex-col lg:flex-row overflow-y-auto lg:overflow-hidden">
      
      {/* Left Column: Producer & Consumer Group Controls (38%) */}
      <div className="w-full lg:w-[38%] h-full flex flex-col p-6 sm:p-10 border-b-2 lg:border-b-0 lg:border-r-2 border-stone-900 bg-[#fdfcfb] overflow-y-auto shrink-0 justify-between">
        <div>
          <span className="inline-block px-2.5 py-0.5 font-mono text-[10px] font-black uppercase tracking-wider bg-orange-600 text-white border border-stone-900 shadow-[2px_2px_0px_0px_rgba(28,25,23,1)] mb-4">
            Event Streaming &amp; Partitioning
          </span>
          <h2 className="text-2xl sm:text-3xl font-silkscreen not-italic text-stone-900 mb-2 tracking-tight">
            Apache Kafka
          </h2>
          <p className="text-stone-600 text-sm leading-relaxed mb-6 font-medium">
            Simulating an <strong>Append-Only Log</strong> with <strong>Consumer Groups</strong>. Messages are immutable and evenly partitioned. Consumers track their progress independently using offsets, and instantly rebalance partition ownership when you scale the group up or down!
          </p>

          {/* Producer Panel */}
          <div className="border-2 border-stone-900 bg-white p-5 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)] mb-6">
            <div className="flex justify-between items-center mb-2">
              <h3 className="text-base font-bold font-mono flex items-center gap-2">
                <Server size={18} /> Stream Producer
              </h3>
            </div>
            <p className="text-xs text-stone-600 mb-4 font-mono">
              Continuously publish data to the Kafka topic. The broker round-robins messages into 3 partitions.
            </p>
            <Button 
              onClick={toggleProducer} 
              variant={isProducing ? "secondary" : "default"}
              className={`w-full ${isProducing ? '!bg-rose-500 hover:!bg-rose-600 !text-white' : ''}`}
            >
              {isProducing ? 'Stop Publishing' : 'Start Publishing Stream'}
            </Button>
          </div>

          {/* Consumer Group Panel */}
          <div className="border-2 border-stone-900 bg-white p-5 shadow-[4px_4px_0px_0px_rgba(28,25,23,1)]">
            <div className="flex justify-between items-center border-b-2 border-stone-200 pb-3 mb-3 gap-2">
              <h3 className="text-base font-bold font-mono flex items-center gap-2 truncate">
                <Users size={18} className="shrink-0" /> Group: CG-1
              </h3>
              <div className="flex gap-1.5 shrink-0">
                <Button onClick={handleRemoveConsumer} variant="secondary" className="px-2.5 py-1.5 text-xs" disabled={consumerCount === 0}>
                  <Minus size={14} />
                </Button>
                <Button onClick={handleAddConsumer} variant="primary" className="px-2.5 py-1.5 text-xs">
                  <Plus size={14} />
                </Button>
              </div>
            </div>
            
            <p className="text-xs text-stone-600 mb-4 font-mono">
              Scale consumers up or down. Kafka guarantees exactly 1 consumer per partition within a group.
            </p>

            <div className="max-h-[220px] overflow-y-auto flex flex-col gap-2">
              {consumerGroup.consumers.map(c => (
                <div key={c.id} className="p-3 border-2 border-stone-900 bg-stone-50 flex flex-col gap-1.5">
                  <div className="flex justify-between items-center">
                    <span className="font-mono text-xs font-bold text-stone-900">{c.id}</span>
                    <span className="text-[10px] font-mono px-2 py-0.5 bg-stone-200 border border-stone-400 text-stone-800 font-bold">
                      Partitions: [{c.assignedPartitions.map(p => p.id).join(', ')}]
                    </span>
                  </div>
                  {c.processingMsg ? (
                    <div className="text-[11px] font-mono text-emerald-800 font-bold flex items-center gap-1.5">
                      <Settings size={12} className="animate-spin text-emerald-600"/> Reading MSG-{c.processingMsg.payload.split('-')[1]} (Offset {c.processingMsg.offset})
                    </div>
                  ) : (
                    <div className="text-[11px] font-mono text-stone-400">WAITING FOR NEXT COMMIT...</div>
                  )}
                </div>
              ))}
              {consumerCount === 0 && (
                <p className="text-stone-400 font-mono text-xs italic text-center py-4">
                  No active consumers in CG-1. Click &quot;+&quot; to spawn a consumer.
                </p>
              )}
            </div>
          </div>
        </div>

        <div className="mt-6 pt-4 border-t-2 border-dashed border-stone-300 font-mono text-xs text-stone-500">
          Partition offsets are persisted in <code>__consumer_offsets</code> for zero-data-loss checkpointing.
        </div>
      </div>

      {/* Right Column: Partition Grid (62%) */}
      <div className="flex-1 w-full h-full relative flex flex-col justify-center bg-[#fdfcfb] p-6 sm:p-10 overflow-y-auto min-h-[480px]">
        <div className="w-full max-w-4xl mx-auto bg-white border-2 border-stone-900 p-6 sm:p-8 shadow-[6px_6px_0px_0px_rgba(28,25,23,1)]">
          <div className="flex justify-between items-center mb-6 pb-3 border-b-2 border-stone-900">
            <h3 className="font-silkscreen not-italic text-lg text-stone-900 font-bold">
              Topic: &quot;events-stream&quot;
            </h3>
            <span className="font-mono text-xs font-bold bg-amber-100 border border-stone-900 px-2 py-0.5">
              3 Partitions
            </span>
          </div>
          <PartitionGrid partitions={partitions} consumerOffsets={offsets} />
        </div>
      </div>
      
    </div>
  );
}
