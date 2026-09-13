export interface Message {
  id: string;
  createdAt: number;
}

export class Broker {
  public queue: Message[] = [];
  public processedCount: number = 0;
  
  public onStateChange?: () => void;

  public publish(msg: Message) {
    if (this.queue.length > 50) return;
    this.queue.push(msg);
    this.notify();
  }

  public consume(): Message | null {
    if (this.queue.length > 0) {
      const oldestMessage = this.queue[0];
      if (Date.now() - oldestMessage.createdAt >= 600) {
        const msg = this.queue.shift();
        this.notify();
        return msg || null;
      }
    }
    return null;
  }

  public ack() {
    this.processedCount++;
    this.notify();
  }

  public reset() {
    this.queue = [];
    this.processedCount = 0;
    this.notify();
  }

  private notify() {
    if (this.onStateChange) this.onStateChange();
  }
}
