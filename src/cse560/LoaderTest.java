package cse560;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LoaderTest {

	private Interpreter machine = new InterpreterImp();
	private Memory mem = new MemoryImp();
	
	/**
	 * Pass a Header record to check if parseString will properly parse a Header record.  This test will run fine if parseString accepts Header records.
	 */
	@Test
	public void passHeader() {
		Loader loader = new LoaderImp(machine);
		loader.parseString("HSAMPLE00013456");
	}
	
	/**
	 * Pass an End record to check if parseString works with End records.  passEnd worked properly if no errors arise.
	 */
	@Test
	public void passEnd() {
		Loader loader = new LoaderImp(machine);
		loader.parseString("HSAMPLE00001111");
		loader.parseString("E0011");
	}
	
	/**
	 * Pass a Text record to check if parseString works with Text records.  If parseString works properly, the value 4567 will be stored
	 * in the memory location 0001.  
	 */
	@Test
	public void passText() {
		Loader loader = new LoaderImp(machine);
		loader.parseString("HSAMPLE00003333");
		loader.parseString("T00014567");
		int val = mem.get(0001);
		assertEquals("Memory value set by text record = 4567", val, 4567);
	}
	
	/**
	 * Pass a Header record to parseString to make sure that it reads and stores the name properly.  If it runs properly, the name SAMPLE should
	 * be read and stored.
	 */
	@Test
	public void checkHeaderName() {
		Loader loader = new LoaderImp(machine);
		loader.parseString("HSAMPLE00003333");
		String name = loader.getSegmentName();
		assertEquals("Header Name = SAMPLE", name, "SAMPLE");
	}
	
	/**
	 * Check to make sure the pc gets set properly from the End record.  If the End record is read properly, the PC should be set to the value of 0005.
	 */
	@Test
	public void checkPC() {
		Loader loader = new LoaderImp(machine);
		loader.parseString("HSAMPLE00002222");
		loader.parseString("E0005");
		int pc = machine.getPC();
		assertEquals("PC = Beginning address set by End record.", pc, 0005);
	}
}
