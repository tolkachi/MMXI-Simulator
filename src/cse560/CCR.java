package cse560;

/**
 * An enumeration defining the states the MMXI Condition Code Register (CCR) can
 * take on. Since only one bit can ever be active at a time, the complete state
 * of the CCR can be represented by storing the currently active bit.
 *
 * @author Igor Tolkachev
 *
 */
public enum CCR {
    /** CCR bit set to negative. */
    N,
    /** CCR bit set to zero. */
    Z,
    /** CCR bit set to positive. */
    P
}
