package com.appspot.WebTobinQ.client;

import static junit.framework.Assert.*;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.junit.Test;
import static com.appspot.WebTobinQ.client.QParserTest.*;

public class QInterpreterTest {
	public static class ConsoleForTest implements Writable
	{
		public StringBuffer Result = new StringBuffer();
		public void write(CharSequence cs) {
			Result.append(cs);
		}
	}
	@Test
	public void test_hello()
	{
		ConsoleForTest console = new ConsoleForTest();
		QInterpreter intp = new QInterpreter(console);
		intp.eval("x<-c(1,2,3)\ny<-c(5,76)\n");
		System.out.println(console.Result.toString());
		assertNotNull(intp);
	}
	
	@Test
	public void test_eval_plus()
	{
		QInterpreter intp = createInterpreter();
		Object actual = intp.eval("2+3");
		assertEquals(5, (int)(Integer)actual);
	}
	
	@Test
	public void test_plus() throws RecognitionException
	{
		int expect = 5;
		Object actual = evalSimpleBinary("2+3");
		
		assertEquals(expect, (int)(Integer)actual);
	}

	private Object evalSimpleBinary(String code) throws RecognitionException {
		Tree t = parseExpression(code);
		
		QInterpreter intp = createInterpreter();
		Object actual = intp.evalBinary(t.getChild(0), t.getChild(1), t.getChild(2));
		return actual;
	}

	public String getConsoleOutput(QInterpreter intp) {
		String result;
		ConsoleForTest console = (ConsoleForTest)intp.getConsole();
		result = console.Result.toString();
		return result;
	}
		
	static QInterpreter createInterpreter()
	{
		ConsoleForTest console = new ConsoleForTest();
		return new QInterpreter(console);		
	}

}
