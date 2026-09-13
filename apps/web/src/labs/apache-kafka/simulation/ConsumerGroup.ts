import { Broker, Partition } from './Broker';
import type { Message } from './types';

export class Consumer {
  public id: string;
  public assignedPartitions: Partition[] = [];
  private group: ConsumerGroup;
  private isRunning: boolean = false;
  private timerId: any = null;
  public processingMsg: Message | null = null;

  constructor(id: string, group: ConsumerGroup) {
    this.id = id;
    this.group = group;
  }

  public start() {
    this.isRunning = true;
    this.poll();
  }

  public stop() {
    this.isRunning = false;
    if (this.timerId) clearTimeout(this.timerId);
  }

  private poll() {
    if (!this.isRunning) return;

    let foundMsg: Message | null = null;
    for (const p of this.assignedPartitions) {
      const currentOffset = this.group.offsets.get(p.id)!;
      const nextMsg = p.messages.find(m => m.offset === currentOffset + 1);
      if (nextMsg) {
        foundMsg = nextMsg;
        break;
      }
    }

    if (foundMsg) {
      this.processingMsg = foundMsg;
      this.group.notify();
      
      this.timerId = setTimeout(() => {
        if (!this.isRunning || !this.processingMsg) return;
        this.group.commitOffset(this.processingMsg.partition, this.processingMsg.offset);
        this.processingMsg = null;
        this.group.notify();
        this.poll();
      }, 1500);
    } else {
      this.timerId = setTimeout(() => this.poll(), 500);
    }
  }
}

export class ConsumerGroup {
  public id: string;
  public consumers: Consumer[] = [];
  public offsets: Map<number, number> = new Map();
  private broker: Broker;
  public onStateChange?: () => void;

  constructor(id: string, broker: Broker) {
    this.id = id;
    this.broker = broker;
    broker.partitions.forEach(p => this.offsets.set(p.id, -1)); 
  }

  public addConsumer() {
    const c = new Consumer(`${this.id}-${this.consumers.length + 1}`, this);
    this.consumers.push(c);
    this.rebalance();
    c.start();
    this.notify();
  }

  public removeConsumer() {
    if (this.consumers.length === 0) return;
    const c = this.consumers.pop();
    c?.stop();
    this.rebalance();
    this.notify();
  }

  public stopAll() {
    this.consumers.forEach(c => c.stop());
  }

  private rebalance() {
    if (this.consumers.length === 0) return;
    this.consumers.forEach(c => c.assignedPartitions = []);
    let cIdx = 0;
    this.broker.partitions.forEach(p => {
      this.consumers[cIdx].assignedPartitions.push(p);
      cIdx = (cIdx + 1) % this.consumers.length;
    });
  }

  public commitOffset(partitionId: number, offset: number) {
    this.offsets.set(partitionId, offset);
    this.notify();
  }

  public notify() {
    if (this.onStateChange) this.onStateChange();
  }
}
