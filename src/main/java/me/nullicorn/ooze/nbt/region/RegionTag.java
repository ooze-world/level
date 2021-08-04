package me.nullicorn.ooze.nbt.region;

import java.io.IOException;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.TagType;

/**
 * An assortment of NBT tags used by Minecraft for storing chunk information.
 *
 * @author Nullicorn
 */
public enum RegionTag {

  /*
   * Tags within the root compounds of the `region` directory.
   */

  /**
   * The wrapper compound around a chunk's data.
   * <p><br>
   * Located at the root of a compound in an anvil world file.
   */
  ROOT_CHUNK("Level", TagType.COMPOUND),

  /**
   * The version that a chunk was last saved in.
   * <p><br>
   * Located at the root of a compound in an anvil world file.
   */
  ROOT_DATA_VERSION("DataVersion", TagType.INT, 100),

  /*
   * Tags within the "Level" compounds of the `region` directory.
   */

  /**
   * The chunk's location along the X axis, measured in 16-block units.
   * <p><br>
   * Located in the {@link #ROOT_CHUNK chunk tag}.
   */
  CHUNK_POS_X("xPos", TagType.INT),

  /**
   * The chunk's location along the Z axis, measured in 16-block units.
   * <p><br>
   * Located in the {@link #ROOT_CHUNK chunk tag}.
   */
  CHUNK_POS_Z("zPos", TagType.INT),

  /**
   * A list of 16-block tall cubes that store portions of the chunk's blocks.
   * <p><br>
   * Located in the {@link #ROOT_CHUNK chunk tag}.
   */
  CHUNK_SECTIONS("Sections", TagType.LIST),

  /**
   * A list compounds containing information about each mob/object inside the chunk's horizontal
   * boundaries.
   * <p><br>
   * Only valid within a world's {@code region} directory. As of data version {@code 2679} (1.17.x),
   * the tag is still supported, but {@link #ENTITY_CHUNK_LIST} and {@link #ENTITY_CHUNK_POSITION}
   * are preferred.
   */
  CHUNK_ENTITIES("Entities", TagType.LIST),

  /**
   * A list of compounds containing information about each block in the chunk with metadata
   * (containers, moving pistons, etc).
   */
  CHUNK_BLOCK_ENTITIES("TileEntities", TagType.LIST),

  /**
   * The chunk's generation state.
   * <p><br>
   * Data versions prior to {@code 1466} (1.13.x) should use {@link #CHUNK_HAS_LIGHT} and {@link
   * #CHUNK_HAS_BLOCKS} instead.
   */
  CHUNK_STATUS("Status", TagType.STRING, 1466),

  /**
   * Whether or not the game has calculated light levels for the chunk yet.
   * <p><br>
   * As of data version {@code 1466} (1.13.x), this tag is deprecated in favor of {@link
   * #CHUNK_STATUS}.
   */
  CHUNK_HAS_LIGHT("LightPopulated", TagType.BYTE, 99, 1465),

  /**
   * Whether or not the game has calculated light levels for the chunk yet.
   * <p><br>
   * As of data version {@code 1466} (1.13.x), this tag is deprecated in favor of {@link
   * #CHUNK_STATUS}.
   */
  CHUNK_HAS_BLOCKS("TerrainPopulated", TagType.BYTE, 99, 1465),

  /*
   * Tags within the compound elements of a chunk's "Sections".
   */

  /**
   * The section's vertical distance from {@code y=0}, measured in 16-block units.
   * <p><br>
   * Located in the compounds of a chunk's {@link #CHUNK_SECTIONS section list}.
   */
  SECTION_ALTITUDE("Y", TagType.INT),

  /**
   * A list of all block states that can be used in the section.
   * <p><br>
   * Each block state is a compound containing a {@link #BLOCK_NAME name}, and optionally {@link
   * #BLOCK_PROPERTIES extra properties}.
   * <p><br>
   * Data versions prior to {@code 1451} (1.13.x) should use {@link #LEGACY_SECTION_BLOCKS}, {@link
   * #LEGACY_SECTION_BLOCK_STATES}, and {@link #LEGACY_SECTION_BLOCK_EXTENSIONS} instead.
   * <p><br>
   * Located in the compounds of a chunk's {@link #CHUNK_SECTIONS section list}.
   */
  SECTION_PALETTE("Palette", TagType.LIST, 1451),

  /**
   * A {@link RegionCompactArrayCodec compact array} of 4096 integers (e.g. multiple values in a
   * single longs; the number of longs in the array is always less).
   * <p><br>
   * Each compacted value in the array is an index pointing to a state in the section's {@link
   * #SECTION_PALETTE palette}. The array's {@code magnitude} is the number of bits needed to hold
   * the palette's last index.
   * <p><br>
   * Located in the compounds of a chunk's {@link #CHUNK_SECTIONS section list}.
   */
  SECTION_BLOCKS("BlockStates", TagType.LONG_ARRAY, 1451),

