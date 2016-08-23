package cse560;

/**
 * Simulates the loader component of the MMXI Machine. The loader receives a
 * string value from the {@link Simulator} through the {@code parseString}
 * method. It then determines whether or not the string value is a Header
 * Record, Text Record, or End Record.
 * <p>
 * If a record is malformed in some way, the Loader will print an error and
 * terminate the program. The errors that need to be detected are as follows:
 * <ul>
 * <li>100: Instruction address is set outside of the maximum memory address set
 * in the header.</li>
 * <li>101: Text record found before a header record was read.</li>
 * <li>102: Too many header records.</li>
 * <li>103: End record found before a header record was read.</li>
 * <li>104: Malformed record read.</li>
 * <li>105: Malformed record read. Record did not meet the length requirements.</li>
 * <li>106: End record execution address is outside the boundaries set by the header record.</li>
 * </ul>
 * <p>
 * Constructors: {@code Loader(Interpreter i)} - Initializes a new Loader
 * instance that will initialize {@code i}.
 *
 * @author Igor Tolkachev
 */

public interface Loader {

    /**
     * Parses the string that is passed to the {@link Loader}. Checks to make
     * sure the instruction is inside the memory boundaries and sets the
     * instruction to its proper memory location.
     *
     * @param record
     *            The string that is passed into the {@link Loader}.
     */
    void parseString(String record);

    /**
     * Returns the segment name if a header record was found.
     *
     * @return Segment name if header record was found. Otherwise, null.
     */
    String getSegmentName();
}