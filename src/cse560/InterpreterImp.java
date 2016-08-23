package cse560;

import java.util.Random;

/**
 * Provides an implementation of the Interpreter interface, emulating the MMXI
 * machine.
 * <p>
 * Implementation details:
 * <ul>
 * <li>When the PC reaches the end of memory, InterpreterImp will print a
 * warning and loop back to 0.
 * </ul>
 * <p>
 * Correspondence:
 * <ul>
 * <li>{@code PC = pc}</li>
 * <li>{@code CCR = (true, false, false) if ccr = CCR.N}<br />
 * {@code CCR = (false, true, false) if ccr = CCR.Z}<br />
 * {@code CCR = (false, false, true) if ccr = CCR.P}</li>
 * <li> {@code R[i] = registers[i] for all i in [0,8)}</li>
 * </ul>
 *
 * @author  Igor Tolkachev
 *
 */
public final class InterpreterImp implements Interpreter {

    // ------------------------------------------------------------------------
    // PRIVATE VARIABLES
    // ------------------------------------------------------------------------

    /** Representation of memory as a Map. */
    private final Memory memory = new MemoryImp();

    /** Program Counter */
    private int pc;

    /** Condition Code Register */
    private CCR ccr = CCR.Z;

    /** the 8 registers */
    private final int[] registers = new int[8];

    /** state of the machine */
    private boolean halted;

    /** last executed instruction */
    private String lastInstruction;

    // -------------------------------------------------------------------------
    // PRIVATE OPERATIONS
    // -------------------------------------------------------------------------

    // NOTE: these operations have been given package access in order to be
    // able to do white box testing with JUnit

    /**
     * Returns the bit range of value specified by (start, end). Note that the
     * zeroth bit is the rightmost bit.
     * <p>
     * Requires:
     * <ul>
     * <li>{@code 0 <= end <= start < 32}</li>
     * </ul>
     * <p>
     * Ensures:
     * <ul>
     * <li>All bits higher than {@code start - end} are 0.</li>
     * <li>{@code BIT_RANGE(#value, start, end) = }<br/>
     * {@code BIT_RANGE(bitRange, start - end, 0)}</li>
     * </ul>
     */
    int bitRange(int value, int start, int end) {
        int mask = 0xFFFFFFFF;

        // Move the rightmost bit to position 0
        value = value >> end;

        // Create a mask.
        mask = mask >>> (31 - (start - end));

        return value & mask;
    }

    /**
     * Truncates {@code value} to maximum value.
     */
    int twoBytes(int value) {
        return value & Memory.MAX_VALUE;
    }

    /**
     * Returns bits 15:12 from a 16-bit value.
     */
    int opcode(int instr) {
        return bitRange(instr, 15, 12);
    }

    /**
     * Returns the address on the same page as the PC with the offset of
     * {@code instr}.
     *
     * @param instr
     *            16-bit instruction containing the offset
     *
     * @return {@code PC[15:9]} concatenated with {@code instr[8:0]}
     */
    int offsetAddress(int instr) {
        int page = bitRange(pc, 15, 9) << 9;

        if (page != bitRange(pc - 1, 15, 9) << 9) {
            System.out.println("Warning 050: Current instruction is at last "
                    + "address of the current memory page. Address formed from"
                    + " operand is on the next page.");
        }
        return (bitRange(pc, 15, 9) << 9) + bitRange(instr, 8, 0);
    }

    /**
     * Sets the CCR based on the value of {@code R[n]}.
     * <p>
     * Requires: {@code 0 <= n < 8}
     * <p>
     * Ensures: CCR = CCR.N iff R[n] < 0, CCR.Z iff R[n] = 0, CCR.P otherwise
     */
    void setCCR(int n) {
        int value = registers[n];

        if (value == 0) {
            ccr = CCR.Z;
        }
        // check sign bit to see if it's negative
        else if (bitRange(value, 15, 15) == 1) {
            ccr = CCR.N;
        } else {
            ccr = CCR.P;
        }
    }

