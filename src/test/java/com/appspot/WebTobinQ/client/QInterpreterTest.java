package com.appspot.WebTobinQ.client;

import static junit.framework.Assert.*;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
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
	public void test_eval_plus()
	{
		QInt expected = new QInt(5);
		QInterpreter intp = createInterpreter();
		Object actual = intp.eval("2+3");
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_evalTerm_int()
	{
		QInt expected = new QInt(3);

		QInterpreter intp = createInterpreter();
		CommonTree arg = createIntTree("3");
		
		Object actual = intp.evalTerm(arg);
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_evalTerm_XXBINARY()
	{
		QInt expected = new QInt(3);
		
		QInterpreter intp = createInterpreter();
		CommonTree parent = createTree(QParser.XXBINARY, "");
		// "+" is not STR_CONST, but do this for the times being.
		parent.addChild(createTree(QParser.STR_CONST, "+"));
		parent.addChild(createIntTree("1"));
		parent.addChild(createIntTree("2"));
		
		Object actual = intp.evalTerm(parent);
		
		assertEquals(expected, actual);
	}
	
	CommonTree createTree(int type, String val)
	{
		return new CommonTree(new CommonToken(type, val));
	}
	
	@Test
	public void test_evalTerm_symbol()
	{
		QInterpreter intp = createInterpreter();
		Object expected = intp._curEnv.get("c");
		
		Object actual = intp.evalTerm(createTree(QParser.SYMBOL, "c"));
		assertEquals(expected, actual);
	}

	private CommonTree createIntTree(String val) {
		return createTree(QParser.DecimalLiteral, val);
	}
	
	@Test
	public void test_eval_plus_2statements()
	{
		QInt expected = new QInt(9);
		QInterpreter intp = createInterpreter();
		Object actual = intp.eval("2+3\n4+5");
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_eval_plus_3terms()
	{
		QInt expected = new QInt(6);
		QInterpreter intp = createInterpreter();
		Object actual = intp.eval("1+2+3");
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_evalBinary_plus() throws RecognitionException
	{
		QInt expected = new QInt(5);
		Object actual = evalSimpleBinary("2+3");
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_evalPlus_normal()
	{
		QInt expected = new QInt(3);
		
		QInt arg1 = new QInt(1);
		QInt arg2 = new QInt(2);
		QInt actual = (QInt)_intp.evalPlus(arg1, arg2);
		assertEquals(expected, actual);
	}
		
	
	@Test
	public void test_evalSublist_multiple() throws RecognitionException
	{
		// (XXSUBLIST (XXSUB1 1) (XXSUB1 2) (XXSUB1 3))
		Tree subList = buildSubList("c(1, 2, 3)");
		QInterpreter intp = createInterpreter();
		QObject ret = intp.evalSubList(subList);

		assertVector123(ret);
	}

	@Test
	public void test_evalSublist_single() throws RecognitionException
	{
		QInt expected = new QInt(1);
		
		// (XXSUBLIST (XXSUB1 1))
		Tree subList = buildSubList("c(1)");
		QInterpreter intp = createInterpreter();
		QObject actual = intp.evalSubList(subList);

		assertEquals(expected, actual);
	}
	
	private void assertVector123(QObject ret) {
		assertEquals(3, ret.getLength());
		assertEquals(new QInt(1), ret.get(0));
		assertEquals(new QInt(2), ret.get(1));
		assertEquals(new QInt(3), ret.get(2));
	}
	
	@Test
	public void test_assignToFormalList() throws RecognitionException
	{
		Tree subList = buildSubList("c(1)");
		QInterpreter intp = createInterpreter();
		
		Environment target = new Environment(null);
		intp.assignToFormalList(subList, null, target);
		QObject args = target.get("...");
		
		assertNotNull(args);
		assertEquals("numeric", args.getMode());
		assertEquals(1, ((QInt)args).getValue());
	}

	// code is like "c(1, 2, 3)"
	private Tree buildSubList(String code) throws RecognitionException {
		// (XXFUNCALL c (XXSUBLIST (XXSUB1 1) (XXSUB1 2) (XXSUB1 3)))
		Tree expr = parseExpression(code);
		Tree subList = expr.getChild(1);
		return subList;
	}
	
	QInterpreter _intp = createInterpreter();

	@Test
	public void test_evalCallFunction() throws RecognitionException
	{
		// (XXFUNCALL c (XXSUBLIST (XXSUB1 1) (XXSUB1 2) (XXSUB1 3)))
		Tree expr = parseExpression("c(1, 2, 3)");
		QObject ret = _intp.evalCallFunction(expr);

		assertVector123(ret);		
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

	
	@Test
	public void test_Environment_root()
	{
		QInt expected = new QInt(1);
		Environment env = new Environment(null);
		env.put("a", expected);
		
		assertNull(env.get("dummy"));
		assertEquals(expected,env.get("a"));
	}
	
	@Test
	public void test_Environment_chain()
	{
		QInt expectedA = new QInt(1);
		QInt expectedB = new QInt(2);

		Environment envParent = new Environment(null);
		envParent.put("a", expectedA);
		Environment envChild = new Environment(envParent);
		envChild.put("b", expectedB);
		
		
		assertNull(envParent.get("b"));
		assertEquals(expectedA, envParent.get("a"));
		
		assertEquals(expectedB, envChild.get("b"));
		assertEquals(expectedA, envChild.get("a"));
	}
}
