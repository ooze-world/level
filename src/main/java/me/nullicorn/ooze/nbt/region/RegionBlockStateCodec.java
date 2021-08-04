package me.nullicorn.ooze.nbt.region;

import static me.nullicorn.ooze.nbt.region.RegionTag.BLOCK_NAME;
import static me.nullicorn.ooze.nbt.region.RegionTag.BLOCK_PROPERTIES;

import java.io.IOException;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.ooze.level.BlockState;

/**
 * Provides serialization to and from NBT block states stored in palettes.
 *
 * @author Nullicorn
 */
public class RegionBlockStateCodec {

  private final DataVersion version;

  /**
   * Creates a codec compatible with a specific Minecraft {@code version}.
   *
   * @throws IllegalArgumentException if the {@code version} is {@code null}, or if it does not
   *                                  support {@link DataVersion#isPaletteSupported() palettes}.
   */
  public RegionBlockStateCodec(DataVersion version) {
    if (version == null) {
      throw new IllegalArgumentException("version cannot be null");
    } else if (!version.isPaletteSupported()) {
      throw new IllegalArgumentException("version does not support block state palettes");
    }

    this.version = version;
  }

  /**
   * @return the Minecraft world version that the codec is compatible with.
   */
  public DataVersion getCompatibility() {
    return version;
  }

  /**
   * Creates a new compound tag containing the {@code state}'s {@link BlockState#getName() name} and
   * {@link BlockState#getProperties() properties}, if it has any.
   * <p>
   * The state's name can be found under a string tag, {@code Name}, within the compound. If the
   * state {@link BlockState#hasProperties() has any properties}, those are also copied into the new
   * compound under a compound tag, {@code Properties}.
   *
   * @param state The block state to NBT-encode.
   * @return an NBT compound resembling the inputted block state.
   */
  public NBTCompound encode(BlockState state) {
    if (state == null) {
      throw new IllegalArgumentException("null cannot be encoded as a block state");
    }

    NBTCompound encoded = new NBTCompound();

    BLOCK_NAME.setFor(encoded, state.getName());
    if (state.hasProperties()) {
      // Copy the properties to a mutable compound so
      // that the caller can modify them if needed.
      NBTCompound properties = new NBTCompound();
      properties.putAll(state.getProperties());

      BLOCK_PROPERTIES.setFor(encoded, properties);
    }

    return encoded;
  }

  /**
   * Creates a new block state with the name and properties defined by an NBT compound tag. The
   * expected format is described {@link #encode(BlockState) here}.
   *
   * @param state An NBT compound representing a Minecraft block state.
   * @return the block state defined by the compound's tags.
   * @throws IOException if the compound has no {@code Name} tag.
   */
  public BlockState decode(NBTCompound state) throws IOException {
    if (state == null) {
      throw new IllegalArgumentException("null cannot be decoded as a block state");
    }

    String name = BLOCK_NAME.getFrom(state, true);
    NBTCompound properties = BLOCK_PROPERTIES.getFrom(state, false);

    if (properties == null) {
      return new BlockState(name);
    }
    return new BlockState(name, properties);
  }
}
