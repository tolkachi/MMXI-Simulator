package cse560;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.util.Random;

//TODO: Add all test cases and fill in javadoc comments for each

public class InterpreterTest {
	/** The instance of an InterpreterImp object to test. */
	private final InterpreterImp i = new InterpreterImp();

	/** Random number generator */
	private final Random generator = new Random();

	//-------------------------------------------------------------------------
	// Test private operations
	//-------------------------------------------------------------------------
	
	/**
	 * Test checks private operation bitRange()  
	 * getting bits on the both bounds and in the middle of the value 
	 */
	@Test
	public void testBitRange()
	{
		//bitRange(0xABC4, 15, 12)
		int tvalue = 0xABC4;  //1010101111000100
		assertEquals("wrong value",10, i.bitRange(tvalue, 15, 12));
		//bitRange(0xABC4, 2, 0)
		assertEquals("wrong value",4, i.bitRange(tvalue, 2, 0));
		//bitRange(0xABC4, 6, 3)
		assertEquals("wrong value",8, i.bitRange(tvalue, 6, 3));
		
	}
	
	/**
	 * Test checks appropriate  execution of private operation twoBytes()
	 */
	@Test
	public void testTwoBytes()
	{
	int value = 325689; //1001111100000111001
		assertEquals("wrong value",63545, i.twoBytes(value));
	}
	
	/**
	 * Test checks appropriate  execution of private operation opcode()
	 */
	@Test
	public void testOpcode()
	{
		int value = 0x0AE00; //1010111000000000
		assertEquals("wrong value",10, i.opcode(value));
		
	}

	/**
	 * Test checks appropriate  execution of private operation offsetAddress()
	 */
	@Test
	public void testOffsetAddress()
	{
		int value = 0xAE17; //1010111000010111	
		int PC = 25000;    // 0110000110101000
		i.setPC(PC);
		assertEquals("wrong value",24599, i.offsetAddress(value));
	}

	/**
	 * CCR.N is true iff the value is negative
	 * 
	 * CCR.Z is true iff the value is equal to zero
	 * 
	 * CCR.P is true iff the value is positive
	 */
	@Test
	public void testSetCCR()
	{
		i.setRegister(0, -5);
		i.setCCR(0);
		assertEquals("only CCR.N is true", CCR.N, i.getCCR());

		i.setRegister(1, 0);
		i.setCCR(1);
		assertEquals("only CCR.Z is true", CCR.Z, i.getCCR());

		i.setRegister(2, 9);
		i.setCCR(2);
		assertEquals("only CCR.P is true", CCR.P, i.getCCR());
	}
	
	/**
	 * Test checks appropriate  execution of a public operation setRegister()
	 */
	@Test
	public void testSetRegister()
	{
		i.setRegister(1, 32000);
		assertEquals("wrong value",32000, i.getRegister(1));
		
	}
	//-------------------------------------------------------------------------
	// Test instructions
	//-------------------------------------------------------------------------

	/**
	 * Test decoding and execution of NOT with the following cases:
	 * <ul>
	 * <li>~ 0x0000 = 0x1111</li>
	 * <li>~ 0x1111 = 0x0000</li>
	 * <li>~ 0x8000 = 0x7FFF</li>
	 * <li>~ 0x7FFF = 0x8000</li>
	 * <li>~ 0xB2E3 = 0x4D1C</li>
	 * <li>~ 0x4D1C = 0xB2E3</li>
	 * </ul>
	 */
	@Test
	public void NOT_Instruction()
	{
		// NOT R0,R0
		i.setMemory(0, Integer.parseInt("1001000000000000", 2));

		i.step();
		assertEquals("NOT 0x0000 = 0x1111", i.twoBytes(-1), i.getRegister(0));

		i.setRegister(0, -1);
		i.setPC(0);
		i.step();
		assertEquals("NOT 0x1111 = 0x0000", 0, i.getRegister(0));

		i.setRegister(0, 0x8000);
		i.setPC(0);
		i.step();
		assertEquals("NOT 0x8000 = 0x7FFF", 0x7FFF, i.getRegister(0));

		i.setRegister(0, 0x7FFF);
		i.setPC(0);
		i.step();
		assertEquals("NOT 0x7FFF = 0x8000", 0x8000, i.getRegister(0));

		i.setRegister(0, 0xB2E3);
		i.setPC(0);
		i.step();
		assertEquals("NOT 0xB2E3 = 0x4D1C", 0x4D1C, i.getRegister(0));

		i.setRegister(0, 0x4D1C);
		i.setPC(0);
		i.step();
		assertEquals("NOT 0x4D1C = 0xB2E3", 0xB2E3, i.getRegister(0));
	}

