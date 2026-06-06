import { Broker, Message } from './Broker';

export class Consumer {
  public id: string;
  private broker: Broker;
  private pollingId: number | null = null;
  public processingMessage: Message | null = null;
  public isRunning: boolean = true;
  
  public onStateChange?: () => void;

  constructor(id: string, broker: Broker) {
    this.id = id;
    this.broker = broker;
  }

  public start() {
    this.isRunning = true;
    this.poll();
  }

  private poll() {
    if (!this.isRunning) return;
    
    // Attempt to grab a message
    const msg = this.broker.consume();
    if (msg) {
      this.processingMessage = msg;
      this.notify();
      
      // Simulate processing time (3000ms) - Slower than producer so queue builds up
      setTimeout(() => {
        if (!this.isRunning) return;
        this.broker.ack();
        this.processingMessage = null;
        this.notify();
        // Immediately poll for next message
        this.poll();
      }, 3000);
    } else {
      // No messages, backoff and poll again
      this.pollingId = window.setTimeout(() => this.poll(), 200);
    }
  }

  public stop() {
    this.isRunning = false;
    if (this.pollingId) clearTimeout(this.pollingId);
  }

  private notify() {
    if (this.onStateChange) this.onStateChange();
  }
}
