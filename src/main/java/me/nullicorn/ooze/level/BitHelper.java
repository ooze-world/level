package me.nullicorn.ooze.level;

import java.util.Arrays;
import java.util.BitSet;

/**
 * Helper methods for various binary operations.
 *
 * @author Nullicorn
 */
public final class BitHelper {

  /**
   * @return the minimum number of bits needed to represent the {@code value}.
   * @implSpec Negative numbers will always have a width of 32 ({@link Integer#SIZE}). Similarly,
   * zero will always have a width of {@code 0}.
   */
  public static int getWidthInBits(int value) {
    return Integer.SIZE - Integer.numberOfLeadingZeros(value);
  }

  /**
   * Creates a 32-bit mask. In that mask, the first {@code n} low-order bits are set, where {@code
   * n} is the value of {@code numberOfBits}.
   * <p><br>
   * <h3>Examples:</h3>
   * <pre>{@code
   *                  MSB                              LSB
   *                   |--------------------------------|
   * getMaskOver(0)  == 00000000000000000000000000000000
   * getMaskOver(1)  == 00000000000000000000000000000001
   * getMaskOver(10) == 00000000000000000000001111111111
   * getMaskOver(32) == 11111111111111111111111111111111
   * }</pre>
   *
   * @return a bitmask with {@code n} consecutive bits set from the right side (least-sig).
   */
  public static int getSizedMask(int numberOfBits) {
    return (int) (1L << numberOfBits) - 1;
  }

  /**
   * Determines the fewest number of bytes (octets) needed to represent {@code n} bits, where {@code
   * n = numberOfBits}.
   *
   * @param numberOfBits The number of bits in question.
   * @return the fewest number of bytes that could be used to represent the supplied {@code
   * numberOfBits}.
   */
  public static int getFewestBytes(int numberOfBits) {
    return (int) Math.ceil(numberOfBits / (double) Byte.SIZE);
  }

  /**
   * Copies the contents of a {@link BitSet} to {@code byte[]}.
   * <p>
   * By design, {@link BitSet#toByteArray()} only creates a byte array long enough to hold up to the
   * highest <b>set</b> bit. Any unset bits that are implicitly included get cut off.
   * <p>
   * This function avoids that by either padding the output array with zeros, or truncating it, in
   * order for it to contain exactly the desired number of bits, rounded up to the next octet/byte.
   * <p>
   * When truncating ({@code numberOfBits < bits.length()}), any extraneous bits—those that fit in
   * the last byte, but exceed the {@code numberOfBits}—will be replaced with zeros.
   * <p>
   * Bits in the resulting array are ordered in the same way specified by {@link
   * BitSet#toByteArray()}.
   *
   * @param bits         The BitSet to convert.
   * @param numberOfBits The number of bits to be copied to the array.
   * @return the BitSet's contents, as a sequence of octets.
   */
  public static byte[] getAsByteArray(BitSet bits, int numberOfBits) {
    byte[] asBytes = bits.toByteArray();

    // If applicable, trim or pad the byte array.
    int bytesNeeded = getFewestBytes(numberOfBits);
    if (asBytes.length != bytesNeeded) {
      asBytes = Arrays.copyOf(bits.toByteArray(), bytesNeeded);
    }

    // If applicable, unset any trailing bits in the last byte.
    if (numberOfBits < bits.length()) {
      int bitsToKeep = numberOfBits % Byte.SIZE;
      int keptBitsMask = getSizedMask(bitsToKeep);

      asBytes[asBytes.length - 1] &= keptBitsMask;
    }

    return asBytes;
  }

  private BitHelper() {
    throw new UnsupportedOperationException(getClass() + " should not be instantiated");
  }
}
