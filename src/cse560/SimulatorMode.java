package cse560;

/**
 * Represents the three different execution modes for the simulator.
 *
 * @author Igor Tolkachev
 */
public enum SimulatorMode {
    /**
     * Execute the program without interruption and without printing any state
     * data. This is the default mode..
     */
    QUIET,

    /**
     * After loading memory but before execution, print the current page of
     * memory. Then, after each instruction is executed, print the values of the
     * registers, the CCR, the PC, and a user-readable parse of the instruction
     * that just executed. After completion, print the current page of memory
     * again.
     */
    TRACE,

    /**
     * Identical to running in trace mode, except that the program will ask the
     * user to press ENTER before executing the next instruction.
     */
    STEP
}
