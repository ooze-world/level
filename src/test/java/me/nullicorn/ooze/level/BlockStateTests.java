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
  void shouldFailIfNameIsNull() {
    assertThrows(IllegalArgumentException.class, () -> new BlockState(null));
  }

  @Test
  void shouldFailIfPropertiesAreNull() {
    assertThrows(IllegalArgumentException.class, () -> new BlockState("ooze_test_state", null));
  }

  @Test
  void shouldConstructorArgsBeUsed() {
    String name = "ooze:test_state";
    NBTCompound properties = new NBTCompound();
    properties.put("testType", "cool");

    BlockState state = new BlockState(name, properties);
    assertEquals(name, state.getName());
    assertEquals(properties, state.getProperties());
  }

  @Test
  void shouldPropertiesBeImmutable() {
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
  void shouldNotHavePropertiesIfEmpty() {
    NBTCompound properties = new NBTCompound();

    BlockState stateWithoutProperties = new BlockState("ooze:test_state", properties);
    assertFalse(stateWithoutProperties.hasProperties());
  }

  @Test
  void shouldHavePropertiesIfNotEmpty() {
    NBTCompound properties = new NBTCompound();
    properties.put("foo", "bar");

    BlockState stateWithProperties = new BlockState("ooze:test_state", properties);
    assertTrue(stateWithProperties.hasProperties());
  }

  @Test
  void shouldTwoStatesEqualWhenExpected() {
    EqualsVerifier
        .forClass(BlockState.class)
        .withNonnullFields("name", "properties")
        .verify();
  }
}
