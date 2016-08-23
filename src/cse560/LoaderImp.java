package cse560;

/**
 * Parses an MMXI object file and uses the results to initialize a specified
 * instance of {@link Interpreter}.
 * <p>
 * This particular implementation is intentionally lax. Any record that does not
 * start with a header code ('H', 'T', or 'E') is ignored. Additionally, any
 * text beyond the expected length for particular record (e.g., nine in the case
 * of a text record) is also ignored.
 * <p>
 * Finally, LoaderImp will not print any warnings if the object file contains
 * multiple text records for the same address.
 *
 * @author Igor Tolkachev
 */
public class LoaderImp implements Loader {

    /** True iff a header record has been found. */
    private boolean headerRead = false;

    /** A reference to the Interpreter object that this Loader will initialize. */
    private final Interpreter machine;

    /** Minimum address of segment based on header record. */
    private int minAddrVal = 0;

    /** Maximum address of segment based on header record. */
    private int maxAddrVal = 0;

    /** Value to set into memory. */
    private int memValue = 0;

    /** Segment name of the object file. Remains null until header read. */
    private String segmentName = null;

    /**
     * Whether the last read record was a header ('H'), text ('T') or execute
     * ('E') record.
     */
    private char type = 0;

    /** True iff the most recently read instruction was well-formed. */
    private boolean validIns = false;

    /**
     * Initializes a new LoaderImp instance to initialize {@code machine}.
     *
     * @param machine
     *            The Interpreter instance to be initialized.
     */
    public LoaderImp(Interpreter machine) {
        this.machine = machine;
    }

    /**
     * Sets {@code type} to the record type of {@code record}.
     * <p>
     * Ensures: {@code type = record[0]} if {@code |record| > 0}. Else
     * {@code type = 0}.
     *
     * @param record
     *            The record whose type will be determined.
     */
    private void checkType(String record) {
        if (record.length() > 0) {
            type = record.charAt(0);
        } else {
            type = 0;
        }
    }

    /**
     * Calculates the maximum address for this segment based on the given header
     * record.
     * <p>
     * Requires: {@code type = 'H'}
     *
     * @param record
     *            The header record to parse.
     * @return The starting address + the declared segment length.
     */
    private int getMaxAddrVal(String record) {

        try {
            String strAddr = record.substring(7, 11);
            maxAddrVal = Integer.parseInt(strAddr, 16)
                    + Integer.parseInt(record.substring(11, 15), 16) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Error 104: Malformed record encountered.");
            System.exit(1);
        } catch (StringIndexOutOfBoundsException s) {
            System.out
                    .println("Error 105: Malformed record encountered.  Record length requirements not met.");
            System.exit(1);
        }

        return maxAddrVal;
    }

    /**
     * Determines whether the given address is within the bounds for this
     * segment.
     *
     * @param addr
     *            The address to verify.
     * @return {@code minAddrVal <= addr <= maxAddrVal}
     */
    private boolean isValid(int addr) {
        boolean result = false;

        if (minAddrVal <= addr && addr <= maxAddrVal) {
            result = true;
        }

        return result;
    }

    @Override
    public String getSegmentName() {
        return segmentName;
    }

    @Override
    public void parseString(String record) {

        int addr = 0;

        // Check the type of instruction
        checkType(record);

        switch (type) {
        case 'H':
            // Check to make sure that only 1 header line is read in.

            try {

                if (!headerRead) {
                    headerRead = true;
                    minAddrVal = Integer.parseInt(record.substring(7, 11), 16);
                    maxAddrVal = getMaxAddrVal(record);
                    segmentName = record.substring(1, 7);
                } else {
                    System.out.println("Error 102: Too many header records.");
                    System.exit(1);
                }
            } catch (StringIndexOutOfBoundsException s) {
                System.out
                        .println("Error 105: Malformed record encountered. Record length requirements not met.");
                System.exit(1);
            }
            break;

        case 'T':
            if (!headerRead) {
                System.out
                        .println("Error 101: Text record found before header.");
                System.exit(1);
            } else {
                try {
                    // Pull the memory location from the string.
                    String strAddr = record.substring(1, 5);

                    // Convert the memory location a string to an integer.
                    addr = Integer.parseInt(strAddr, 16);
                } catch (NumberFormatException e) {
                    System.out
                            .println("Error 104: Malformed record encountered.");
                    System.exit(1);
                } catch (StringIndexOutOfBoundsException s) {
                    System.out
                            .println("Error 105: Malformed record encountered.  Record length requirements not met.");
                    System.exit(1);
                }

                // Compare the memory location to the max location to check if
                // it is a
                // valid instruction.
                validIns = isValid(addr);

                // Error Checking: If ins addr > header max mem addr.
                if (!validIns) {
                    System.out
                            .println("Error 100: Instruction address is set outside of the maximum memory address set in the header.");
                    System.exit(1);
                }

                try {
                    // Get the memory value from the string passed through.
                    memValue = Integer.parseInt(record.substring(5, 9), 16);
                } catch (NumberFormatException e) {
                    System.out
                            .println("Error 104:  Malformed record encountered.");
                    System.exit(1);
                } catch (StringIndexOutOfBoundsException s) {
                    System.out
                            .println("Error 105: Malformed record encountered. Record length requirements not met.");
                    System.exit(1);
                }

                // Set M[addr] = memValue
                machine.setMemory(addr, memValue);
            }

            break;

        case 'E':
            if (!headerRead) {
                System.out
                        .println("Error 103: End record found before header.");
                System.exit(1);
            } else {

            	if (Integer.parseInt(record.substring(1, 5), 16) < minAddrVal || Integer.parseInt(record.substring(1,5), 16) > maxAddrVal){
 
            		System.out.println("Error 106: End record execution address is outside the boundaries set by the header record.");
            		System.exit(1);
            		
            	}
            	else {

               		try {
                    	machine.setPC(Integer.parseInt(record.substring(1, 5), 16));
                	} catch (NumberFormatException e) {
                		System.out
                    		.println("Error 104: Malformed record encountered.");
                    	System.exit(1);
                	} catch (StringIndexOutOfBoundsException s) {
                    	System.out
                    		.println("Error 105: Malformed record encountered. Record length requirements not met.");
                    	System.exit(1);
                	}
            	}
            }
            break;

        default:
            // If a line does not start with H, T, or E, ignore it as if it were
            // a comment.
        }
    }
}
