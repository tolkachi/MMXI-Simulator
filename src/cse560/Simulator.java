package cse560;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Main program of the MMXI Simulator. Provides a command-line based user
 * interface and coordinates loading MMXI object files and running the
 * interpreter.
 * <p>
 * The command-line arguments accepted by the Simulator are: The command-line
 * options accepted by Simulator are:
 * <ul>
 * <li>{@code -q} - Run in quiet mode. This is the default setting.</li>
 * <li>{@code -t} - Run in trace mode.</li>
 * <li>{@code -s} - Run in step mode.</li>
 * <li>{@code -M N} - Terminate execution after $N$ instructions. Default: 1000.
 * </li>
 * <li>{@code -f file} - The name of the object file to execute. (Required)</li>
 * </ul>
 * If a required option is missing, a nonexistent option is provided (e.g., "
 * {@code -G}"), or a provided argument is malformed (e.g., "{@code -M platypus}
 * "), the program will print a usage message and exit. This will also occur if
 * more than one execution mode is selected, as in "{@code -q -t -f foo.o}".
 * <p>
 * Since most of the "work" of the program is done in other classes, the
 * structure of Simulator is fairly simple. The #main# method is divided into
 * three logical sections (excluding variable declaration): command-line
 * argument parsing, object-file loading, and running the simulation.
 * <p>
 * Command-line arguments are parsed using the JOpt Simple library. A link to
 * the documentation for JOpt Simple is provided in the introduction to the
 * Programmer's Guide.
 *
 * @author Igor Tolkachev
 *
 */
public final class Simulator {
    /** Default limit on number of instructions to execute. */
    private static final int DEFAULT_MAX_STEPS = 1000;

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------

    /**
     * Private constructor to prevent instantiation.
     */
    private Simulator() {
        // Do nothing.
    }

    /**
     * Prints the current memory page of the given Interpreter object as
     * hexadecimal.
     *
     * @param machine
     *            The machine to fetch the current memory page from.
     */
    private static void printCurrentPage(final Interpreter machine) {
        final int outputRows = 32; // Number of rows to output per "chunk"
        final int outputCols = 8; // Number of columns for each row (in words)

        int page = Memory.getPageNumber(machine.getPC()) << (Memory.WORD_LEN - Memory.PAGE_LEN);
        int offset = 0; // Start with offset zero and go from there

        // Print the memory page in two 16 x 16 chunks, with row and column
        // guides.
        for (int chunk = 0; chunk < 2; ++chunk) {
            // Allow space for the row guides.
            System.out.print("    ");

            // Print the column guides
            // Note that if the guide is wider than one character there will be
            // alignment issues.
            for (int col = 0; col < outputCols; ++col) {
                System.out.printf("%4x ", col);
            }
            System.out.println();

            // Print sixteen rows of memory, with each row beginning with the
            // offset.
            for (int row = 0; row < outputRows; ++row) {
                System.out.printf("%03x ", offset);

                // Print sixteen words of memory.
                for (int col = 0; col < outputCols; ++col) {
                    System.out
                            .printf("%04x ", machine.getMemory(page + offset));
                    ++offset;
                }
                System.out.println();
            }

            System.out.println();
        }
    }

    /**
     * Prints the last executed instruction, registers, CCR, and PC of the
     * provided Interpreter.
     *
     * @param machine
     *            The machine whose state should be printed.
     */
    public static void printState(final Interpreter machine) {
        // ... Print last executed instruction
        System.out.print("Last instruction: ");
        System.out.println(machine.getLastInstruction());

        // ... Print registers

        for (int i = 0; i < Interpreter.NUM_REGS; ++i) {
            System.out.printf("  R%x ", i);
        }

        System.out.println();

        for (int i = 0; i < Interpreter.NUM_REGS; ++i) {
            System.out.printf("%04x ", machine.getRegister(i));
        }

        System.out.println();

        // ... Print CCR

        System.out.printf("CCR: %s\n", machine.getCCR().toString());

        // ... Print instruction and PC
        // E.g., "PC: 2A4C    ADD R5, R0, R5"

        System.out.printf("PC: %04x\n", machine.getPC());
    }

    /**
     * Prints a usage message for the Simulator. This includes all supported
     * options and their defaults.
     */
    private static void printUsage() {
        System.out.println("Usage: java -jar \"MMXI Simulator.jar\" [options]");
        System.out.println("    -q          Run in quiet mode (Default)");
        System.out.println("    -t          Run in trace mode");
        System.out.println("    -s          Run in step mode");
        System.out
                .println("    (Only one of the above options may be selected)");
        System.out.println("    -M N        Stop execution after N steps");
        System.out.println("    -f file     Execute the object file \"file\"");
    }

    // -------------------------------------------------------------------------
    // MAIN
    // -------------------------------------------------------------------------

    /**
     * @param args
     *            Command-line arguments.
     */
    public static void main(final String[] args) {
        Interpreter machine = new InterpreterImp();
        Loader loader = new LoaderImp(machine);
        OptionParser optParser = new OptionParser("qstM:f:");
        OptionSet options;

        String segmentName; // Name of the segment from the object file.

        // Maximum number of instructions to execute.
        int maxSteps = Simulator.DEFAULT_MAX_STEPS;

        // Operation mode for the simulator: Quiet, Trace, or Step
        SimulatorMode mode = SimulatorMode.QUIET;

        // Counter for the number of instructions executed so far.
        int stepCount = 0;

        // BufferedReader to read input file
        BufferedReader input = null;
        File inputFile = null;

        // The number of times the execution mode was set.
        int modeOptionCount = 0;

        // ... Process command arguments.

        // Try to parse the options. If an error occurs during parsing, print
        // usage and exit.
        try {
            options = optParser.parse(args);

            // If the -s or -t option is set, change mode to step or trace,
            // respectively. If neither is set or (implicitly) if -q is set,
            // leave it in quiet mode.
            if (options.has("s")) {
                mode = SimulatorMode.STEP;
                ++modeOptionCount;
            }

            if (options.has("t")) {
                mode = SimulatorMode.TRACE;
                ++modeOptionCount;
            }

            if (options.has("q")) {
                ++modeOptionCount;
            }

            // If -M is set, get its argument and use it as the maximum number
            // of
            // instructions to execute.
            if (options.has("M")) {
                maxSteps = Integer.parseInt((String) options.valueOf("M"));
            }

            // If -f is NOT set, exit with a usage message. We can't do anything
            // without an input file.
            if (!options.has("f") || options.valueOf("f") == null) {
                System.out.println("Please specify an input file using -f.");
                Simulator.printUsage();
                System.exit(0);
            } else {
                inputFile = new File((String) options.valueOf("f"));
            }
        } catch (OptionException e) {
            Simulator.printUsage();
            System.exit(1);
        }

        // If more than one mode is selected, print a usage message and exit
        if (modeOptionCount > 1) {
            System.err.println("Please select exactly one execution mode.");
            Simulator.printUsage();
        }

        // ... Read object file into loader.

        if (!inputFile.exists()) {
            System.err.println("Error 200: Input file does not exist.");
            Simulator.printUsage();
            System.exit(1);
        }
        try {
            String inputLine;
            input = new BufferedReader(new FileReader(inputFile));

            // Read from input file until empty, passing each line to loader for
            // processing.
            while ((inputLine = input.readLine()) != null) {
                loader.parseString(inputLine);
            }
        } catch (IOException e) {
            System.err.println("Error 201: Could not read input file: " + e);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException e) {
                System.err.println("Error 202: Problem closing input file: "
                        + e);
                System.exit(1);
            }
        }

        // Get the segment name of the object file. If null, the object file was
        // missing a header record, so print an error and exit.

        segmentName = loader.getSegmentName();

        if (segmentName == null) {
            System.err.println("Error 203: Object file missing header record.");
            Simulator.printUsage();
            System.exit(1);
        }

        // If the PC is -1, no end record was found. Exit with error.
        if (machine.getPC() == -1) {
            System.err.println("Error 205: No end record found.");
            System.exit(1);
        }

        // ... Run simulation until complete.

        // If running in a non-quiet mode, print the current page of memory
        // before beginning execution.
        if (mode != SimulatorMode.QUIET) {
            Simulator.printCurrentPage(machine);
        }

        // Step the machine until it either halts or we reach the maximum step
        // count.
        while (!machine.isHalted() && stepCount < maxSteps) {
            // If operating in step mode, require user input before executing
            // the next instruction.
            if (mode == SimulatorMode.STEP) {
            //    System.console().readLine("Press ENTER to continue");
            }

            machine.step();

            if (mode != SimulatorMode.QUIET) {
                System.out.println();
                Simulator.printState(machine);
            }

            ++stepCount;
        }

        // Print error if loop terminated due to stepCount exceed maximum.
        if (stepCount >= maxSteps) {
            System.out.println("Error 204: Maximum instruction count reached.");
        }

        // If running in a non-quiet mode, print the current page of memory
        // after completing execution.
        if (mode != SimulatorMode.QUIET) {
            Simulator.printCurrentPage(machine);
        }
    }
}
