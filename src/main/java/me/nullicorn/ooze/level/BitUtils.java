package me.nullicorn.ooze.level;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Helper methods for various binary operations.
 *
 * @author Nullicorn
 */
final class BitUtils {

  /**
   * @return the minimum number of bits needed to represent the {@code value}.
   */
  public static int widthInBits(int value) {
    return Integer.SIZE - Integer.numberOfLeadingZeros(value);
  }

  /**
   * @return a bitmask with {@code numBits} consecutive bits set from the right side (least-sig).
   */
  public static int createBitMask(int numBits) {
    return (int) (1L << numBits) - 1;
  }

  /**
   * @return the lowest number of bytes needed to hold the {@code amount} of bits.
   */
  public static int bitsNeeded(int amount) {
    return (int) Math.ceil(amount / (double) Byte.SIZE);
  }

  /**
   * @return The result of {@link BitSet#toByteArray()}. The returned array is trimmed or padded
   * with {@code 0}s until it has however many bytes are needed to hold {@code size} bits.
   */
  public static byte[] bitsToBytes(BitSet bits, int size) {
    int sizeInBytes = bitsNeeded(size);
    return Arrays.copyOf(bits.toByteArray(), sizeInBytes);
  }

  private BitUtils() {
    throw new UnsupportedOperationException("BitUtils should not be instantiated");
  }
}
