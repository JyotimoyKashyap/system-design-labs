import { Broker } from './Broker';

export class Producer {
  private broker: Broker;
  private intervalId: any = null;
  public isRunning: boolean = true;
  private msgCount = 0;

  constructor(broker: Broker) {
    this.broker = broker;
  }

  public start() {
    this.isRunning = true;
    this.intervalId = setInterval(() => {
      this.msgCount++;
      this.broker.publish(`DATA-${this.msgCount}`);
    }, 800);
  }

  public stop() {
    this.isRunning = false;
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }
}
