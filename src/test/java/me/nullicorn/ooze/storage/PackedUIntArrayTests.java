package me.nullicorn.ooze.storage;

import static me.nullicorn.ooze.storage.PackedUIntArray.bytesNeeded;
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
  void shouldConstructorsRejectNegativeSizes(int negativeSize) {
    Class<? extends Throwable> expectedException = NegativeArraySizeException.class;

    PackedUIntArrayData invalidData = PackedUIntArrayData.newBuilder()
        .setSize(negativeSize)
        .setMagnitude(5)
        .setContents(ByteString.EMPTY)
        .build();

    assertThrows(expectedException, () -> PackedUIntArray.fromProto(invalidData));
    assertThrows(expectedException, () -> new PackedUIntArray(negativeSize, 5));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 33})
  void shouldConstructorsRejectInvalidMagnitudes(int invalidMagnitude) {
    Class<? extends Throwable> expectedException = IllegalArgumentException.class;

    PackedUIntArrayData invalidData = PackedUIntArrayData.newBuilder()
        .setSize(1)
        .setMagnitude(invalidMagnitude)
        .setContents(ByteString.EMPTY)
        .build();

    assertThrows(expectedException, () -> PackedUIntArray.fromProto(invalidData));
    assertThrows(expectedException, () -> new PackedUIntArray(5, invalidMagnitude));
  }

  @Test
  void shouldConstructorInputsBeUsed() {
    int size = 23;
    int magnitude = 7;
    PackedUIntArray array = new PackedUIntArray(size, magnitude);

    assertEquals(size, array.size());
    assertEquals(magnitude, array.magnitude());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10})
  void shouldMaxValueCorrespondToMagnitude(int magnitude) {
    long expectedMax = (magnitude == 0)
        ? 0
        : 1L << magnitude;

    PackedUIntArray array = new PackedUIntArray(5, magnitude);
    assertEquals(expectedMax, array.max());
  }

  @Test
  void shouldAccessorsRejectNegativeIndices() {
    PackedUIntArray array = new PackedUIntArray(5, 5);

    assertThrows(ArrayIndexOutOfBoundsException.class, () -> array.get(-1));
    assertThrows(ArrayIndexOutOfBoundsException.class, () -> array.set(-1, 0));
  }

  @Test
  void shouldRetrieveCorrectValues() {
    int magnitude = 18;
    int size = 1 << magnitude;
    PackedUIntArray array = new PackedUIntArray(size, magnitude);

    // Set each value in the array to its index.
    for (int i = 0; i < size; i++) {
      array.set(i, i);
    }

    // Check that the correct values were set.
    // This is done separately from the first
    // loop in case a bug causes adjacent uints
    // to overwrite each other.
    for (int i = 0; i < size; i++) {
      assertEquals(i, array.get(i));
    }
  }

  @Test
  void shouldReplaceExistingValues() {
    int magnitude = 18;
    int size = 1 << magnitude;
    PackedUIntArray array = new PackedUIntArray(size, magnitude);

    // Set all of the array's bits to 1.
    // This way we make sure bits are cleared
    // first when replacing.
    for (int i = 0; i < size; i++) {
      array.set(i, size - 1);
    }

    // Set each value in the array to its index.
    // This is done separately from the first
    // loop to make sure adjacent replacements
    // don't interfere with one another.
    for (int i = 0; i < size; i++) {
      array.set(i, i);
    }

    // Check each value. This is separate from
    // the first two loops for the reason mentioned
    // above.
    for (int i = 0; i < size; i++) {
      assertEquals(i, array.get(i));
    }
  }

  @Test
  void shouldGenerateMatchingProto() {
    int magnitude = 18;
    int size = 1 << magnitude;

    PackedUIntArray array = new PackedUIntArray(size, magnitude);
    PackedUIntArrayData proto = array.toProto();

    assertEquals(array.size(), proto.getSize());
    assertEquals(array.magnitude(), proto.getMagnitude());
    assertEquals(bytesNeeded(size, magnitude), proto.getContents().size());
  }

  @Test
  void shouldTwoArraysEqualWhenExpected() {
    EqualsVerifier
        .forClass(PackedUIntArray.class)
        .withIgnoredFields("valueMask")
        .verify();
  }
}
