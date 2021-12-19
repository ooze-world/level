package me.nullicorn.ooze.level;

import static me.nullicorn.ooze.level.PackedUIntArray.bytesNeeded;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.ooze.protos.PackedUIntArrayData;
import com.google.protobuf.ByteString;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Nullicorn
 */
class PackedUIntArrayTests {

  @ParameterizedTest
  @ValueSource(ints = {-1})
  void constructor_shouldRejectNegativeSizes(int negativeSize) {
    Class<? extends Throwable> expectedException = NegativeArraySizeException.class;

    PackedUIntArrayData invalidData = PackedUIntArrayData.newBuilder()
        .setSize(negativeSize)
        .setMagnitude(5)
        .setContents(ByteString.EMPTY)
        .build();

    assertThrows(expectedException, () -> PackedUIntArray.fromProto(invalidData));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 33})
  void constructor_shouldRejectInvalidMagnitudes(int invalidMagnitude) {
    Class<? extends Throwable> expectedException = IllegalArgumentException.class;

    PackedUIntArrayData invalidData = PackedUIntArrayData.newBuilder()
        .setSize(1)
        .setMagnitude(invalidMagnitude)
        .setContents(ByteString.EMPTY)
        .build();

    assertThrows(expectedException, () -> PackedUIntArray.fromProto(invalidData));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 5, 10, 64, 1000, 4096, Short.MAX_VALUE})
  void size_shouldMatchConstructorValue(int size) {
    PackedUIntArray actual = new PackedUIntArray(new int[size]);
    assertEquals(size, actual.size());
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 4, 5, 8, 16, 24, 32})
  void magnitude_shouldMatchConstructorValue(int magnitude) {
    int maxValue = (int) ((1L << magnitude) - 1);

    int[] input = new int[100];
    input[0] = maxValue;

    PackedUIntArray actual = new PackedUIntArray(input);
    assertEquals(magnitude, actual.magnitude());
    assertEquals(Integer.toUnsignedLong(maxValue), actual.max());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, Byte.MIN_VALUE, Short.MIN_VALUE, Integer.MIN_VALUE})
  void get_shouldRejectNegativeIndices(int index) {
    PackedUIntArray array = new PackedUIntArray();
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> array.get(index));
  }

  @Test
  void get_shouldReturnCorrectValues() {
    int magnitude = 18;
    int size = 1 << magnitude;

    // Set each value in the array to its index.
    int[] expected = new int[size];
    for (int i = 0; i < size; i++) {
      expected[i] = i;
    }

    PackedUIntArray actual = new PackedUIntArray(expected);

    // Check that the correct values were set.
    // This is done separately from the first
    // loop in case a bug causes adjacent uints
    // to overwrite each other.
    for (int i = 0; i < size; i++) {
      assertEquals(i, actual.get(i));
    }
  }

  @Test
  void toProto_shouldMatchArray() {
    int magnitude = 18;
    int size = 1 << magnitude;

    int[] expected = new int[size];
    for (int i = 0; i < size; i++) {
      expected[i] = size - 1;
    }

    PackedUIntArray actual = new PackedUIntArray(expected);
    PackedUIntArrayData proto = actual.toProto();

    assertEquals(actual.size(), proto.getSize());
    assertEquals(actual.magnitude(), proto.getMagnitude());
    assertEquals(bytesNeeded(size, magnitude), proto.getContents().size());
    assertEquals(actual, PackedUIntArray.fromProto(proto));
  }

  @Test
  void equals_shouldTwoArraysEqualWhenExpected() {
    EqualsVerifier
        .forClass(PackedUIntArray.class)
        .withIgnoredFields("valueMask")
        .verify();
  }
}
