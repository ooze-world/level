package me.nullicorn.ooze.storage;

import com.github.ooze.protos.PackedUIntArrayData;
import com.google.protobuf.ByteString;
import java.util.Arrays;
import java.util.Objects;

/**
 * A binary data structure for packing unsigned integers (aka UInts) as closely as possible into
 * bytes.
 *
 * @author Nullicorn
 */
public class PackedUIntArray {

  /**
   * Helper function for construction.
   *
   * @return the correct length of the {@link #contents} field, given the array's {@code size} and
   * {@code magnitude}.
   */
  static int bytesNeeded(int size, long magnitude) {
    long bytesNeeded = Math.floorDiv(size * magnitude, Byte.SIZE) + 1;
    if (bytesNeeded != (int) bytesNeeded) {
      throw new IllegalArgumentException("(size * magnitude) overflows a byte array");
    }
    return (int) bytesNeeded;
  }

  /**
   * Capacity of the array.
   */
  private final int size;

  /**
   * Number of bits per uint.
   */
  private final int magnitude;

  /**
   * Internal storage for values. See .proto definition for more details.
   */
  private final byte[] contents;

  /**
   * Bitmask over the number of bits per uint (aka {@link #magnitude}).
   */
  private final int valueMask;

  /**
   * @param data Protocol buffer to copy from.
   */
  public PackedUIntArray(PackedUIntArrayData data) {
    this(data.getSize(), data.getMagnitude(), data.getContents().toByteArray());

    // Just to be safe, clear any extraneous trailing
    // bits on the last byte. If set, they would mess
    // up equals() and hashCode().
    if (magnitude != 0) {
      long lastBitOffset = magnitude * (size - 1L) - 1;
      contents[contents.length - 1] &= ~(valueMask << lastBitOffset);
    }
  }

  /**
   * @param size      Number of uints the array can hold.
   * @param magnitude Nmber of bits used to store each uint.
   */
  public PackedUIntArray(int size, int magnitude) {
    this(size, magnitude, new byte[bytesNeeded(size, magnitude)]);
  }

  private PackedUIntArray(int size, int magnitude, byte[] contents) {
    this.size = size;
    this.magnitude = magnitude;
    this.contents = contents;
    checkValid();

    valueMask = BitUtils.createBitMask(magnitude);
  }

  /**
   * Helper function for validating the array's fields after construction.
   */
  private void checkValid() {
    if (size < 0) {
      throw new NegativeArraySizeException(Integer.toString(size));
    } else if (magnitude < 0 || magnitude > Integer.SIZE) {
      throw new IllegalArgumentException("magnitude must be in range [0, 32]: " + magnitude);
    }

    int expectedBytes = bytesNeeded(size, magnitude);
    if (contents.length != expectedBytes) {
      throw new IllegalArgumentException(expectedBytes + " bytes expected, not " + contents.length);
    }
  }

  /**
   * @return the number of uints that the array can hold.
   */
  public int size() {
    return size;
  }

  /**
   * @return the number of bits used to store uints in the array.
   */
  public int magnitude() {
    return magnitude;
  }

  /**
   * @return the maximum value that can be stored in the array. Equivalent to {@code 1L <<
   * magnitude}, unless the magnitude is {@code 0}. In that case, {@code 0} is returned.
   * @see #magnitude() magnitude
   */
  public long max() {
    return magnitude == 0
        ? 0L
        : 1L << magnitude;
  }

  /**
   * @param index Zero-based index of the uint.
   * @return the uint value at the index.
   * @throws ArrayIndexOutOfBoundsException if the {@code index} is negative, or if it
   *                                        equals/exceeds the array's {@link #size() size}.
   */
  public int get(int index) {
    return getOrReplace(index, false, -1);
  }

  /**
   * @param index Zero-based index of the uint.
   * @param value UInt to replace the existing value with.
   * @return the uint previously at that index.
   * @throws ArrayIndexOutOfBoundsException if the {@code index} is negative, or if it
   *                                        equals/exceeds the array's {@link #size() size}.
   * @throws IllegalArgumentException       if the {@code value}, when unsigned, exceeds the array's
   *                                        {@link #max() maximum value}.
   */
  public int set(int index, int value) {
    return getOrReplace(index, true, value);
  }