	/**
	 * Test decoding and execution of ADD with the following cases:
	 * <ol>
	 * <li>Second operand is a register</li>
	 * 	<ul>
	 * 		<li>add 0 to 0</li>
	 * 		<li>add 32767 to 32767</li>
	 * 		<li>add -32768 to -32768</li>
	 * 		<li>add positive to 0</li>
	 * 		<li>add negative to 0</li>
	 * 		<li>add positive to 32767</li>
	 * 		<li>add negative to 32767</li>
	 * 		<li>add positive to -32768</li>
	 * 		<li>add negative to -32768</li>
	 * 		<li>add two positive (no overflow)</li>
	 * 		<li>add two positive (overflow)</li>
	 * 		<li>add two negative (no overflow)</li>
	 * 		<li>add two negative (overflow)</li>
	 * 		<li>add positive and negative</li>
	 * 	</ul>
	 * <li>Second operand is immediate</li>
	 * 	<ul>
	 *	  	<li>add 0 to 0</li>
	 * 		<li>add positive to 0</li>
	 * 		<li>add negative to 0</li>
	 * 		<li>add positive to 32767</li>
	 * 		<li>add negative to 32767</li>
	 * 		<li>add positive to -32768</li>
	 * 		<li>add negative to -32768</li>
	 * 		<li>add two positive (no overflow)</li>
	 * 		<li>add two negative (no overflow)</li>
	 * 		<li>add positive and negative</li>
	 * 	</ul>
	 * </ol>
	 */
	@Test
	public void ADD_Instruction()
	{
		int rand, rand2;
		String imm5;

		// ADD R0,R1,R2
		i.setMemory(0, Integer.parseInt("0001000001000010", 2));
		i.step();
		assertEquals("0 + 0 = 0", 0, i.getRegister(0));

		i.setRegister(1, 32767);
		i.setRegister(2, 32767);
		i.setPC(0);
		System.out.print("Overflow warning expected here-->");
		i.step();
		assertEquals("32767 + 32767 = -2", i.twoBytes(-2), i.getRegister(0));

		i.setRegister(1, -32768);
		i.setRegister(2, -32768);
		i.setPC(0);
		System.out.print("Overflow warning expected here-->");
		i.step();
		assertEquals("-32768 + -32768 = 0", 0, i.getRegister(0));

		// add positive to 0
		i.setRegister(2, 0);
		rand = generator.nextInt(Memory.MAX_VALUE / 2) + 1;
		i.setRegister(1, rand);
		i.setPC(0);
		i.step();
		assertEquals("x(>0) + 0 = x", rand, i.getRegister(0));

		// add negative to 0
		rand = generator.nextInt(Memory.MAX_VALUE / 2 + 2) * -1;
		i.setRegister(1, rand);
		i.setPC(0);
		i.step();
		assertEquals("x(<0) + 0 = x", i.twoBytes(rand), i.getRegister(0));

		// add positive to 32767
		i.setRegister(2, 32767);
		rand = generator.nextInt(Memory.MAX_VALUE / 2) + 1;
		i.setRegister(1, rand);
		i.setPC(0);
		System.out.print("Overflow warning expected here-->");
		i.step();
		assertEquals("x(>0) + 32767 = negative", i.getRegister(0), 
				rand + 32767);

		// add negative to 32767
		rand = generator.nextInt(Memory.MAX_VALUE / 2 + 2) * -1;
		i.setRegister(1, rand);
		i.setPC(0);
		i.step();
		assertEquals("x(<0) + 32767 = x + 32767", i.getRegister(0), 
				rand + 32767);

		// add positive to -32768
		i.setRegister(2, -32768);
		rand = generator.nextInt(Memory.MAX_VALUE / 2) + 1;
		i.setRegister(1, rand);
		i.setPC(0);
		i.step();
		assertEquals("x(>0) + -32768 = x + -32768", i.getRegister(0), 
				i.twoBytes(rand - 32768));

		// add negative to -32768
		rand = generator.nextInt(Memory.MAX_VALUE / 2 + 2) * -1;
		i.setRegister(1, rand);
		i.setPC(0);
		System.out.print("Overflow warning expected here-->");
		i.step();
		assertEquals("x(<0) + -32768 = positive", i.getRegister(0), 
				i.twoBytes(rand - 32768));

		// add two positive (no overflow)
		rand = generator.nextInt((Memory.MAX_VALUE - 5) / 4) + 1;
		rand2 = generator.nextInt((Memory.MAX_VALUE - 5) / 4) + 1;
		i.setRegister(1, rand);
		i.setRegister(2, rand2);
		i.setPC(0);
		i.step();
		assertEquals("x(>0) + y(>0) = x + y", rand + rand2, i.getRegister(0));

		// add two positive (overflow)
		rand = generator.nextInt((Memory.MAX_VALUE - 5) / 4)
				+ (Memory.MAX_VALUE - 5) / 4 + 2;
		rand2 = generator.nextInt((Memory.MAX_VALUE - 5) / 4)
				+ (Memory.MAX_VALUE - 5) / 4 + 2;
		i.setRegister(1, rand);
		i.setRegister(2, rand2);
		i.setPC(0);
		System.out.print("Overflow warning expected here-->");
		i.step();
		assertEquals("x(>0) + y(>0) = overflow", i.getRegister(0), 
				rand + rand2);

		// add two negative (no overflow)
		rand = (generator.nextInt((Memory.MAX_VALUE - 5) / 4) + 1) * -1;
		rand2 = (generator.nextInt((Memory.MAX_VALUE - 5) / 4) + 1) * -1;
		i.setRegister(1, rand);
		i.setRegister(2, rand2);
		i.setPC(0);
		i.step();
		assertEquals("x(<0) + y(<0) = x + y", i.getRegister(0),
				i.twoBytes(rand + rand2));

		// add two negative (overflow)
		rand = (generator.nextInt((Memory.MAX_VALUE - 5) / 4)
				+ (Memory.MAX_VALUE - 5) / 4 + 3) * -1;
		rand2 = (generator.nextInt((Memory.MAX_VALUE - 5) / 4)
				+ (Memory.MAX_VALUE - 5) / 4 + 3) * -1;
		i.setRegister(1, rand);
		i.setRegister(2, rand2);
		i.setPC(0);
		System.out.print("Overflow warning expected here-->");
		i.step();
		assertEquals("x(<0) + y(<0) = overflow", i.getRegister(0),
				i.twoBytes(rand + rand2));

		// Now test immediate addressing

		// ADD R0,R1,0
		i.setMemory(0, Integer.parseInt("0001000001100000", 2));
		i.setRegister(1, 0);
		i.setPC(0);
		i.step();
		assertEquals("0 + 0 = 0", 0, i.getRegister(0));

		// add positive to 0
		rand = generator.nextInt(15) + 1;
		imm5 = Integer.toBinaryString(rand);
		while (imm5.length() < 5)
		{
			imm5 = "0".concat(imm5);
		}
		// ADD R0,R1,imm5
		i.setMemory(0, Integer.parseInt("00010000011" + imm5, 2));
		i.setPC(0);
		i.step();
		assertEquals("x(>0) + 0 = x", rand, i.getRegister(0));

		// add negative to 0
		rand = generator.nextInt(15) + 16;
		imm5 = Integer.toBinaryString(rand);
		while (imm5.length() < 5)
		{
			imm5 = "1".concat(imm5);
		}
		i.setMemory(0, Integer.parseInt("00010000011" + imm5, 2));
		i.setPC(0);
		i.step();
		assertEquals("x(<0) + 0 = x", i.twoBytes((rand << 27) >> 27),
				i.getRegister(0));

		// add positive to 32767
		i.setRegister(1, 32767);
		rand = generator.nextInt(15) + 1;
		imm5 = Integer.toBinaryString(rand);
		while (imm5.length() < 5)
		{
			imm5 = "0".concat(imm5);
		}
		i.setMemory(0, Integer.parseInt("00010000011" + imm5, 2));
		i.setPC(0);
		System.out.print("Overflow warning expected here-->");
		i.step();
		assertEquals("x(>0) + 32767 = negative", rand + 32767, 
				i.getRegister(0));

		// add negative to 32767
		rand = generator.nextInt(15) + 16;
		imm5 = Integer.toBinaryString(rand);
		while (imm5.length() < 5)
		{
			imm5 = "1".concat(imm5);
		}
		i.setMemory(0, Integer.parseInt("00010000011" + imm5, 2));
		i.setPC(0);
		i.step();
		assertEquals("x(<0) + 32767 = x + 32767", ((rand << 27) >> 27) + 32767,
				i.getRegister(0));

		// add positive to -32768
		i.setRegister(1, -32768);
		rand = generator.nextInt(15) + 1;
		imm5 = Integer.toBinaryString(rand);
		while (imm5.length() < 5)
		{
			imm5 = "0".concat(imm5);
		}
		i.setMemory(0, Integer.parseInt("00010000011" + imm5, 2));
		i.setPC(0);
		i.step();
		assertEquals("x(>0) + -32768 = x + -32768", i.twoBytes(rand - 32768),
				i.getRegister(0));

		// add negative to -32768
		rand = generator.nextInt(15) + 16;
		imm5 = Integer.toBinaryString(rand);
		while (imm5.length() < 5)
		{
			imm5 = "1".concat(imm5);
		}
		i.setMemory(0, Integer.parseInt("00010000011" + imm5, 2));
		i.setPC(0);
		System.out.print("Overflow warning expected here-->");
		i.step();
		assertEquals("x(<0) + -32768 = positive", 
				i.twoBytes(((rand << 27) >> 27) - 32768), i.getRegister(0));

		// add two positive (no overflow)
		rand2 = generator.nextInt(Memory.MAX_VALUE / 2 - 16) + 1;
		i.setRegister(1, rand2);
		rand = generator.nextInt(15) + 1;
		imm5 = Integer.toBinaryString(rand);
		while (imm5.length() < 5)
		{
			imm5 = "0".concat(imm5);
		}
		i.setMemory(0, Integer.parseInt("00010000011" + imm5, 2));
		i.setPC(0);
		i.step();
		assertEquals("x(>0) + y(>0) = x + y", rand + rand2, i.getRegister(0));

		// add two negative (no overflow)
		rand2 = (generator.nextInt(Memory.MAX_VALUE / 2 - 16) + 1) * -1;
		i.setRegister(1, rand2);
		rand = generator.nextInt(15) + 16;
		imm5 = Integer.toBinaryString(rand);
		while (imm5.length() < 5)
		{
			imm5 = "1".concat(imm5);
		}
		i.setMemory(0, Integer.parseInt("00010000011" + imm5, 2));
		i.setPC(0);
		i.step();
		assertEquals("x(<0) + y(<0) = x + y", 
				i.twoBytes(((rand << 27) >> 27) + rand2), i.getRegister(0));
	}

