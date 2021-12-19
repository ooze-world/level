package me.nullicorn.ooze.level;

import com.github.ooze.protos.LevelData;
import com.github.ooze.protos.LevelData.Builder;
import com.github.ooze.protos.LevelData.Coordinates;
import com.github.ooze.protos.LevelData.Dimensions;
import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.nedit.type.NBTList;
import me.nullicorn.nedit.type.TagType;
import me.nullicorn.ooze.level.nbt.NbtHelper;

/**
 * Basic information about a compact Minecraft world. The level itself is comprised of cubes called
 * "cells". Each cell is 16 blocks long, meaning a cell hold 4096 blocks total. The level also holds
 * information about the entities and block entities in the level, as well as any persistent
 * metadata that the user wishes to store.
 *
 * @author Nullicorn
 */
public class Level {

  /**
   * A comparator for putting coordinates in XZY order, which is how cells should be sorted.
   */
  private static final Comparator<Coordinates> CELL_SORT = Comparator
      .comparingInt(Coordinates::getX)
      .thenComparingInt(Coordinates::getZ)
      .thenComparingInt(Coordinates::getY);

  /**
   * Helper function for creating a {@link Coordinates} object without using the builder.
   */
  private static Coordinates createCoordinates(int x, int y, int z) {
    return Coordinates.newBuilder().setX(x).setY(y).setZ(z).build();
  }

  /**
   * Helper function for getting a cell's index in the population bit field, given the level's size
   * and origin.
   *
   * @apiNote All three parameters are measured in 16-block units.
   */
  private static int indexOfCellAt(Coordinates location, Coordinates origin, Dimensions levelSize) {
    int xOffset = location.getX() - origin.getX();
    int yOffset = location.getY() - origin.getY();
    int zOffset = location.getZ() - origin.getZ();
    return (xOffset * levelSize.getDepth() * levelSize.getHeight())
           + (zOffset * levelSize.getHeight())
           + yOffset;
  }

  /**
   * The level's blocks, stored in 16-wide cubes.
   */
  private final NavigableMap<Coordinates, Cell> cells;

  /**
   * Custom NBT data related to the level. Could include things like creation date, level-specific
   * configuration, etc.
   */
  private final NBTCompound metadata;

  /**
   * The raw NBT data for any mobs and objects in the level.
   */
  private final NBTList entities;

  /**
   * The raw NBT data for any block entities (aka tile entities) in the level. This includes things
   * like containers, enchanting tables, etc.
   */
  private final NBTList blockEntities;

  public Level() {
    cells = new TreeMap<>(CELL_SORT);
    metadata = new NBTCompound();
    entities = new NBTList(TagType.COMPOUND);
    blockEntities = new NBTList(TagType.COMPOUND);
  }

  /**
   * @return The cell at those coordinates in the level, or {@code null} if there is none set there.
   * @apiNote Coordinates use units of 16 blocks, which is the size of a cell (on all sides).
   */
  public Cell getCell(int x, int y, int z) {
    return cells.get(createCoordinates(x, y, z));
  }

  /**
   * Sets the blocks for a 16-block cube in the level. If there were already blocks there, this
   * operation will overwrite them.
   *
   * @param cell The blocks to put at that location.
   * @throws IllegalArgumentException if the {@code cell} is {@code null}.
   * @apiNote Coordinates use units of 16 blocks, which is the size of a cell (on all sides).
   */
  public void setCell(int x, int y, int z, Cell cell) {
    if (cell == null) {
      throw new IllegalArgumentException("null cell cannot be added to level");
    }

    cells.put(createCoordinates(x, y, z), cell);
  }

  /**
   * Removes any blocks in a 16x16x16 cube from the level. Subsequent calls to {@link #getCell(int,
   * int, int) getCell()} will return {@code null} for those coordinates unless the blocks are
   * repopulated (via {@link #setCell(int, int, int, Cell) setCell()}).
   *
   * @apiNote Coordinates use units of 16 blocks, which is the size of a cell (on all sides).
   */
  public void clearCell(int x, int y, int z) {
    cells.remove(createCoordinates(x, y, z));
  }

  /**
   * A mutable container for holding persistent information about the level itself.
   */
  public NBTCompound getMetadata() {
    return metadata;
  }

  /**
   * A list of NBT compounds, each representing an mob or object in the level.
   */
  public NBTList getEntities() {
    return entities;
  }

  /**
   * A list of NBT compounds, each representing a block entity (aka tile entity) in the level. This
   * includes things like container blocks, enchanting tables, etc.
   */
  public NBTList getBlockEntities() {
    return blockEntities;
  }

  /**
   * @return a Protocol Buffer containing all the same information as the level itself.
   * @throws IOException if any of the level's arbitrary data cannot be NBT-encoded (e.g. metadata,
   *                     entities, block states, etc).
   */
  public LevelData toProto() throws IOException {
    Builder builder = LevelData.newBuilder();

    // Custom info about the level.
    builder.setMetadata(NbtHelper.encodeToBytes(metadata));

    // Regular entities (mobs, objects, etc).
    for (Object entity : entities) {
      builder.addEntities(NbtHelper.encodeToBytes((NBTCompound) entity));
    }

    // Block entities (aka tile entities).
    for (Object blockEntity : blockEntities) {
      builder.addBlockEntities(NbtHelper.encodeToBytes((NBTCompound) blockEntity));
    }

    // Determine the level's size using the
    // distance between the farthest two cells.
    Coordinates origin = cells.firstKey();
    Coordinates farthest = cells.lastKey();
    Dimensions size = Dimensions.newBuilder()
        .setWidth(farthest.getX() - origin.getX())
        .setDepth(farthest.getZ() - origin.getZ())
        .setHeight(farthest.getY() - origin.getY())
        .build();

    // Where & how big the level is.
    builder.setOrigin(origin);
    builder.setSize(size);

    // Determine which cells have blocks in them.
    int populationSize = size.getWidth() * size.getDepth() * size.getHeight();
    BitSet population = new BitSet(populationSize);
    Map<String, Palette> palettesByName = new HashMap<>();

    cells.forEach((location, cell) -> {
      // Mark the cell as populated.
      int cellIndex = indexOfCellAt(location, origin, size);
      population.set(cellIndex, true);

      // Add the cell to the level.
      builder.addCells(cell.toProto());

      // Add the cell's block palette to the level.
      Palette palette = cell.getPalette();
      Palette existing = palettesByName.put(palette.getName(), palette);

      // Make sure two different palettes don't use the same name.
      if (!palette.equals(existing)) {
        throw new IllegalStateException("palette name \"" + palette.getName() + "\" is not unique");
      }
    });

    // Tell the level which cells we added above.
    byte[] populationBytes = BitHelper.getAsByteArray(population, populationSize);
    builder.setPopulation(ByteString.copyFrom(populationBytes));

    // Tell the level which types of blocks it can use.
    for (Palette palette : palettesByName.values()) {
      builder.addPalettes(palette.toProto());
    }

    return builder.build();
  }
}