  /**
   * An array of 4096 block IDs for the section. Blocks appear in YZX order, meaning all blocks with
   * the same Z and Y positions will be adjacent in the array.
   * <p><br>
   * "Block ID" refers to the unique numeric identifiers assigned to blocks and items in older
   * versions of Minecraft. As of data version {@code 1451} (1.13.x), this tag is deprecated in
   * favor of {@link #SECTION_BLOCKS} and {@link #SECTION_PALETTE}.
   * <p><br>
   * Located in the compounds of a chunk's {@link #CHUNK_SECTIONS section list}.
   */
  LEGACY_SECTION_BLOCKS("Blocks", TagType.BYTE_ARRAY, 99, 1450),

  /**
   * An array of 2048 octets, each containing two of the data/damage values for corresponding blocks
   * in the {@link #LEGACY_SECTION_BLOCKS block array}.
   * <p><br>
   * Each octet contains two data/damage values. Given an index in the block array, the
   * corresponding data value can be found in the octet at {@code index / 2}. Even-number indices
   * use the octet's lowest four bits, and odd-number indices use the highest 4 bits.
   * <p><br>
   * As of data version {@code 1451} (1.13.x), this tag is deprecated in favor of {@link
   * #SECTION_BLOCKS} and {@link #SECTION_PALETTE}.
   * <p><br>
   * Located in the compounds of a chunk's {@link #CHUNK_SECTIONS section list}.
   */
  LEGACY_SECTION_BLOCK_STATES("Data", TagType.BYTE_ARRAY, 99, 1450),

  /**
   * An optional array of 2048 octets, each containing a pair of 4-bit values that can be used to
   * extend the {@link #LEGACY_SECTION_BLOCKS block array}.
   * <p><br>
   * Extension allows block IDs to use up to 12 bits, rather than the usual 8-bit cap imposed by the
   * block array. Extensions are applied by taking the corresponding 4 bits from this array,
   * left-shifting them 8 bits, and then adding the result to the corresponding block ID. The 4-bit
   * groups of this array are indexed exactly the same as the {@link #LEGACY_SECTION_BLOCK_STATES
   * data array}.
   * <p><br>
   * As of data version {@code 1451} (1.13.x), this tag is deprecated in favor of {@link
   * #SECTION_BLOCKS} and {@link #SECTION_PALETTE}.
   * <p><br>
   * Located in the compounds of a chunk's {@link #CHUNK_SECTIONS section list}.
   */
  LEGACY_SECTION_BLOCK_EXTENSIONS("Add", TagType.BYTE_ARRAY, 99, 1450),

  /*
   * Tags that make up entries of a section's "Palette".
   */

  /**
   * The block's main identifier, with or without a namespace.
   * <p><br>
   * e.g. {@code minecraft:stone}, {@code air}, {@code namespace:value}, etc.
   * <p><br>
   * Located in the compounds of a section's {@link #SECTION_PALETTE palette}.
   */
  BLOCK_NAME("Name", TagType.STRING, 1451),

  /**
   * An optional compound that defines extra information about a block's state. This includes things
   * like orientation, power, level, etc.
   * <p><br>
   * Located in the compounds of a section's {@link #SECTION_PALETTE palette}.
   */
  BLOCK_PROPERTIES("Properties", TagType.COMPOUND, 1451),

  /*
   * Tags relating to the positions of blocks-entities and entities.
   */

  /**
   * A list of 3 doubles indicating an entity's absolute X, Y, and Z positions in the world, in that
   * order.
   * <p><br>
   * Located in the compounds of a chunk's {@link #CHUNK_ENTITIES entity list}, or in an
   * entity-chunk's {@link #ENTITY_CHUNK_LIST entries} (data versions {@code 2679}+ only).
   */
  POS_ENTITY("Pos", TagType.LIST),

  /**
   * An integer indicating a block-entity's absolute X coordinate in the world.
   * <p><br>
   * Located in the compounds of a chunk's {@link #CHUNK_BLOCK_ENTITIES block-entity list}.
   *
   * @see #POS_Y_BLOCK_ENTITY
   * @see #POS_Z_BLOCK_ENTITY
   */
  POS_X_BLOCK_ENTITY("x", TagType.INT),

  /**
   * An integer indicating a block-entity's absolute Y coordinate in the world.
   * <p><br>
   * Located in the compounds of a chunk's {@link #CHUNK_BLOCK_ENTITIES block-entity list}.
   *
   * @see #POS_X_BLOCK_ENTITY
   * @see #POS_Z_BLOCK_ENTITY
   */
  POS_Y_BLOCK_ENTITY("y", TagType.INT),

