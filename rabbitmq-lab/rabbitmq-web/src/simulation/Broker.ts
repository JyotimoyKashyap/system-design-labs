export interface Message {
  id: string;
  createdAt: number;
}

export class Broker {
  public queue: Message[] = [];
  public processedCount: number = 0;
  
  public onStateChange?: () => void;

  public publish(msg: Message) {
    // Prevent unbounded growth in the browser to avoid crashing
    if (this.queue.length > 50) return;
    this.queue.push(msg);
    this.notify();
  }

  public consume(): Message | null {
    if (this.queue.length > 0) {
      const msg = this.queue.shift();
      this.notify();
      return msg || null;
    }
    return null;
  }

  public ack() {
    this.processedCount++;
    this.notify();
  }

  private notify() {
    if (this.onStateChange) this.onStateChange();
  }
}
