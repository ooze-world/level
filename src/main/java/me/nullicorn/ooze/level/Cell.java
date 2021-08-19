package me.nullicorn.ooze.level;

import com.github.ooze.protos.CellData;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * A 16x16x16 cube of blocks.
 *
 * @author Nullicorn
 */
public final class Cell {

  /**
   * A constant value returned by the {@link #empty()} factory.
   */
  private static final Cell EMPTY = new Cell(Palette.empty(), new PackedUIntArray(new int[4096]));

  /**
   * A factory for creating empty cells, such that each value in the {@link #getBlocks() blocks}
   * array maps to an {@link BlockState#isEmpty() empty} state in the {@link #getPalette()
   * palette}.
   *
   * @return an empty cell.
   * @see BlockState#isEmpty()
   * @see BlockState#empty()
   * @see Palette#empty()
   */
  public static Cell empty() {
    return EMPTY;
  }

  private final Palette         palette;
  private final PackedUIntArray blocks;

  public Cell(Palette palette, PackedUIntArray blocks) {
    if (palette == null) {
      throw new IllegalArgumentException("null palette cannot be used in cell");
    } else if (blocks == null) {
      throw new IllegalArgumentException("null block array cannot be used in cell");
    } else if (blocks.size() != 4096) {
      throw new IllegalArgumentException("Block array must have 4096 uints, not " + blocks.size());
    }

    this.palette = palette;
    this.blocks = blocks;
  }

  /**
   * A list of block states that can be used in the cell.
   */
  public Palette getPalette() {
    return palette;
  }

  /**
   * The blocks in the cell. Values in the array represent the {@link #getPalette() palette index}
   * of the block's state. Because cells are 16-block cubes, the array's size is always 4096.
   * <p>
   * Blocks appear in the array in XZY order. This means that given a block's X, Y, and Z offset
   * within the cell, it's array index is at: <pre>{@code (x << 8) | (z << 4) | y}</pre>
   */
  public PackedUIntArray getBlocks() {
    return blocks;
  }

  /**
   * @return A protocol buffer with the same palette and blocks as the cell.
   */
  public CellData toProto() {
    return CellData.newBuilder()
        .setPaletteName(palette.getName())
        .setBlocks(blocks.toProto())
        .build();
  }

  /**
   * Creates an identical cell to the current one, but using a new palette that <em>only</em>
   * contains the states used by the cell.
   * <p><br>
   * This can be useful if multiple cells share a palette, but only one cell is needed at a time
   * (such as in Minecraft's level format, where every section has its own palette).
   * <p><br>
   * In some cases this may also decrease the size of the block array because fewer states will need
   * to be referenced, meaning a lower {@link Palette#magnitude() magnitude}.
   *
   * @return an identical cell with its own palette.
   */
  public Cell isolatedCopy() {
    int arrayLength = blocks.size();
    int paletteSize = palette.size();

    // Determine which (and how many) of the palette's
    // states are being used in the block array.
    //
    // Each index in the BitSet corresponds to the state
    // at the same index in the palette.
    BitSet isStateUsed = new BitSet(paletteSize);
    for (int i = 0; i < arrayLength; i++) {
      int state = blocks.get(i);

      // Mark the index as "used" (as long as it's in
      // bounds).
      if (state >= 0 && state < paletteSize) {
        isStateUsed.set(state, true);
      }
    }
    int numberOfUsedStates = isStateUsed.cardinality();

    // A list all the states indicated by the isStateUsed
    // BitSet, in the same order.
    List<BlockState> isolatedPalette = new ArrayList<>(numberOfUsedStates);

    // Represents the same "blocks" as the original array,
    // but using the isolated palette.
    PackedUIntArray isolatedArray;

    // If all states are used, then we can short-circuit
    // by just copying the existing palette & blocks.
    if (numberOfUsedStates >= paletteSize) {
      // If all states are used, then the existing
      // palette & array can just be copied.
      palette.forEach(isolatedPalette::add);
      isolatedArray = blocks;

    } else {
      // A map for keeping track of each state's index
      // in the original palette and the isolated one.
      ArrayUIntMap newIndices = new ArrayUIntMap(numberOfUsedStates);

      // Only copy the "used" states into the isolated
      // palette.
      for (int i = 0; i < paletteSize; i++) {
        if (isStateUsed.get(i)) {
          BlockState state = palette.get(i);

          isolatedPalette.add(state);
          newIndices.set(i, isolatedPalette.size() - 1);
        }
      }

      // Recreate the "blocks" array using the isolated
      // palette.
      int[] tempIsolatedArray = new int[arrayLength];
      for (int i = 0; i < arrayLength; i++) {
        int state = blocks.get(i);
        int newState = newIndices.get(state);

        // If for whatever reason the state wasn't
        // remapped at all, set it to 0.
        tempIsolatedArray[i] = (newState == -1)
            ? 0
            : newState;
      }

      // And pack the array.
      isolatedArray = new PackedUIntArray(tempIsolatedArray);
    }

    String paletteName = "ooze:isolated_" + Integer.toHexString(hashCode());
    int paletteVersion = palette.getDataVersion();

    return new Cell(
        new Palette(paletteName, paletteVersion, isolatedPalette),
        isolatedArray
    );
  }
}
