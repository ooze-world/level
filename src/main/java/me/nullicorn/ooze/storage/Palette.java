package me.nullicorn.ooze.storage;

import com.github.ooze.protos.BlockStateData;
import com.github.ooze.protos.PaletteData;
import com.github.ooze.protos.PaletteData.Builder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.nullicorn.nedit.type.NBTCompound;
import me.nullicorn.ooze.nbt.NbtUtils;
import me.nullicorn.ooze.storage.Palette.BlockState;

/**
 * @author Nullicorn
 */
public class Palette implements Iterable<BlockState> {

  // TODO: 7/28/21 Finish javadocs.
  // TODO: 7/28/21 Move constructor params & `add(BlockState)` to a builder.

  private final String name;
  private final int    dataVersion;

  private final List<BlockState> states;

  public Palette(String name, int dataVersion) {
    this.name = name;
    this.dataVersion = dataVersion;
    this.states = new ArrayList<>();
  }

  /**
   * @return the name that cells can use to refer to the palette.
   */
  public String getName() {
    return name;
  }

  /**
   * @return the Minecraft <a href=https://minecraft.fandom.com/wiki/Data_version>data version</a>
   * that the palette's states are compatible with.
   */
  public int getDataVersion() {
    return dataVersion;
  }

  /**
   * @return the number of states in the palette.
   */
  public int size() {
    return states.size();
  }

  public BlockState get(int index) {
    // TODO: 7/28/21 Return air for out-of-bounds identifiers.
    return states.get(index);
  }

  public int add(BlockState state) {
    int index = states.indexOf(state);
    if (index == -1) {
      index = states.size();
      states.add(state);
    }
    return index;
  }

  /**
   * @return a Protocol Buffer with the same {@link #getName() name}, {@link #getDataVersion() data
   * version}, and block states as the palette.
   * @throws IOException if any of the palette's states could not be NBT-encoded.
   */
  public PaletteData toProto() throws IOException {
    Builder b = PaletteData.newBuilder()
        .setName(name)
        .setDataVersion(dataVersion);

    for (BlockState state : states) {
      b.addStates(state.toProto());
    }
    return b.build();
  }

  @Override
  public Iterator<BlockState> iterator() {
    Iterator<BlockState> mutableIter = states.iterator();

    // Wrap the iterator in one that *does not* support remove().
    // Otherwise indexing could get screwed up by accident.
    return new Iterator<BlockState>() {
      @Override
      public boolean hasNext() {
        return mutableIter.hasNext();
      }

      @Override
      public BlockState next() {
        return mutableIter.next();
      }
    };
  }

  public static class BlockState {

    private final String      name;
    private final NBTCompound properties;

    public BlockState(String name) {
      this(name, new NBTCompound());
    }

    public BlockState(String name, NBTCompound properties) {
      if (name == null) {
        throw new IllegalArgumentException("name cannot be null");
      } else if (properties == null) {
        throw new IllegalArgumentException("properties cannot be null; use an empty compound");
      }

      this.name = name;
      this.properties = properties;
    }

    public String getName() {
      return name;
    }

    public NBTCompound getProperties() {
      return properties;
    }

    public boolean hasProperties() {
      return !properties.isEmpty();
    }

    public BlockStateData toProto() throws IOException {
      return BlockStateData.newBuilder()
          .setName(name)
          .setProperties(NbtUtils.toByteString(properties))
          .build();
    }
  }
}
