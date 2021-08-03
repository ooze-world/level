package me.nullicorn.ooze.nbt.region;

/**
 * Wrapper around Minecraft world versions. Used to identify the version a chunk is saved in.
 *
 * @author Nullicorn
 */
public class DataVersion {

  protected final int value;

  /**
   * @param version See {@link #getValue() value}.
   */
  public DataVersion(int version) {
    this.value = version;
  }

  /**
   * @return The number that Minecraft uses to identify the version.
   */
  public int getValue() {
    return value;
  }

  /**
   * @return {@code true} if the version supports the {@code Palette} and {@code BlockStates} tags
   * on chunk sections.
   * @apiNote If {@code false}, the {@code Blocks}, {@code Add}, and {@code Data} tags should be
   * used instead.
   */
  public boolean isPaletteSupported() {
    return value >= 1451;
  }

  /**
   * @return {@code true} if the version supports the {@code Status} tag on chunks.
   * @apiNote If {@code false}, the {@code LightPopulated} and {@code TerrainPopulated} tags should
   * be used instead.
   */
  public boolean isStatusTagSupported() {
    return value >= 1466;
  }

  /**
   * @return {@code true} if the version requires blocks in the {@code BlockStates} array to be
   * inside one {@code long} only.
   * @apiNote If {@code false}, the last block inside a {@code long} may have more bits in the next
   * {@code long} as well. When that happens, the remaining bits can be found on the
   * least-significant side of the next {@code long}.
   */
  public boolean isBlockArrayPadded() {
    return value >= 2527;
  }

  public boolean isEntityDirectorySupported() {
    return value >= 2679;
  }
}
