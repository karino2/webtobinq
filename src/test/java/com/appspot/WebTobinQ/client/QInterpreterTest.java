package com.appspot.WebTobinQ.client;

import static junit.framework.Assert.*;

import org.junit.Test;


public class QInterpreterTest {
	public class ConsoleForTest implements Writable
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

}
