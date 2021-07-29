package me.nullicorn.ooze.storage;

/**
 * Helper methods for various binary operations.
 *
 * @author Nullicorn
 */
public final class BitUtils {

  /**
   * @return the minimum number of bits needed to represent the {@code value}.
   * @apiNote {@code 0} is considered to have a width of {@code 1}.
   */
  public static int widthInBits(int value) {
    if (value == 0) {
      return 1;
    }
    return Integer.SIZE - Integer.numberOfLeadingZeros(value);
  }

  /**
   * @return a bitmask with {@code numBits} consecutive bits set from the right side (least-sig).
   */
  public static int createBitMask(int numBits) {
    return (int) (1L << numBits) - 1;
  }

  /**
   * @return the number of bytes needed to store {@code bitCount} bits.
   */
  public static int bitsToBytes(int bitCount) {
    return (int) Math.ceil(bitCount / (double) Byte.SIZE);
  }

  private BitUtils() {
    throw new UnsupportedOperationException("BitUtils should not be instantiated");
  }
}