  /**
   * Helper method for performing the {@code get} operation, and optionally {@code set} (aka
   * replace) as well.
   *
   * @param doReplace   {@code true} if this was called by a setter. {@code false} otherwise.
   * @param replacement Value to insert at the {@code index}.
   * @return the uint at the index, or the previous value if {@code doReplace == true}.
   * @throws ArrayIndexOutOfBoundsException if {@code index < 0 || index >= size}.
   * @throws IllegalArgumentException       if {@code doReplace && replacement > max}, when {@code
   *                                        replacement} is unsigned.
   * @implNote {@code get} and {@code set} are combined in this method because they share a lot of
   * the same bitwise logic. {@link #set(int, int) set()} also acts as a getter for the previous
   * value anyways, so it also saves time when setting.
   */
  private int getOrReplace(int index, boolean doReplace, int replacement) {
    if (index < 0 || index >= size) {
      throw new ArrayIndexOutOfBoundsException(index);
    } else if (doReplace && Integer.toUnsignedLong(replacement) > max()) {
      throw new IllegalArgumentException("value > max: " + replacement + " > " + max());
    } else if (magnitude == 0) {
      return 0;
    }

    // Returned at the end.
    int value = 0;

    // Overall "index" of the uint's first bit in the
    // content array. This is only needed to determine
    // where to start and stop iterating (startIndex &
    // endIndex).
    long startBit = (long) index * magnitude;

    // The uint's offset (in bits) within the current
    // byte. Always zero after the first byte because we
    // continue reading from the rightmost(least-sig)
    // bit of the next byte.
    int offset = (int) (startBit % Byte.SIZE);

    // # of bits of the uint we have so far. This is
    // increased variably with each iteration. Used to
    // determine how far new bits should be shifted onto the
    // return value.
    int bitsConsumed = 0;

    // Content array indices that we need to iterate between.
    int startIndex = (int) Math.floorDiv(startBit, Byte.SIZE);
    int endIndex = (int) Math.floorDiv(startBit + magnitude - 1, Byte.SIZE);

    for (int i = startIndex; i <= endIndex; i++) {
      value |= ((contents[i] & 0xFF) >>> offset) << bitsConsumed;

      if (doReplace) {
        contents[i] &= ~(valueMask << offset); // Clear the bits used by the uint.
        contents[i] |= (replacement << offset) >>> bitsConsumed; // Insert new value.
      }

      // Determine how many of the uint's bits came from
      // the current byte. The left expression represents
      // how many bits we *could* have read from the byte,
      // given our offset. The right expression represents
      // how many bits we still *need* to read overall.
      bitsConsumed += Math.min(Byte.SIZE - offset, magnitude - bitsConsumed);

      // If the loop continues, resume reading from the
      // rightmost (least-sig) bit of the next byte.
      offset = 0;
    }

    // Chop of any extra leading bits (most-sig) and return.
    return value & valueMask;
  }

  /**
   * @return a Protocol Buffer with the same {@link #size() size}, {@link #magnitude() magnitude}, and
   * contents as the array. The buffer can be passed to {@link #PackedUIntArray(PackedUIntArrayData)
   * this constructor} to create an identical array.
   */
  public PackedUIntArrayData toProto() {
    return PackedUIntArrayData.newBuilder()
        .setSize(size)
        .setMagnitude(magnitude)
        .setContents(ByteString.copyFrom(contents))
        .build();
  }

  @Override
  public String toString() {
    if (size == 0) {
      return "[]";
    }

    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < size; i++) {
      sb.append(get(i)).append(", ");
    }
    sb.replace(sb.length() - 2, sb.length() - 1, "]");

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PackedUIntArray array = (PackedUIntArray) o;
    return size == array.size &&
           magnitude == array.magnitude &&
           Arrays.equals(contents, array.contents);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(size, magnitude);
    result = 31 * result + Arrays.hashCode(contents);
    return result;
  }
}
