import type { RPCMessage } from './types';
import { Node } from './Node';

export class Network {
  private nodes: Map<string, Node> = new Map();
  public onMessageSent?: (msg: RPCMessage, latency: number) => void;

  registerNode(node: Node) {
    this.nodes.set(node.id, node);
  }

  send(message: RPCMessage) {
    // Slower latency for beautiful UI visualization (400ms - 800ms)
    const latency = Math.floor(Math.random() * 400) + 400;
    
    if (this.onMessageSent) this.onMessageSent(message, latency);
    
    setTimeout(() => {
      const recipient = this.nodes.get(message.toId);
      if (recipient && recipient.getState() !== 'DEAD') {
        recipient.receiveMessage(message);
      }
    }, latency);
  }

  broadcast(messageFn: (toId: string) => RPCMessage, fromId: string) {
    this.nodes.forEach((_, toId) => {
      if (toId !== fromId) {
        this.send(messageFn(toId));
      }
    });
  }
}
