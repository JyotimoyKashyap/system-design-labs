/**
 * Simple 32-bit FNV-1a hash function.
 * It's computationally inexpensive and good for distributing keys evenly across the ring.
 * 
 * @param key The string to hash
 * @param ringSize The size of the consistent hashing ring
 * @returns A positive integer modulo ringSize
 */
export function fnv1aHash(key: string, ringSize: number): number {
  let hash = 2166136261; // FNV offset basis
  for (let i = 0; i < key.length; i++) {
    hash ^= key.charCodeAt(i);
    hash += (hash << 1) + (hash << 4) + (hash << 7) + (hash << 8) + (hash << 24);
  }
  // Convert to 32-bit unsigned integer
  hash = hash >>> 0;
  return hash % ringSize;
}