	/**
	 * Test decoding and execution of AND with the following cases:
	 * (both addressing modes for some of them)
	 * <ul>
	 * <li>0 AND 0 = 0</li>
	 * <li>0xFFFF AND 0 = 0</li>
	 * <li>0xFFFF AND 0xFFFF = 0xFFFF</li>
	 * <li>0 AND 0xFFFF = 0</li>
	 * <li>x(random) AND 0xFFFF = x</li>
	 * <li>x(random) AND 0 = 0</li>
	 * </ul>
	 */
	@Test
	public void AND_Instruction()
	{
		int rand;

		// AND R0,R1,R2
		i.setMemory(0, Integer.parseInt("0101000001000010", 2));

		i.step();
		assertEquals("0 AND 0 = 0", 0, i.getRegister(0));

		i.setRegister(1, 0xFFFF);
		i.setPC(0);
		i.step();
		assertEquals("0xFFFF AND 0 = 0", 0, i.getRegister(0));

		i.setRegister(2, 0xFFFF);
		i.setPC(0);
		i.step();
		assertEquals("0xFFFF AND 0xFFFF = 0xFFFF", 0xFFFF, i.getRegister(0));

		i.setRegister(1, 0);
		i.setPC(0);
		i.step();
		assertEquals("0 AND 0xFFFF = 0", 0, i.getRegister(0));

		rand = generator.nextInt(Memory.MAX_VALUE);
		i.setRegister(1, rand);
		i.setPC(0);
		i.step();
		assertEquals("x(random) AND 0xFFFF = x", rand, i.getRegister(0));

		rand = generator.nextInt(Memory.MAX_VALUE);
		i.setRegister(1, rand);
		i.setRegister(2, 0);
		i.setPC(0);
		i.step();
		assertEquals("x(random) AND 0 = 0", 0, i.getRegister(0));

		// repeat some of these tests with immediate addressing
		
		// AND R0,R1,0
		i.setMemory(0, Integer.parseInt("0101000001100000", 2));
		i.setRegister(1, 0);
		i.step();
		assertEquals("0 AND 0 = 0", 0, i.getRegister(0));

		i.setRegister(1, 0xFFFF);
		i.setPC(0);
		i.step();
		assertEquals("0xFFFF AND 0 = 0", 0, i.getRegister(0));

		rand = generator.nextInt(Memory.MAX_VALUE);
		i.setRegister(1, rand);
		i.setPC(0);
		i.step();
		assertEquals("x(random) AND 0 = 0", 0, i.getRegister(0));
	}