    /**
     * Sets register {@code n} to {@code value} truncated 16 bits.
     * <p>
     * Requires: {@code 0 <= n < 8}
     */
    void setRegister(int n, int value) {
        registers[n] = twoBytes(value);
    }

    // -------------------------------------------------------------------------
    // PUBLIC OPERATIONS
    // -------------------------------------------------------------------------

    @Override
    public void setMemory(int addr, int value) {
        memory.set(addr, value);
    }

    @Override
    public int getMemory(int addr) {
        return memory.get(addr);
    }

    @Override
    public int getPC() {
        return pc;
    }

    @Override
    public void setPC(int value) {
        pc = value;
    }

    @Override
    public CCR getCCR() {
        return ccr;
    }

    @Override
    public int getRegister(int n) {
        return registers[n];
    }

    @Override
    public String getLastInstruction() {
        // The last executed instruction is formatted as valid assembly
        // language for the MMXI machine
        return lastInstruction;
    }

    @Override
    public boolean isHalted() {
        return halted;
    }

    @Override
    public void step() {
        // retrieve instruction from memory, increment the PC, and look at the
        // opcode
        int instr = memory.get(pc);

        ++pc;
        if (pc > Memory.MAX_ADDR) {
            System.out.println("Warning 051: Maximum address exceeded. "
                    + "Resetting PC to 0.");
            pc = 0;
        }
        switch (opcode(instr)) {
        // ------------------------------
        // Data processing instructions
        // ------------------------------
        case 9: // **** NOT
        {
            // set DR to the bit complement of SR, set CCR, format instruction
            // as user-readable String
            int DR = bitRange(instr, 11, 9), SR = bitRange(instr, 8, 6);

            setRegister(DR, ~registers[SR]);
            setCCR(DR);
            lastInstruction = "NOT R" + DR + ",R" + SR;
            break;
        }
        case 1: // **** ADD
        {
            // set DR to the addition of SR1 and SR2 or sign-extended imm5
            // set CCR
            int DR = bitRange(instr, 11, 9), SR1 = bitRange(instr, 8, 6), op1 = registers[SR1], result;

            if (bitRange(instr, 5, 5) == 0) {
                int SR2 = bitRange(instr, 2, 0), op2 = registers[SR2];

                result = op1 + op2;
                // warn for overflow if operands are of same sign and result is
                // of different sign
                if ((bitRange(op1, 15, 15) == bitRange(op2, 15, 15))
                        && (bitRange(op1, 15, 15) != bitRange(result, 15, 15))) {
                    System.out
                            .println("Warning 052: Overflow during addition.");
                }
                setRegister(DR, result);
                lastInstruction = "ADD R" + DR + ",R" + SR1 + ",R" + SR2;
            } else {
                // sign-extend to 32 bits
                int imm5 = (bitRange(instr, 4, 0) << 27) >> 27;
                result = op1 + imm5;
                // warn for overflow if operands are of same sign and result is
                // of different sign
                if ((bitRange(op1, 15, 15) == bitRange(imm5, 15, 15))
                        && (bitRange(op1, 15, 15) != bitRange(result, 15, 15))) {
                    System.out
                            .println("Warning 052: Overflow during addition.");
                }
                setRegister(DR, result);
                lastInstruction = "ADD R" + DR + ",R" + SR1 + ",0x"
                        + Integer.toHexString(imm5 & 0xFFFF);
            }
            setCCR(DR);
            break;
        }
        case 5: // **** AND
        {
            // set DR to the lowest 16 bits of bitwise AND of SR1 and SR2 or
            // sign-extended imm5, set CCR
            int DR = bitRange(instr, 11, 9), SR1 = bitRange(instr, 8, 6);

            if (bitRange(instr, 5, 5) == 0) {
                int SR2 = bitRange(instr, 2, 0);

                setRegister(DR, registers[SR1] & registers[SR2]);
                lastInstruction = "AND R" + DR + ",R" + SR1 + ",R" + SR2;
            } else {
                // sign-extend to 32 bits
                int imm5 = (bitRange(instr, 4, 0) << 27) >> 27;

                setRegister(DR, registers[SR1] & imm5);
                lastInstruction = "AND R" + DR + ",R" + SR1 + ",0x"
                        + Integer.toHexString(imm5 & 0xFFFF);
            }
            setCCR(DR);
            break;
        }
        // ------------------
        // Load instructions
        // ------------------
        case 14: // **** LEA
        {
            // set destination register to offset address from pgoffset9
            int DR = bitRange(instr, 11, 9), addr = offsetAddress(instr);

            setRegister(DR, addr);
            lastInstruction = "LEA R" + DR + ",0x" + Integer.toHexString(addr);
            setCCR(DR);
            break;
        }
        case 2: // **** LD
        {
            // set destination register to value at offset address from
            // pgoffset9
            int DR = bitRange(instr, 11, 9), addr = offsetAddress(instr);

            setRegister(DR, memory.get(addr));
            lastInstruction = "LD R" + DR + ",0x" + Integer.toHexString(addr);
            setCCR(DR);
            break;
        }
        case 10: // **** LDI
        {
            // set destination register to value at address at offset address
            // from pgoffset9
            int DR = bitRange(instr, 11, 9), addr = offsetAddress(instr);

            setRegister(DR, memory.get(memory.get(addr)));
            lastInstruction = "LDI R" + DR + ",0x" + Integer.toHexString(addr);
            setCCR(DR);
            break;
        }
        case 6: // **** LDR
        {
            // set destination register to value at address BaseR + index6
            int DR = bitRange(instr, 11, 9), BaseR = bitRange(instr, 8, 6), index6 = bitRange(
                    instr, 5, 0), address = registers[BaseR] + index6;
            setRegister(DR, memory.get(address));
            lastInstruction = "LDR R" + DR + ",R" + BaseR + ",0x"
                    + Integer.toHexString(index6);
            setCCR(DR);
            break;
        }
        // --------------------
        // Store instructions
        // --------------------
        case 3: // **** ST
        {
            // set memory at address from pgoffset9 to value at source register
            int SR = bitRange(instr, 11, 9), addr = offsetAddress(instr);

            memory.set(addr, registers[SR]);
            lastInstruction = "ST R" + SR + ",0x" + Integer.toHexString(addr);
            break;
        }
        case 11: // **** STI
        {
            // set memory at address at offset address from pgoffset9 to value
            // at source register
            int SR = bitRange(instr, 11, 9), addr = offsetAddress(instr);

            memory.set(memory.get(addr), registers[SR]);
            lastInstruction = "STI R" + SR + ",0x" + Integer.toHexString(addr);
            break;
        }
        case 7: // **** STR
        {
            // set memory at address BaseR + index6 to value of source register
            int SR = bitRange(instr, 11, 9), BaseR = bitRange(instr, 8, 6), index6 = bitRange(
                    instr, 5, 0), address = registers[BaseR] + index6;
            memory.set(address, registers[SR]);
            lastInstruction = "STR R" + SR + ",R" + BaseR + ",0x"
                    + Integer.toHexString(index6);
            break;
        }
        // ------------------------------
        // Flow of control instructions
        // ------------------------------
        case 0: // **** BRx
        {
            int N = bitRange(instr, 11, 11), Z = bitRange(instr, 10, 10), P = bitRange(
                    instr, 9, 9);

            // branch (set PC to address from pgoffset9) if any of specified
            // CCR bits in the instruction are set
            if ((N == 1 && ccr == CCR.N) || (Z == 1 && ccr == CCR.Z)
                    || (P == 1 && ccr == CCR.P)) {
                int addr = offsetAddress(instr);

                pc = addr;
                lastInstruction = "BRx " + N + "," + Z + "," + P + ",0x"
                        + Integer.toHexString(addr);
            } else // **** NOP
            {
                lastInstruction = "NOP";
            }
            break;
        }
        case 15: // **** TRAP
        {
            switch (bitRange(instr, 7, 0)) // trapvect8
            {
            case 0x21: // OUT
            {
                // print character from R0[7:0]
                System.out.print((char) bitRange(registers[0], 7, 0));
                lastInstruction = "TRAP x21";
                break;
            }
            case 0x22: // PUTS
            {
                int currentAddr = registers[0];
                int currentChar = bitRange(memory.get(currentAddr), 7, 0);

                while (currentChar != 0) {
                    System.out.print((char) currentChar);
                    ++currentAddr;
                    currentChar = bitRange(memory.get(currentAddr), 7, 0);
                }
                lastInstruction = "TRAP x22";
                break;
            }
            case 0x23: // IN
            {
                // prompt user, read char from console, and store to R0.
                // set CCR
                try {
                    String charString = System.console().readLine(
                            "\nPlease " + "enter ASCII character:");
                    char character = charString.charAt(0);

                    if (charString.length() != 0) {
                        System.out
                                .println("Error 001: Invalid ASCII character.");
                    }
                    setRegister(0, character);
                } catch (NumberFormatException e) {
                    System.err.print("Error 001: Invalid ASCII character.");
                }
                setCCR(0);
                lastInstruction = "TRAP x23";
                break;
            }
            case 0x25: // HALT
            {
                // halt execution and print message
                halted = true;
                lastInstruction = "TRAP x25";
                System.out.println("\nExecution halted.");
                break;
            }
            case 0x31: // OUTN
            {
                // sign-extend and print R0 as decimal integer
                System.out.print((registers[0] << 16) >> 16);
                lastInstruction = "TRAP x31";
                break;
            }
            case 0x33: // INN
            {
                // prompt user, read byte from console, and store to R0.
                // set CCR
                try {
                    String charString = System.console().readLine(
                            "\nPlease " + "enter 16-bit integer:");
                    int result = Integer.parseInt(charString);

                    if (result < -32768 || result > 32767) {
                        System.out
                                .println("Error 002: Invalid 16-bit integer.");
                    }
                    setRegister(0, result);
                    setCCR(0);
                } catch (NumberFormatException e) {
                    System.err.print("Error 002: Invalid 16-bit integer.");
                }
                lastInstruction = "TRAP x33";
                break;
            }
            case 0x43: // RND
            {
                // generate 'random' number between -32768 and 32767 and store
                // to R0. set CCR
                Random generator = new Random();

                setRegister(0, generator.nextInt(Memory.MAX_VALUE + 1)
                        - (Memory.MAX_VALUE + 1) / 2);
                setCCR(0);
                lastInstruction = "TRAP x43";
                break;
            }
            default: // error
            {
                System.out.println("Error 003: Unsupported trap vector.");
            }
                // now set R7 to the PC
                setRegister(7, pc);
            }
            break;
        }
        case 4: // **** JSR
        {
            // set PC to address from pgoffset9. if L is set, store PC to R7
            // first
            int jumpAddress = offsetAddress(instr), L = bitRange(instr, 11, 11);

            if (L == 1) {
                setRegister(7, pc);
            }
            pc = jumpAddress;
            lastInstruction = "JSR " + L + ",0x"
                    + Integer.toHexString(jumpAddress);
            break;
        }
        case 12: // **** JSRR
        {
            // set PC to address BaseR + index6. if L is set, store PC to R7
            // first
            int BaseR = bitRange(instr, 8, 6), index6 = bitRange(instr, 5, 0), L = bitRange(
                    instr, 11, 11);

            if (L == 1) {
                setRegister(7, pc);
            }
            pc = registers[BaseR] + index6;
            lastInstruction = "JSRR " + L + "," + BaseR + ",0x"
                    + Integer.toHexString(index6);
            break;
        }
        case 13: // **** RET
        {
            // copy R7 to PC
            pc = registers[7];
            lastInstruction = "RET";
            break;
        }
        default: // **** DBUG
        {
            lastInstruction = "DBUG";
            Simulator.printState(this);
        }
        }
    }
}