  /**
   * An integer indicating a block-entity's absolute Z coordinate in the world.
   * <p><br>
   * Located in the compounds of a chunk's {@link #CHUNK_BLOCK_ENTITIES block-entity list}.
   *
   * @see #POS_X_BLOCK_ENTITY
   * @see #POS_Y_BLOCK_ENTITY
   */
  POS_Z_BLOCK_ENTITY("z", TagType.INT),

  /*
   * Tags within the root compounds of the `entities` directory.
   */

  /**
   * A list compounds containing information about each mob/object inside the chunk's horizontal
   * boundaries.
   * <p><br>
   * Only valid within the {@code entities} directory of a world, which was added in data version
   * {@code 2679} (1.17.x). For older versions, see {@link #CHUNK_ENTITIES}.
   * <p><br>
   * Located at the root of a compound in an anvil world file.
   */
  ENTITY_CHUNK_LIST("Entities", TagType.LIST, 2679),

  /**
   * A list of two integers, indicating the X and Y coordinates of the chunk respectively.
   * <p><br>
   * Only valid within the {@code entities} directory of a world, which was added in data version
   * {@code 2679} (1.17.x). For older versions, see {@link #CHUNK_ENTITIES}.
   * <p><br>
   * Located at the root of a compound in an anvil world file.
   */
  ENTITY_CHUNK_POSITION("Position", TagType.INT_ARRAY, 2679);

  private final String  name;
  private final TagType expectedType;
  private final int     minVersion;
  private final int     maxVersion;

  RegionTag(String name, TagType type) {
    this(name, type, 99);
  }

  RegionTag(String name, TagType type, int lowestVersion) {
    this(name, type, lowestVersion, Integer.MAX_VALUE);
  }

  RegionTag(String name, TagType type, int lowestVersion, int highestVersion) {
    this.name = name;
    this.expectedType = type;
    this.minVersion = lowestVersion;
    this.maxVersion = highestVersion;
  }

  /**
   * @return {@code true} if the tag is supported by the Minecraft world version. Otherwise {@code
   * false}.
   */
  public boolean isSupportedIn(int dataVersion) {
    return dataVersion >= minVersion && dataVersion <= maxVersion;
  }

  /**
   * Gets the value of the tag as the direct child of a compound.
   *
   * @param compound    The compound to try and get the tag's value from.
   * @param isRequired  If {@code true}, an {@link IOException} is thrown if the tag cannot be found
   *                    in the compound. If {@code false}, {@code null} is returned in that
   *                    scenario.
   * @param dataVersion The Minecraft world version that the value is being retrieved for.
   * @param <T>         The expected runtime type of the value.
   * @return the tag's value in the compound, or {@code null} if {@code isRequired == false} and the
   * compound does not contain a tag with the expected name and type.
   * @throws IOException              if {@code isRequired == true} and the compound does not
   *                                  contain a tag with the expected name and type.
   * @throws IllegalArgumentException if the input compound is null.
   */
  public <T> T getFrom(NBTCompound compound, boolean isRequired, int dataVersion) throws IOException {
    checkVersion(dataVersion);

    if (compound == null) {
      throw new IllegalArgumentException("Cannot get tag " + this + " from null compound");
    }

    Object value = compound.get(name);
    if (value == null || TagType.fromObject(value) != expectedType) {
      if (isRequired) {
        throw new IOException(expectedType + " tag \"" + name + "\" not found: " + compound);
      }
      return null;
    }

    // Suppressed because the type is implicitly checked via TagType.
    // noinspection unchecked
    return (T) value;
  }

  /**
   * Sets the value of the tag as the direct child of the input compound.
   *
   * @param compound    The compound to set the tag's value in.
   * @param value       The value to set the tag to.
   * @param dataVersion The Minecraft world version that the value is being retrieved for.
   * @throws IllegalArgumentException      if the compound or value are {@code null}, or if the
   *                                       value's class is not compatible with the tag's NBT type.
   * @throws UnsupportedOperationException if the compound is immutable.
   */
  public void setFor(NBTCompound compound, Object value, int dataVersion) {
    checkVersion(dataVersion);

    if (compound == null) {
      throw new IllegalArgumentException("Cannot set tag " + this + " for null compound");
    } else if (value == null) {
      throw new IllegalArgumentException("NBT value cannot be null");
    }

    TagType actualType = TagType.fromObject(value);
    if (actualType != expectedType) {
      throw new IllegalArgumentException("Value must be " + expectedType + ", not " + actualType);
    }

    compound.put(name, value);
  }

  /**
   * @throws IllegalArgumentException if the tag is not compatible with the provided {@code
   *                                  dataVersion}.
   */
  private void checkVersion(int dataVersion) {
    if (!isSupportedIn(dataVersion)) {
      String reason = "Tag " + this + " requires ";

      reason += (maxVersion == Integer.MAX_VALUE)
          ? "at least version " + minVersion
          : "version in range [" + minVersion + ", " + maxVersion + "]";

      throw new IllegalArgumentException(reason + ", not " + dataVersion);
    }
  }
}
