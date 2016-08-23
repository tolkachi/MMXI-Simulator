package cse560;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the Memory interface using a mapping from addresses to values. By
 * default, if an address is not contained in the map its value is assumed to be
 * zero. This convention is enforced by removing addresses from the map if the
 * client sets its value to zero, thereby reducing memory overhead.
 * <p>
 * Convention: For all addresses {@code addr}, if
 * {@code M[addr] = 0, (addr, value) is not in M)}.
 *
 * @author Igor Tolkachev
 *
 */
public final class MemoryImp extends Memory {
    /** The highest possible positive value for a memory cell. */
    private static final int MAX_POSITIVE_VALUE = (int) Math.pow(2,
            Memory.WORD_LEN - 1) - 1;

    /** The Map-based representation of memory. */
    private final Map<Integer, Integer> memory = new HashMap<Integer, Integer>();

    @Override
    public int get(final int addr) {
        // If the address has already been assigned a value, return it.
        // Otherwise, return zero.
        if (this.memory.containsKey(addr)) {
            return this.memory.get(addr);
        } else {
            return 0;
        }
    }

    @Override
    public int getSigned(final int addr) {
        // If the address has already been assigned a value, return it
        // with sign extension.
        //
        // Otherwise, return zero.
        if (this.memory.containsKey(addr)) {
            int value = this.memory.get(addr);

            // If the value is greater than the maximum possible
            // positive value, perform the necessary two's complement
            // adjustment.
            //
            // Otherwise it can be returned as it.
            if (value > MemoryImp.MAX_POSITIVE_VALUE) {
                return value - Memory.MAX_VALUE - 1;
            } else {
                return value;
            }
        } else {
            return 0;
        }
    }

    @Override
    public void set(final int addr, final int value) {
        // If the value is a zero, just remove it from the map. The
        // default return value is zero for undefined entries, so this
        // will work out.
        //
        // Otherwise, set M[addr] = value, truncated to two bytes.
        if (value == 0) {
            this.memory.remove(addr);
        } else {
            this.memory.put(addr, value & Memory.MAX_VALUE);
        }
    }
}