	/**
	 * Test checks appropriate decoding and execution of LEA instruction
	 */
	@Test
	public void LEA_Instruction()
	{
		//LEA R7,31
		i.setMemory(0, Integer.parseInt("1110111000011111", 2));
		i.setRegister(7, 0);
		i.step();
		assertEquals("R[7] = 31", i.getRegister(7), 31);
	}
	
	/**
	 * Test checks appropriate decoding and execution of LD instruction
	 */
	@Test
	public void LD_Instruction()
	{
        //LD R7,31
		i.setMemory(0, Integer.parseInt("0010111000011111", 2));
		i.setPC(0);
		i.setMemory(31, 10);
		i.setRegister(7, 0);
		i.step();
		assertEquals("R[7] = 31", 10, i.getRegister(7));
	}
	
	/**
	 * Test checks appropriate decoding and execution of LDI instruction
	 */
	@Test
	public void LDI_Instruction()  
	{ 
		//LDI R7,31
		i.setMemory(0, Integer.parseInt("1010111000011111", 2));
		i.setPC(0);
		i.setRegister(7, 0);
		i.setMemory(31, 32);
		i.setMemory(32, 10);
		i.step();
		assertEquals("R[7] = 10", 10, i.getRegister(7));
	}
	
	/**
	 * Test checks appropriate decoding and execution of LDR instruction
	 */
	@Test
	public void LDR_Instruction()
	{
		//LD R7,R0,31
		i.setMemory(0, Integer.parseInt("0110111000011111", 2));
		i.setPC(0);
		i.setRegister(0, 1);
		i.setMemory(31, 10);
		i.setMemory(32, 11);
		i.setRegister(7, 0);
		i.step();
		assertEquals("R[7] = 11", 11, i.getRegister(7));
	}

