package me.nullicorn.ooze.storage;

import com.github.ooze.protos.BlockStateData;
import java.io.IOException;
import java.util.Objects;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.ooze.nbt.NbtUtils;

/**
 * Information about a type of block in Minecraft.
 *
 * @author Nullicorn
 */
public final class BlockState {

  /**
   * A factory for converting block states from their ProtoBuf form.
   *
   * @throws IOException if the state's properties could not be NBT-decoded.
   */
  public static BlockState fromProto(BlockStateData proto) throws IOException {
    if (proto == null) {
      throw new IllegalArgumentException("null proto cannot be converted to a block state");
    }
    return new BlockState(proto.getName(), NbtUtils.decodeFromBytes(proto.getProperties()));
  }

  private final String      name;
  private final NBTCompound properties;

  /**
   * Same as {@link #BlockState(String, NBTCompound)}, but {@code properties} defaults to an empty
   * compound.
   *
   * @param name See {@link #getName()}.
   */
  public BlockState(String name) {
    this(name, new NBTCompound());
  }

  /**
   * @param name       See {@link #getName()}.
   * @param properties See {@link #getProperties()}.
   */
  public BlockState(String name, NBTCompound properties) {
    if (name == null) {
      throw new IllegalArgumentException("name cannot be null");
    } else if (properties == null) {
      throw new IllegalArgumentException("properties cannot be null; use an empty compound");
    }

    this.name = name;
    this.properties = NbtUtils.copyToImmutable(properties);
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
   * orientation, power, growth stage, etc. The returned compound is immutable, so attempting to
   * modify it will result in an {@link UnsupportedOperationException}.
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
   * @return a Protocol Buffer with the same {@link #getName() name} and {@link #getProperties()
   * properties} as the block state.
   * @throws IOException if the state's properties could not be NBT-encoded.
   */
  public BlockStateData toProto() throws IOException {
    return BlockStateData.newBuilder()
        .setName(name)
        .setProperties(NbtUtils.encodeToBytes(properties))
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
    return name.equals(state.name) &&
           properties.equals(state.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, properties);
  }
}
