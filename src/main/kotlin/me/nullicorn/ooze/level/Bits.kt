package me.nullicorn.ooze.level

import java.util.*
import kotlin.experimental.and

/**
 * The minimum number of bits needed to represent the integer.
 *
 * This will always be `32` for negative numbers, due to the way they are represented in two's
 * complement.
 */
internal val Int.width
    get() = Int.SIZE_BITS - countLeadingZeroBits()

/**
 * Copies the contents of a [BitSet] to a [ByteArray]
 *
 * By design, [BitSet.toByteArray] only creates a byte array long enough to hold up to the last
 * *set* bit. Any trailing unset bits that implicitly represent something will get truncated.
 *
 * This function avoids that by either padding the output array with zeros, or truncating it, in
 * order for it to contain exactly the desired number of bits, rounded up to the next octet/byte.
 *
 * When truncating (`numberOfBits < length()`), any extraneous bits—those that fit in the last byte,
 * but exceed the [numberOfBits]—will be replaced with zeros.
 *
 * Bits in the resulting array are ordered in the same way specified by [BitSet.toByteArray].
 *
 * @param numberOfBits The number of bits to be copied to the array.
 * @return the BitSet's contents, as a sequence of octets.
 */
internal fun BitSet.toExactByteArray(numberOfBits: Int): ByteArray {
    var asBytes: ByteArray = this.toByteArray()

    // If applicable, trim or pad the byte array.
    val bytesNeeded = getFewestBytes(numberOfBits)
    if (asBytes.size != bytesNeeded) {
        asBytes = asBytes.copyOf(newSize = bytesNeeded)
    }

    // If applicable, unset any trailing bits in the last byte.
    if (numberOfBits < this.length()) {
        val bitsToKeep: Int = numberOfBits % Byte.SIZE_BITS
        val keptBitsMask = getFullMask(bitsToKeep).toByte()

        asBytes[asBytes.lastIndex] = asBytes.last() and keptBitsMask
    }

    return asBytes
}

/**
 * Creates a 32-bit mask with the first `n` low-order bits set, where `n` is the value of [width].
 *
 * ```
 *                  MSB                              LSB
 *                   |--------------------------------|
 * getFullMask(0)  == 00000000000000000000000000000000
 * getFullMask(1)  == 00000000000000000000000000000001
 * getFullMask(10) == 00000000000000000000001111111111
 * getFullMask(32) == 11111111111111111111111111111111
 * ```
 *
 * @param[width] The number of low-order bits to set in the mask.
 * @return a bitmask with `n` consecutive bits set from the right side (least-sig).
 */
// Equivalent to (int) ((1L << width) - 1) in Java.
internal fun getFullMask(width: Int) = ((1L shl width) - 1).toInt()

/**
 * Determines the fewest number of bytes (octets) needed to represent {@code n} bits, where `n` is
 * the value of [numberOfBits].
 *
 * @param numberOfBits An amount of bits.
 * @return the fewest number of bytes that could be used to represent the supplied [numberOfBits].
 */
internal fun getFewestBytes(numberOfBits: Int): Int {
    var bytesNeeded = numberOfBits / Byte.SIZE_BITS

    // If any bits are leftover, add an extra byte to hold them.
    if (numberOfBits % Byte.SIZE_BITS != 0) bytesNeeded++

    return bytesNeeded
}