	/**
	 * Test checks appropriate decoding and execution of ST instruction
	 */
	@Test
	public void ST_Instruction()
	{
		//ST R7,R5,31
		i.setMemory(0, Integer.parseInt("0011111000011111", 2));
		i.setRegister(7, 1000);
		i.setMemory(31, 0);
		i.setPC(0);
		i.step();
		assertEquals("M[31775] = 1000", i.getMemory(31), 1000);
	}
	
	/**
	 * Test checks appropriate decoding and execution of STI instruction
	 */
	@Test
	public void STI_Instruction()
	{
		//STI R7,R5,31
		i.setMemory(32000, Integer.parseInt("1011111000011111", 2));
		i.setRegister(7, 1000);
		i.setMemory(2000,0);
		i.setMemory(31775, 2000);
		i.setMemory(2000,0);
		i.setPC(32000);
		i.step();
		assertEquals("M[2000] = 1000", 1000, i.getMemory(2000));
	}

	/**
	 * Test decoding and execution of STR by storing a positive and then a 
	 * negative value.
	 */
	@Test
	public void STR_Instruction()
	{
		//STR R2,R5,15
		i.setMemory(6, Integer.parseInt("0111010101001111", 2));
		i.setMemory(25, 0);
		i.setRegister(5, 10);
		i.setRegister(2,8);
		i.setPC(6);
		i.step();
		assertEquals("M[25] = 8", 8, i.getMemory(25));

		//STR R2,R5,15
		i.setMemory(6, Integer.parseInt("0111010101001111", 2));
		i.setMemory(25, 0);
		i.setRegister(5, 10);
		i.setRegister(2,-32000);
		i.setPC(6);
		i.step();
		assertEquals("M[25] = -32000", i.twoBytes(-32000), i.getMemory(25));	
	}
	
