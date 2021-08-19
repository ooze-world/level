package me.nullicorn.ooze.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author Nullicorn
 */
class PaletteTests {

  @Test
  void constructor_shouldRejectNulls() {
    // Name is null.
    assertThrows(IllegalArgumentException.class, () ->
        new Palette(null, 0, Collections.emptyList()));

    // State list is null.
    assertThrows(IllegalArgumentException.class, () ->
        new Palette("name", 0, null));

    // State list contains null.
    assertThrows(IllegalArgumentException.class, () ->
        new Palette("name", 0, Collections.singletonList(null)));
  }

  @Test
  void constructor_shouldAllArgsBeUsed() {
    String expectedName = "test_palette";
    int expectedVersion = 29;
    List<BlockState> expectedStates = generateStates(14);

    Palette palette = new Palette(expectedName, expectedVersion, expectedStates);

    assertEquals(expectedName, palette.getName());
    assertEquals(expectedVersion, palette.getDataVersion());

    // Make sure the palette has all expected states.
    assertEquals(expectedStates.size(), palette.size());
    palette.forEach(blockState -> assertTrue(expectedStates.contains(blockState)));
  }

  @Test
  void constructor_shouldStateListBeImmutable() {
    List<BlockState> states = generateStates(10);

    Palette palette = new Palette("test_palette", 42, states);
    states.clear();

    assertNotEquals(0, palette.size());
  }

  @Test
  void isEmpty_shouldReturnTrueWhenSizeIsZero() {
    Palette emptyPalette = generatePalette(0);
    assertTrue(emptyPalette.isEmpty());
    assertEquals(0, emptyPalette.size());

    Palette nonEmptyPalette = generatePalette(1);
    assertFalse(nonEmptyPalette.isEmpty());
    assertNotEquals(0, nonEmptyPalette.size());
  }

  @Test
  void shouldIndicesBePreserved() {
    List<BlockState> states = generateStates(100);
    Palette palette = new Palette("test_palette", 42, states);

    for (int i = 0; i < states.size(); i++) {
      assertEquals(states.get(i), palette.get(i), "i=" + i);
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 4, 5, 8, 16})
  void magnitude_shouldCorrespondToSize(int magnitude) {
    int numberOfStates = 1 + (1 << (magnitude - 1));
    Palette palette = generatePalette(numberOfStates);

    assertEquals(magnitude, palette.magnitude());
  }

  @Test
  void equals_shouldTwoPalettesEqualWhenExpected() {
    EqualsVerifier
        .forClass(Palette.class)
        .withNonnullFields("name", "states")
        .verify();
  }

  /**
   * Same as the overloaded method, but an arbitrary name and data-version are used for the returned
   * palette.
   *
   * @see #generatePalette(String, int, int)
   */
  private static Palette generatePalette(int numberOfStates) {
    return generatePalette("test_palette", 42, numberOfStates);
  }

  /**
   * Generates a new palette of unique block states.
   *
   * @param name           A unique name for the palette.
   * @param dataVersion    The Minecraft data-version that the palette should be compatible with.
   * @param numberOfStates The number of unique states that the palette will have.
   * @return the generated palette.
   */
  private static Palette generatePalette(String name, int dataVersion, int numberOfStates) {
    return new Palette(name, dataVersion, generateStates(numberOfStates));
  }

  /**
   * Generates a list of block states, each unique from one another.
   *
   * @param numberOfStates The number of states to generate, which is also the size of the returned
   *                       list.
   * @return the generated states.
   */
  private static List<BlockState> generateStates(int numberOfStates) {
    List<BlockState> states = new ArrayList<>(numberOfStates);
    for (int i = 0; i < numberOfStates; i++) {
      states.add(new BlockState("test_state_" + i));
    }
    return states;
  }
}
