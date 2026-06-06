import { Network } from './Network';
import { Node } from './Node';
import type { NodeState } from './types';

export class ClusterManager {
  public network: Network;
  public nodes: Node[] = [];

  public onNodeStateChange?: (id: string, state: NodeState, term: number) => void;
  public onNodeTimerReset?: (id: string, duration: number) => void;

  constructor(size: number) {
    this.network = new Network();

    for (let i = 1; i <= size; i++) {
      const node = new Node(`Node-${i}`, this.network, size);
      
      node.onStateChange = (id, state, term) => {
        if (this.onNodeStateChange) this.onNodeStateChange(id, state, term);
      };

      node.onTimerReset = (id, duration) => {
        if (this.onNodeTimerReset) this.onNodeTimerReset(id, duration);
      };

      this.network.registerNode(node);
      this.nodes.push(node);
    }
  }

  public start() {
    this.nodes.forEach(node => node.start());
  }

  public killNode(id: string) {
    const node = this.nodes.find(n => n.id === id);
    if (node) node.kill();
  }

  public reviveNode(id: string) {
    const node = this.nodes.find(n => n.id === id);
    if (node) node.revive();
  }
}