	/**
	 * Test decoding and execution of BRx:
	 * <ul>
	 *  <li>check that NOP doesn't change the PC</li>
	 *  <li>check that an unconditional branch changes the PC</li>
	 *  <li>check that the remaining cases only branch if the specified N Z P bits in the instruction are set in the CCR
	 * </ul>
	 */
	@Test
	public void BRx_Instruction()
	{
		// NOP
		i.setMemory(7, Integer.parseInt("0000000000000000", 2));
		i.setPC(7);
		i.step();
		assertEquals("PC = #PC + 1", 8, i.getPC());
		
		//BRx 111,82
		i.setMemory(992, Integer.parseInt("0000111001010010", 2));
		i.setPC(992);
		i.step();
		assertEquals("PC is changed", (i.bitRange(992, 15, 9) << 9) + 82, i.getPC());
		
		//first test all possibilities for negative numbers
		i.setRegister(0, -1);
		
		//BRx 100,82
		i.setMemory(15, Integer.parseInt("0000100001010010", 2));
		i.setCCR(0);
		i.setPC(15);
		i.step();
		assertEquals("PC = 82", 82, i.getPC());
		
		//BRX 010,82
		i.setMemory(19, Integer.parseInt("0000010001010010", 2));
		i.setCCR(0);
		i.setPC(19);
		i.step();
		assertEquals("branch not taken", 20, i.getPC());
		
		//BRx 001,82
		i.setMemory(99, Integer.parseInt("0000001001010010", 2));
		i.setCCR(0);
		i.setPC(99);
		i.step();
		assertEquals("branch not taken", 100, i.getPC());
		
		//BRx 011,82
		i.setMemory(199, Integer.parseInt("0000011001010010", 2));
		i.setCCR(0);
		i.setPC(199);
		i.step();
		assertEquals("branch not taken", 200, i.getPC());
		
		//BRx 110,82
		i.setMemory(299, Integer.parseInt("0000110001010010", 2));
		i.setCCR(0);
		i.setPC(299);
		i.step();
		assertEquals("branch taken", 82, i.getPC());

		//BRx 101,82
		i.setMemory(399, Integer.parseInt("0000101001010010", 2));
		i.setCCR(0);
		i.setPC(399);
		i.step();
		assertEquals("branch taken", 82, i.getPC());
	}
	
