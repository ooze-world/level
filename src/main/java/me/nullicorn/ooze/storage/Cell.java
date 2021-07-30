package me.nullicorn.ooze.storage;

import com.github.ooze.protos.CellData;

/**
 * A 16x16x16 cube of blocks.
 *
 * @author Nullicorn
 */
public class Cell {

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
}
