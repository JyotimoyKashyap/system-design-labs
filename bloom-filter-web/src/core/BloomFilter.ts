import { HashProvider } from './HashProvider';

export class BloomFilter {
  public size: number;
  public bitArray: boolean[];
  public itemsAdded: number = 0;

  constructor(size: number = 128) {
    this.size = size;
    this.bitArray = new Array(size).fill(false);
  }

  /**
   * Adds a string to the bloom filter.
   * Returns the array indices that were computed.
   */
  add(item: string): number[] {
    const indices = HashProvider.getHashes(item, this.size);
    indices.forEach(idx => {
      this.bitArray[idx] = true;
    });
    this.itemsAdded++;
    return indices;
  }

  /**
   * Checks if a string is present.
   * Returns { present, indices } where indices are the hash locations checked.
   */
  check(item: string): { present: boolean; indices: number[] } {
    const indices = HashProvider.getHashes(item, this.size);
    const present = indices.every(idx => this.bitArray[idx]);
    return { present, indices };
  }

  reset() {
    this.bitArray = new Array(this.size).fill(false);
    this.itemsAdded = 0;
  }
}
