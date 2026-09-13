export class HashProvider {
  /**
   * FNV-1a Hash
   */
  static hash1(str: string, max: number): number {
    let hash = 2166136261;
    for (let i = 0; i < str.length; i++) {
      hash ^= str.charCodeAt(i);
      hash = Math.imul(hash, 16777619);
    }
    return Math.abs(hash) % max;
  }

  /**
   * DJB2 Hash
   */
  static hash2(str: string, max: number): number {
    let hash = 5381;
    for (let i = 0; i < str.length; i++) {
      hash = Math.imul(hash, 33) ^ str.charCodeAt(i);
    }
    return Math.abs(hash) % max;
  }

  /**
   * SDBM Hash
   */
  static hash3(str: string, max: number): number {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = str.charCodeAt(i) + (hash << 6) + (hash << 16) - hash;
    }
    return Math.abs(hash) % max;
  }

  /**
   * Returns k=3 hash indices for a given string
   */
  static getHashes(str: string, max: number): number[] {
    return [
      this.hash1(str, max),
      this.hash2(str, max),
      this.hash3(str, max)
    ];
  }
}
