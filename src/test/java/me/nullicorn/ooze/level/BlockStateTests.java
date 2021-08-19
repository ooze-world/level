package me.nullicorn.ooze.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import me.nullicorn.nedit.type.NBTCompound;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

/**
 * @author Nullicorn
 */
class BlockStateTests {

  // TODO: 7/29/21 Add tests for proto conversion.

  @Test
  void constructor_shouldFailIfNameIsNull() {
    assertThrows(IllegalArgumentException.class, () -> new BlockState(null));
  }

  @Test
  void constructor_shouldFailIfPropertiesAreNull() {
    assertThrows(IllegalArgumentException.class, () -> new BlockState("ooze_test_state", null));
  }

  @Test
  void constructor_shouldAllArgsBeUsed() {
    final String name = "ooze:test_state";
    final boolean isEmpty = false;
    final NBTCompound properties = new NBTCompound();
    properties.put("testType", "cool");

    BlockState state = new BlockState(name, properties, isEmpty);
    assertEquals(name, state.getName());
    assertEquals(properties, state.getProperties());
    assertEquals(isEmpty, state.isEmpty());
  }

  @Test
  void getProperties_shouldPropertiesBeImmutable() {
    String nestedTagName = "nested";

    NBTCompound properties = new NBTCompound();
    NBTCompound nested = new NBTCompound();
    nested.put("foo", "bar");
    properties.put(nestedTagName, nested);

    BlockState state = new BlockState("ooze:test_state", properties);

    // Make sure the root compound was copied.
    assertNotSame(properties, state.getProperties());

    // Make sure the nested compound was copied.
    nested.clear();
    assertFalse(state.getProperties().getCompound(nestedTagName).isEmpty());

    // Make sure we can't modify the compound's contents.
    assertThrows(UnsupportedOperationException.class, state.getProperties()::clear);
  }

  @Test
  void hasProperties_shouldReturnFalseIfCompoundIsEmpty() {
    NBTCompound properties = new NBTCompound();

    BlockState stateWithoutProperties = new BlockState("ooze:test_state", properties);
    assertFalse(stateWithoutProperties.hasProperties());
  }

  @Test
  void hasProperties_shouldReturnTrueIfCompoundIsNotEmpty() {
    NBTCompound properties = new NBTCompound();
    properties.put("foo", "bar");

    BlockState stateWithProperties = new BlockState("ooze:test_state", properties);
    assertTrue(stateWithProperties.hasProperties());
  }

  @Test
  void empty_shouldEmptyFactoryReturnEmptyState() {
    assertTrue(BlockState.empty().isEmpty());
  }

  @Test
  void equals_shouldTwoStatesEqualWhenExpected() {
    EqualsVerifier
        .forClass(BlockState.class)
        .withNonnullFields("name", "properties")
        .verify();
  }
}
