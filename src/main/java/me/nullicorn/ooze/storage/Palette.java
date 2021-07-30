package me.nullicorn.ooze.storage;

import com.github.ooze.protos.BlockStateData;
import com.github.ooze.protos.PaletteData;
import com.github.ooze.protos.PaletteData.Builder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A zero-indexed list of {@link BlockState block states}. Allows data using the palette to refer to
 * smaller integers rather than entire states repeatedly.
 *
 * @author Nullicorn
 */
public final class Palette implements Iterable<BlockState> {

  /**
   * A factory for converting palettes from their ProtoBuf form.
   *
   * @throws IOException              if the properties of any of the palette's states could not be
   *                                  NBT-decoded.
   * @throws IllegalArgumentException if the {@code proto} is null.
   */
  public static Palette fromProto(PaletteData proto) throws IOException {
    if (proto == null) {
      throw new IllegalArgumentException("null proto cannot be converted to a palette");
    }

    List<BlockState> states = new ArrayList<>();

    // Wrap each state's proto in a BlockState object.
    for (BlockStateData stateProto : proto.getStatesList()) {
      BlockState state = BlockState.fromProto(stateProto);
      states.add(state);
    }

    return new Palette(proto.getName(), proto.getDataVersion(), states);
  }

  private final String           name;
  private final int              dataVersion;
  private final List<BlockState> states;

  /**
   * @param name        See {@link #getName()}.
   * @param dataVersion See {@link #getDataVersion()}.
   * @param states      The block states in the palette. This order is kept, so that {@link
   *                    #get(int) get()} will return the same block state given an index in the
   *                    list.
   * @throws IllegalArgumentException if the {@code name} or {@code states} list are {@code null},
   *                                  or if any state in the list is {@code null}.
   */
  public Palette(String name, int dataVersion, List<BlockState> states) {
    if (name == null) {
      throw new IllegalArgumentException("name cannot be null");
    } else if (states == null) {
      throw new IllegalArgumentException("state list cannot be null");
    }

    // Make sure this list doesn't contain any null states.
    for (int i = 0; i < states.size(); i++) {
      if (states.get(i) == null) {
        throw new IllegalArgumentException("state cannot be null (i=" + i + ")");
      }
    }

    this.name = name;
    this.dataVersion = dataVersion;

    // Immutably copy the state list.
    this.states = Collections.unmodifiableList(new ArrayList<>(states));
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

  /**
   * @return whether the palette has any states in it. {@code true} only when {@code size() == 0}.
   */
  public boolean isEmpty() {
    return states.isEmpty();
  }

  /**
   * @return the block state at the {@code index} in the palette.
   * @throws IndexOutOfBoundsException if the index is invalid, which happens when {@code index < 0
   *                                   || index >= size()}.
   */
  public BlockState get(int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException("Index must be from 0 to " + (size() - 1) + ": " + index);
    }
    return states.get(index);
  }

  /**
   * @return a Protocol Buffer with the same {@link #getName() name}, {@link #getDataVersion() data
   * version}, and block states as the palette.
   */
  public PaletteData toProto() throws IOException {
    Builder palette = PaletteData.newBuilder()
        .setName(name)
        .setDataVersion(dataVersion);

    for (BlockState state : states) {
      palette.addStates(state.toProto());
    }
    return palette.build();
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

  @Override
  // Format: "name[state1, state2, state3, ...]"
  public String toString() {
    if (states.isEmpty()) {
      return name + "[]";
    }

    StringBuilder sb = new StringBuilder(name).append('[');
    forEach(state -> sb.append(state).append(", "));

    // Replace the trailing comma with a closing bracket.
    sb.replace(sb.length() - 2, sb.length() - 1, "]");
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Palette that = (Palette) o;
    return dataVersion == that.dataVersion &&
           name.equals(that.name) &&
           states.equals(that.states);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, dataVersion, states);
  }
}
