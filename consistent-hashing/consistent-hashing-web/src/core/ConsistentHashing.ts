import { fnv1aHash } from './HashUtils';

export interface StorageNode {
  name: string;
  ip: string;
  hash: number;
}

export class ConsistentHashing {
  public nodes: StorageNode[];
  public keys: number[]; // Sorted list of hashes representing node positions
  public totalSlots: number;

  constructor(initialNodes: Omit<StorageNode, 'hash'>[], totalSlots: number) {
    this.keys = [];
    this.nodes = [];
    this.totalSlots = totalSlots;

    for (const node of initialNodes) {
      this.addNode(node);
    }
  }

  public addNode(nodeInfo: Omit<StorageNode, 'hash'>): StorageNode {
    if (this.nodes.length >= this.totalSlots) {
      throw new Error("The hash ring is completely full");
    }

    let hash = fnv1aHash(nodeInfo.ip, this.totalSlots);

    // Linear probing if there's a collision
    while (this.keys.includes(hash)) {
      hash = (hash + 1) % this.totalSlots;
    }

    const newNode: StorageNode = { ...nodeInfo, hash };

    // Find the correct position to insert the hash (maintain sorted order)
    let index = this.keys.findIndex((k) => k > hash);
    if (index === -1) {
      index = this.keys.length; // Insert at the end
    }

    this.keys.splice(index, 0, hash);
    this.nodes.splice(index, 0, newNode);
    return newNode;
  }

  public removeNode(node: StorageNode): void {
    const index = this.nodes.findIndex((n) => n.hash === node.hash);
    if (index >= 0) {
      this.keys.splice(index, 1);
      this.nodes.splice(index, 1);
    }
  }

  public getTargetNode(dataKey: string): { node: StorageNode; keyHash: number } | null {
    if (this.keys.length === 0) {
      return null;
    }

    const keyHash = fnv1aHash(dataKey, this.totalSlots);

    // Find the first node whose hash is greater than or equal to the key's hash
    let index = this.keys.findIndex((k) => k >= keyHash);

    // If no such node exists, it wraps around to the first node
    if (index === -1 || index === this.keys.length) {
      index = 0;
    }

    return { node: this.nodes[index], keyHash };
  }
}