	/**
	 * Test decoding and execution of TRAP:
	 * <ul>
	 *  <li>Check that TRAP x21 outputs an ASCII character correctly</li>
	 *  <li>Check that TRAP x22 outputs a null-terminated string correctly</li>
	 *  <li>Check that TRAP x23 handles user input of a character correctly</li>
	 *  <li>Check that TRAP x31 outputs a decimal integer correctly</li>
	 *  <li>Check that TRAP x43 stores a random number in R0</li>
	 * </ul>
	 */
	@Test
	public void TRAP_Instruction()
	{
		// TRAP x21
		i.setMemory(447, Integer.parseInt("1111000000100001", 2));
		i.setPC(447);
		i.setRegister(0, 38);
		System.out.print("\n'" + (char)38 + "' expected here-->");
		i.step();
		
		// TRAP x22
		i.setMemory(839, Integer.parseInt("1111000000100010", 2));
		i.setPC(839);
		//set R0 to first character of string
		i.setRegister(0, 556); //address where string starts
		i.setMemory(556, (int)'I');
		i.setMemory(557, (int)' ');
		i.setMemory(558, (int)'a');
		i.setMemory(559, (int)'m');
		i.setMemory(560, (int)' ');
		i.setMemory(561, (int)'a');
		i.setMemory(562, (int)' ');
		i.setMemory(563, (int)'s');
		i.setMemory(564, (int)'t');
		i.setMemory(565, (int)'r');
		i.setMemory(566, (int)'i');
		i.setMemory(567, (int)'n');
		i.setMemory(568, (int)'g');
		i.setMemory(569, (int)'!');
		i.setMemory(570, (int)'\0');
		System.out.print("\n'I am a string!' expected here-->");
		i.step();

		// TRAP x31
		i.setMemory(847, Integer.parseInt("1111000000110001", 2));
		i.setPC(847);
		i.setRegister(0, -7232);
		System.out.print("\n'-7232' expected here-->");
		i.step();
		
		// TRAP x43
		i.setMemory(343, Integer.parseInt("1111000001000011", 2));
		i.setPC(343);
		i.step();
		System.out.print("\nRandom: ");
		System.out.println(i.getRegister(0));
	}

