import { Broker, type Message } from './Broker';

export class Consumer {
  public id: string;
  private broker: Broker;
  private pollingId: any = null;
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
    
    const msg = this.broker.consume();
    if (msg) {
      this.processingMessage = msg;
      this.notify();
      
      setTimeout(() => {
        if (!this.isRunning) return;
        this.broker.ack();
        this.processingMessage = null;
        this.notify();
        this.poll();
      }, 3000);
    } else {
      this.pollingId = setTimeout(() => this.poll(), 200);
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
