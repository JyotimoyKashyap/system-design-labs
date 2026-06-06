import type { Message } from './types';

export class Partition {
  public id: number;
  public messages: Message[] = [];
  public nextOffset: number = 0;

  constructor(id: number) { 
    this.id = id; 
  }

  append(payload: string): Message {
    const msg = {
      id: Math.random().toString(36).substring(2, 6).toUpperCase(),
      partition: this.id,
      offset: this.nextOffset++,
      payload
    };
    this.messages.push(msg);
    
    // UI Constraint: Only keep the last 30 messages in memory so the visual queue 
    // doesn't scroll infinitely into oblivion, mimicking log retention policies.
    if (this.messages.length > 30) {
      this.messages.shift();
    }
    return msg;
  }
}

export class Broker {
  public partitions: Partition[] = [];
  public onStateChange?: () => void;

  constructor(numPartitions: number = 3) {
    for (let i = 0; i < numPartitions; i++) {
      this.partitions.push(new Partition(i));
    }
  }

  public publish(payload: string) {
    // Simulated round-robin or random partitioning
    const partitionIndex = Math.floor(Math.random() * this.partitions.length);
    this.partitions[partitionIndex].append(payload);
    this.notify();
  }

  private notify() {
    if (this.onStateChange) this.onStateChange();
  }
}
