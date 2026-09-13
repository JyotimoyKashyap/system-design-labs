import { Broker } from './Broker';

export class Producer {
  public id: string;
  private broker: Broker;
  private intervalId: any = null;
  public isRunning: boolean = true;
  
  constructor(id: string, broker: Broker) {
    this.id = id;
    this.broker = broker;
  }

  public start() {
    this.isRunning = true;
    this.intervalId = setInterval(() => {
      this.broker.publish({
        id: Math.random().toString(36).substring(2, 6).toUpperCase(),
        createdAt: Date.now()
      });
    }, 1000);
  }

  public stop() {
    this.isRunning = false;
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }
}
