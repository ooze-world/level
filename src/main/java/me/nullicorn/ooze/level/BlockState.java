package me.nullicorn.ooze.level;

import com.github.ooze.protos.BlockStateData;
import java.io.IOException;
import java.util.Objects;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.ooze.level.nbt.NbtHelper;

/**
 * Information about a type of block in Minecraft.
 *
 * @author Nullicorn
 */
public final class BlockState {

  /**
   * A constant value returned by the {@link #empty()} factory.
   */
  private static final BlockState EMPTY = new BlockState("air", true);

  /**
   * A factory for creating empty block states, especially for use as a fall-back state or similar.
   *
   * @return an empty block state.
   * @see #isEmpty()
   */
  public static BlockState empty() {
    return EMPTY;
  }

  /**
   * A factory for converting block states from their ProtoBuf form.
   *
   * @throws IOException              if the state's properties could not be NBT-decoded.
   * @throws IllegalArgumentException if the {@code proto} is {@code null}.
   */
  public static BlockState fromProto(BlockStateData proto) throws IOException {
    if (proto == null) {
      throw new IllegalArgumentException("null proto cannot be converted to a block state");
    }
    return new BlockState(proto.getName(), NbtHelper.decodeFromBytes(proto.getProperties()));
  }

  private final String      name;
  private final NBTCompound properties;
  private final boolean     isEmpty;

  /**
   * Constructs a non-empty block state with the provided {@code name}, but with no properties.
   *
   * @see #BlockState(String, NBTCompound, boolean)
   */
  public BlockState(String name) {
    this(name, new NBTCompound(), false);
  }

  /**
   * Constructs a non-empty block state with the provided {@code name} and {@code properties}.
   *
   * @see #BlockState(String, NBTCompound, boolean)
   */
  public BlockState(String name, NBTCompound properties) {
    this(name, properties, false);
  }

  /**
   * Constructs a potentially-empty block state with the provided {@code name}, but with no
   * properties.
   *
   * @see #BlockState(String, NBTCompound, boolean)
   */
  public BlockState(String name, boolean isEmpty) {
    this(name, new NBTCompound(), isEmpty);
  }

  /**
   * Constructs a block state that uses the provided name and properties.
   *
   * @param name       See {@link #getName()}.
   * @param properties See {@link #getProperties()}. If a state has no properties, this should be an
   *                   empty compound, not {@code null}.
   * @param isEmpty    See {@link #isEmpty()}.
   * @throws IllegalArgumentException if the provided {@code name} or {@code properties} are {@code
   *                                  null}.
   */
  public BlockState(String name, NBTCompound properties, boolean isEmpty) {
    if (name == null) {
      throw new IllegalArgumentException("name cannot be null");
    } else if (properties == null) {
      throw new IllegalArgumentException("properties cannot be null; use an empty compound");
    }

    this.name = name;
    this.properties = NbtHelper.copyToImmutable(properties);
    this.isEmpty = isEmpty;
  }

  /**
   * @return the block's main identifier. May or may not be prefixed with a namespace, such as
   * "{@code minecraft:}".
   */
  public String getName() {
    return name;
  }

  /**
   * @return additional information defining the state of the block. Includes things like
   * orientation, power, growth stage, etc.
   * @implNote The returned compound is immutable, and attempts to modify it will result in an
   * {@link UnsupportedOperationException}.
   */
  public NBTCompound getProperties() {
    return properties;
  }

  /**
   * @return {@code true} if the block has properties. Otherwise {@code false}. This is equivalent
   * to {@code !getProperties().isEmpty()}.
   */
  public boolean hasProperties() {
    return !properties.isEmpty();
  }

  /**
   * Indicates whether or not the state can be considered "empty". In the context of a level, empty
   * states may be ignored to optimize storage, or may be used as a fallback state if no other can
   * be found.
   * <p><br>
   * In vanilla Minecraft, this applies to blocks like {@code air}, {@code void_air}, etc.
   *
   * @return {@code true} if the block should be considered empty. Otherwise {@code false}.
   */
  public boolean isEmpty() {
    return isEmpty;
  }

  /**
   * @return a Protocol Buffer with the same {@link #getName() name} and {@link #getProperties()
   * properties} as the block state.
   * @throws IOException if the state's properties could not be NBT-encoded.
   */
  public BlockStateData toProto() throws IOException {
    return BlockStateData.newBuilder()
        .setName(name)
        .setProperties(NbtHelper.encodeToBytes(properties))
        .build();
  }

  @Override
  public String toString() {
    String asString = name;
    if (hasProperties()) {
      asString += properties.toString();
    }
    return asString;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BlockState state = (BlockState) o;
    return isEmpty == state.isEmpty &&
           name.equals(state.name) &&
           properties.equals(state.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, properties, isEmpty);
  }
}
