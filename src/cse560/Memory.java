package cse560;

/**
 * Simulates a memory controller for the MMXI machine, decoupling the
 * implementation of memory from the {@link Interpreter}. Supports basic getting
 * and setting of two-byte values, as well as getting values from memory with
 * the sign extended. Also allows clients to determine the page number of a
 * given memory address.
 * <p>
 * <b>Model:</b> A zero-indexed array {@code M} of 2^16 two-byte values.
 *
 * @author Igor Tolkachev
 */
public abstract class Memory {
    /** The number of bits that make up a page number. */
    static final int PAGE_LEN = 7;

    /** The length of a word in memory in bits. */
    static final int WORD_LEN = 16;

    /** The highest possible address. */
    static final int MAX_ADDR = (int) Math.pow(2, MemoryImp.WORD_LEN) - 1;

    /** The highest possible value for a memory cell. */
    static final int MAX_VALUE = (int) Math.pow(2, MemoryImp.WORD_LEN) - 1;

    /**
     * Returns the page number of the given address.
     * <p>
     * Requires: {@code 0 <= addr <= MAX_ADDR}
     *
     * @param addr
     *            The address of memory whose page number to return.
     * @return The page number of addr.
     */
    public static int getPageNumber(final int addr) {
        return addr >> (Memory.WORD_LEN - Memory.PAGE_LEN);
    }

    /**
     * Returns the value at {@code M[addr]} as an integer.
     * <p>
     * Requires: {@code 0 <= addr <= MAX_ADDR}
     *
     * @param addr
     *            The address of the memory cell to return.
     *
     * @return {@code M[addr]}
     */
    abstract int get(final int addr);

    /**
     * Returns the value at {@code M[addr]} as a sign-extended integer.
     * <p>
     * Requires: {@code 0 <= addr < 2^16}
     *
     * @param addr
     *            The address of the memory cell to return.
     *
     * @return {@code M[addr]} with sign extension.
     */
    abstract int getSigned(final int addr);

    /**
     * Sets an address in memory to a given value. If {@code value > 0xffff}, it
     * will be truncated to two bytes.
     * <p>
     * Requires: {@code 0 <= addr < 2^16}
     * <p>
     * Ensures: {@code M[addr] = value & 0x0000ffff}
     *
     * @param addr
     *            The address of the memory cell to update.
     * @param value
     *            The value to place in the designated memory cell.
     */
    abstract void set(int addr, int value);
}
