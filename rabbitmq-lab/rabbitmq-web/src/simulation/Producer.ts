import { v4 as uuidv4 } from 'uuid';
import { Broker } from './Broker';

export class Producer {
  public id: string;
  private broker: Broker;
  private intervalId: number | null = null;
  public isRunning: boolean = true;
  
  constructor(id: string, broker: Broker) {
    this.id = id;
    this.broker = broker;
  }

  public start() {
    this.isRunning = true;
    this.intervalId = window.setInterval(() => {
      this.broker.publish({
        id: uuidv4().substring(0, 4).toUpperCase(),
        createdAt: Date.now()
      });
    }, 1000); // 1 message per second
  }

  public stop() {
    this.isRunning = false;
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }
}
