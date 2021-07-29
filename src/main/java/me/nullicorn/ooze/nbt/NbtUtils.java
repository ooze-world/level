package me.nullicorn.ooze.nbt;

import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import me.nullicorn.nedit.NBTInputStream;
import me.nullicorn.nedit.NBTOutputStream;
import me.nullicorn.nedit.type.NBTCompound;

/**
 * Helper methods for encoding and decoding NBT data.
 *
 * @author Nullicorn
 */
public final class NbtUtils {

  /**
   * Serializes the contents of an NBT compound.
   *
   * @return a string of bytes, representing an NBT-encoded compound. This includes the type and
   * name of each child, finally terminated by a {@code TAG_End}.
   * @throws IOException if the compound could not be serialized.
   */
  public static ByteString toByteString(NBTCompound compound) throws IOException {
    if (compound == null) {
      throw new IllegalArgumentException("null compound cannot be converted to proto");
    } else if (compound.isEmpty()) {
      // Return zero bytes if the compound is empty.
      return ByteString.EMPTY;
    }

    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();

    // Serialize the contents of the compound.
    new NBTOutputStream(bytesOut, false).writeCompound(compound);
    return ByteString.copyFrom(bytesOut.toByteArray());
  }

  /**
   * Builds a compound object from an NBT-encoded string of bytes.
   *
   * @param proto a string of bytes representing an NBT-encoded compound. This includes each tag's
   *              type, name, and value. As specified by the NBT format, the last byte must be a
   *              {@code TAG_End}, even if there are no other tags.
   * @return the compound represented by the bytes.
   * @throws IOException if the byte string was not an NBT-encoded compound.
   */
  public static NBTCompound fromByteString(ByteString proto) throws IOException {
    if (proto == null) {
      throw new IllegalArgumentException("null proto cannot be converted to compound");
    } else if (proto.isEmpty()) {
      // Return an empty compound if no bytes were supplied.
      return new NBTCompound();
    }

    // Open a new stream for the bytes. String interning is enabled for both keys and values.
    try (NBTInputStream in = new NBTInputStream(proto.newInput(), true, true)) {
      return in.readCompound();
    }
  }

  private NbtUtils() {
    throw new UnsupportedOperationException("NbtUtils should not be instantiated");
  }
}
