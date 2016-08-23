package cse560;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MemoryTest {
    /** The instance of a Memory object to test. */
    private final Memory memory = new MemoryImp();

    // -----------------------------------------------------------------------
    // Testing that empty cells are zero.
    // -----------------------------------------------------------------------

    /**
     * In a fresh Memory object, M[0] = 0.
     */
    @Test
    public void doesM0equal0() {
        assertEquals("M[0] = 0", memory.get(0), 0);
    }

    /**
     * In a fresh Memory object, memory cells start at zero.
     *
     * This test checks a sampling of cells within memory.
     */
    @Test
    public void doCellsStartAtZero() {
        for (int i = 0; i < Memory.MAX_ADDR; i += 1024) {
            assertEquals("M[" + i + "] = 0", memory.get(i), 0);
        }
    }

    /**
     * In a fresh Memory object, the last element is also zero.
     */
    @Test
    public void doesMMaxequal0() {
        assertEquals("M[MAX_ADDR] = 0", memory.get(Memory.MAX_ADDR), 0);
    }

    // -----------------------------------------------------------------------
    // Testing set.
    // -----------------------------------------------------------------------

    /**
     * If we set M[0] to a value, it takes on that value.
     */
    @Test
    public void doesSetWorkAtZero() {
        memory.set(0, 42);
        assertEquals("M[0] = 42", memory.get(0), 42);
    }

    /**
     * If we set some addresses to a value, they take on that value.
     */
    @Test
    public void doesSetWorkInGeneral() {
        for (int i = 0; i < Memory.MAX_ADDR; i += 1024) {
            memory.set(i, 1066);
            assertEquals("M[" + i + "] = 0", memory.get(i), 1066);
        }
    }

    /**
     * If we set M[Memory.MAX_ADDR] to a value, it takes on that value.
     */
    @Test
    public void doesSetWorkAtMaxAddr() {
        memory.set(Memory.MAX_ADDR, 23);
        assertEquals("M[MAX_ADDR] = 23", memory.get(Memory.MAX_ADDR), 23);
    }

    /**
     * If we set M[0] to a value and then set it to something else, it takes on
     * the latter value.
     */
    @Test
    public void doubleSetAtZero() {
        memory.set(0, 1234);
        memory.set(0, 0x1234);
        assertEquals("M[0] = 0x1234", memory.get(0), 0x1234);
    }

    /**
     * If we set M[0] to a value and then set it to something else, it takes on
     * the latter value.
     */
    @Test
    public void doubleSetWithinMemory() {
        for (int i = 0; i < Memory.MAX_ADDR; i += 2048) {
            memory.set(i, 1234);
            memory.set(i, 0x1234);
            assertEquals("M[" + i + "] = 0x1234", memory.get(i), 0x1234);
        }
    }

    /**
     * If we set M[MAX_ADDR] to a value and then set it to something else, it
     * takes on the latter value.
     */
    @Test
    public void doubleSetAtMaxAddr() {
        memory.set(Memory.MAX_ADDR, 1234);
        memory.set(Memory.MAX_ADDR, 0x1234);
        assertEquals("M[0] = 0x1234", memory.get(Memory.MAX_ADDR), 0x1234);
    }

    // -----------------------------------------------------------------------
    // Testing getSigned
    // -----------------------------------------------------------------------

    /**
     * Getting from M[0] from unmodified memory should be zero.
     */
    @Test
    public void testGetAtZero() {
        assertEquals("M[0] = 0x0", 0, memory.getSigned(0));
    }

    /**
     * Getting from a non-endpoint value from unmodified memory should be zero.
     */
    @Test
    public void testGetInMiddle() {
        assertEquals("M[0x1234] = 0", 0, memory.getSigned(0x1234));
    }

    /**
     * If M unmodified, M[MAX_ADDR] = 0.
     */
    @Test
    public void testGetAtMaxAddr() {
        assertEquals("M[MAX_ADDR] = 0", 0, memory.getSigned(Memory.MAX_ADDR));
    }

    /**
     * If M[0] is set to -1, M[0] = -1 when retrieved with getSigned.
     */
    @Test
    public void basicSignExtension() {
        memory.set(0, -1);
        assertEquals("M[0] = -1", -1, memory.getSigned(0));
    }

    /**
     * If M[0] is set a mid-range negative value, getSigned works properly.
     */
    @Test
    public void midrangeSignExtension() {
        memory.set(0, -1066);
        assertEquals("M[0] = -1066", -1066, memory.getSigned(0));
    }

    /**
     * Check getSigned(0) when M[0] is one of a range of values near the maximum
     * negative values.
     */
    @Test
    public void getSignedOnMostNegativeNumber() {
        int maxNegative = -(int) Math.pow(2, Memory.WORD_LEN - 1);
        memory.set(0, maxNegative);

        assertEquals("M[0] = -(2^(WORD_LEN - 1))", memory.getSigned(0),
                maxNegative);
    }

    /**
     * Check getSigned when M[0] is the most positive allowed value.
     */
    @Test
    public void getSignedOnMostPositiveNumber() {
        int maxPositive = (int) Math.pow(2, Memory.WORD_LEN - 1) - 1;
        memory.set(0, maxPositive);

        assertEquals("M[0] = 2^(WORD_LEN -1 ) - 1", memory.getSigned(0),
                maxPositive);
    }

    // TODO: Add tests for get.

    // -------------------------------------------------------------------------
    // Miscellaneous tests
    // -------------------------------------------------------------------------

    /**
     * The set/get functions properly truncate values longer than two bytes.
     */
    @Test
    public void testTruncation() {
        memory.set(0, 0xffff0000);
        assertEquals("M[0] = 0", memory.get(0), 0);
    }
}
