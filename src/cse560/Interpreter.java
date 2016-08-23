package cse560;

/**
 * Interpreter objects serve as an emulator for the MMXI architecture.
 * Implementers of this interface are responsible for representing the machine
 * state and simulating the MMXI's fetch/execute loop. Note that while
 * implementers must follow the design outlined in the MMXI specification
 * exactly, certain decisions can vary on an implementation by implementation
 * basis. See {@link ImplementationImp} for specifics.
 * <p>
 * <b>Model:</b>
 * <ul>
 * <li>{@code PC}: a two-byte value</li>
 * <li>{@code CCR}: a 3-tuple of booleans (N, Z, P)</li>
 * <li>{@code R}: a zero-indexed array of eight two-byte values</li>
 * <li>{@code M}: a zero-indexed array of 2^16 two-byte values</li>
 * <li>{@code halted}: a boolean value.</li>
 * </ul>
 *
 * <p>
 * <b>Initial state:</b>
 * <ul>
 * <li>{@code PC = 0x0000}</li>
 * <li>{@code CCR = (false, true, false)}</li>
 * <li>{@code R[i] = 0 for all i in [0, 8)}</li>
 * <li>{@code M[i] = 0 for all i in [0, 2^16)}</li>
 * <li>{@code halted = false}</li>
 * </ul>
 *
 * @author Igor Tolkachev
 */

public interface Interpreter {
    /** Number of general-purpose registers. */
    int NUM_REGS = 8;

    /**
     * Sets an address in memory to a given value. If {@code value > 0xffff}, it
     * will be truncated to two bytes.
     * <p>
     * Requires: {@code 0 <= addr < 2^16} and {@code halted = false}
     * <p>
     * Ensures: {@code M[addr] = value & 0x0000ffff}
     *
     * @param addr
     *            The address of the memory cell to update.
     * @param value
     *            The value to place in the designated memory cell.
     */
    void setMemory(int addr, int value);

    /**
     * Returns the value at {@code M[addr]} as an integer.
     * <p>
     * Requires: {@code 0 <= addr < 2^16} and {@code halted = false}
     *
     * @param addr
     *            The address of the memory cell to return.
     *
     * @return {@code M[addr]}
     */
    int getMemory(int addr);

    /**
     * Returns the value of {@code PC} as an integer.
     * <p>
     * Requires: {@code halted = false}
     *
     * @return {@code PC}
     */
    int getPC();

    /**
     * Changes the value of {@code PC} to {@code value}.
     * <p>
     * Requires: {@code 0 <= value < Memory.MAX_ADDR}
     * <p>
     * Ensures: {@code PC = value}
     *
     * @param value
     *            The new value of {@code PC}
     *
     */
    void setPC(int value);

    /**
     * Returns the value of the active bit in the CCR.
     * <p>
     * Requires: {@code halted = false}
     * <p>
     * Ensures: CCR = CCR.N iff N, CCR.Z iff Z, and CCR.P iff P
     *
     * @return {@code CCR}
     */
    CCR getCCR();

    /**
     * Returns the value at {@code R[n]} as an integer.
     * <p>
     * Requires: {@code 0 <= n < 8} and {@code halted = false}
     *
     * @param n
     *            Which register's value to return.
     *
     * @return {@code R[n]}
     */
    int getRegister(int n);

    /**
     * Prints the last executed instruction in user-readable text format. This
     * format mimics the style of MMXI assembly. For example, the instruction
     * "0xF0025" would be output at "TRAP x25". However, branch instructions are
     * not fully parsed, instead being represented as "BRx".
     *
     * @return the last instruction as string
     */
    String getLastInstruction();

    /**
     * Performs one fetch-execute cycle on the MMXI machine using the current
     * machine state.
     * <p>
     * Requires: {@code halted = false}
     * <p>
     * Ensures: {@code PC} is incremented or set according to the operand of a
     * jump instruction.
     */
    void step();

    /**
     * Returns whether the machine has encountered a "halt" trap.
     *
     * @return {@code halted}
     */
    boolean isHalted();
}