	/**
	 * Test decoding and execution of JSR:
	 * <ul>
	 * <li>check that PC is set to the correct address</li>
	 * <li>check that R7 is set to PC only if the link bit is set</li>
	 * </ul>
	 */
	@Test
	public void JSR_Instruction() 
	{
		// JSR 1,511
		// NEXT PAGE CASE
		i.setMemory(65535, Integer.parseInt("0100100111111111", 2));
		i.setPC(65535);
		System.out.println("\nTwo warnings expected below:");
		i.step();
		assertEquals("R7 = #PC", 0, i.getRegister(7));
		assertEquals("PC = #PC[15:9] + OFFSET", 511, i.getPC());

		// JSRR 0,510
		i.setMemory(65534, Integer.parseInt("0100001111111110", 2));
		i.setRegister(7, 3);
		i.setPC(65534);
		i.step();
		assertEquals("R7 not set", 3, i.getRegister(7));
		assertEquals("PC = PC[15:9] + OFFSET", 65534, i.getPC());

		// JSR 1,510    
		i.setMemory(0, Integer.parseInt("0100100111111110", 2));
		i.setPC(0);
		i.setRegister(7, 0);
		i.step();
		assertEquals("R7 = PC", 1, i.getRegister(7));
		assertEquals("PC = PC[15:9] + OFFSET", 510, i.getPC());

		// JSR 0,510    
		i.setMemory(0, Integer.parseInt("0100000111111110", 2));
		i.setPC(0);
		i.setRegister(7, 99);
		i.step();
		assertEquals("R7 not set", 99, i.getRegister(7));
		assertEquals("PC = PC[15:9] + OFFSET", 510, i.getPC());

		// JSR 1,409    
		i.setMemory(9632, Integer.parseInt("0100100110011001", 2));
		i.setPC(9632);
		i.setRegister(7, 0);
		i.step();
		assertEquals("R7 = PC", 9633, i.getRegister(7));
		assertEquals("PC = PC[15:9] + OFFSET", 9625, i.getPC());

		// JSR 0,409  
		i.setMemory(9632, Integer.parseInt("0100000110011001", 2));
		i.setPC(9632);
		i.setRegister(7, 12340);
		i.step();
		assertEquals("R7 not set", 12340, i.getRegister(7));
		assertEquals("PC = PC[15:9] + OFFSET", 9625, i.getPC());
	}

	/**
	 * Test decoding and execution of JSRR:
	 * <ul>
	 * <li>check that PC is set to the correct address</li>
	 * <li>check that R7 is set to PC only if the link bit is set</li>
	 * </ul>
	 */
	@Test
	public void JSRR_Instruction() 
	{
		// JSRR 1,R1,63
		i.setMemory(3, Integer.parseInt("1100100001111111", 2));
		i.setRegister(1, 5);
		i.setPC(3);
		i.step();
		assertEquals("R7 = PC", 4, i.getRegister(7));
		assertEquals("PC = BaseR + index6", 68, i.getPC());

		// JSRR 0,R1,63
		i.setMemory(3, Integer.parseInt("1100000001111111", 2));
		i.setRegister(1, 5);
		i.setRegister(7, 35);
		i.setPC(3);
		i.step();
		assertEquals("R7 not set", 35, i.getRegister(7));
		assertEquals("PC = BaseR + index6", 68, i.getPC());

		//case when maximum address value is exceeded with L=1
		// JSRR 1,R2,63
		i.setMemory(3, Integer.parseInt("1100100010111111", 2));
		i.setRegister(2, 32767);
		i.setPC(3);
		i.step();
		assertEquals("R7 = PC", 4, i.getRegister(7));
		assertEquals("PC = BaseR + index6", 32830, i.getPC());

		//case when maximum address value is exceeded with L=0
		// JSRR 0,R1,63
		i.setMemory(3, Integer.parseInt("1100000010111111", 2));
		i.setRegister(2, 32767);
		i.setRegister(7, 8143);
		i.setPC(3);
		i.step();
		assertEquals("R7 not set", 8143, i.getRegister(7));
		assertEquals("PC = BaseR + index6", 32830, i.getPC());
	}

	/**
	 * Test decoding and execution of RET: check that PC is set to a predefined value of R7
	 */
	@Test
	public void RET_Instruction()
	{
		// RET
		i.setMemory(0, Integer.parseInt("1101000000000000", 2));
		i.setRegister(7, 5);
		i.step();
		assertEquals("pc = 5", i.getPC(), 5);
	}
	
	/**
	 * Test decoding and execution of DBUG: visually inspect output
	 */
	@Test
	public void DBUG_Instruction()
	{
		// DBUG
		i.setMemory(0, Integer.parseInt("1000000000000000", 2));
		System.out.println("\nMachine state expected below:");
		i.step();
	}
}
