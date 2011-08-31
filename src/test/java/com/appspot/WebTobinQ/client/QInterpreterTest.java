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
		QObject expected = QObject.createInt(5);
		QInterpreter intp = createInterpreter();
		Object actual = intp.eval("2+3");
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_evalTerm_int()
	{
		QObject expected = QObject.createInt(3);

		QInterpreter intp = createInterpreter();
		CommonTree arg = createIntTree("3");
		
		Object actual = intp.evalTerm(arg);
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_evalTerm_XXBINARY()
	{
		QObject expected = QObject.createInt(3);
		
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
		QObject expected = QObject.createInt(9);
		QInterpreter intp = createInterpreter();
		Object actual = intp.eval("2+3\n4+5");
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_eval_plus_3terms()
	{
		QObject expected = QObject.createInt(6);
		QInterpreter intp = createInterpreter();
		Object actual = intp.eval("1+2+3");
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_evalBinary_assign() throws RecognitionException
	{
		Tree t = parseExpression("x<-3");

		assertNull(_intp._curEnv.get("x"));

		Object ret = _intp.evalBinary(t.getChild(0), t.getChild(1), t.getChild(2));
		assertNull(ret);
		
		assertEquals(createInt(3), _intp._curEnv.get("x"));
	}
	
	// TODO: comment in after parser is updated(why not?).
    //@Test
	public void test_evalBinary_eqAssign() throws RecognitionException
	{
		Tree t = parseExpressionOrAssign("x=3");

		System.out.println(t.toStringTree());
		//the same as test_evalBinary_assign()...
		assertNull(_intp._curEnv.get("x"));

		Object ret = _intp.evalBinary(t.getChild(0), t.getChild(1), t.getChild(2));
		assertNull(ret);
		
		assertEquals(createInt(3), _intp._curEnv.get("x"));
	}
	
	@Test
	public void test_evalBinary_plus() throws RecognitionException
	{
		QObject expected = QObject.createInt(5);
		Object actual = evalSimpleBinary("2+3");
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_evalBinary_seq() throws RecognitionException
	{
		QObject actual = evalSimpleBinary("3:5");

		assertEquals(3, actual.getLength());
		assertEquals(createInt(3), actual.get(0));
		assertEquals(createInt(4), actual.get(1));
		assertEquals(createInt(5), actual.get(2));
		
	}
	
	@Test
	public void test_evalPlus_normal()
	{
		QObject expected = QObject.createInt(3);
		
		QObject arg1 = QObject.createInt(1);
		QObject arg2 = QObject.createInt(2);
		QObject actual = _intp.evalPlus(arg1, arg2);
		assertEquals(expected, actual);
	}
	
	public static QObject createInt(int i){ return QObject.createInt(i); }
	
	@Test
	public void test_evalPlus_vectorPlusScalar()
	{
		QObject vector = QObject.createInt(1);
		QObject r2 = QObject.createInt(2);
		QObject r3 = QObject.createInt(3);
		vector.set(1, r2);
		// (1, 2) + 3 = (4, 5)
		QObject actual = _intp.evalPlus(vector, r3);
		
		assertEquals(2, actual.getLength());
		assertEquals(createInt(4), actual.get(0));
		assertEquals(createInt(5), actual.get(1));
	}
		
	@Test
	public void test_evalPlus_scalarPlusVector()
	{
		QObject vector = QObject.createInt(1);
		QObject r2 = QObject.createInt(2);
		QObject r3 = QObject.createInt(3);
		vector.set(1, r2);
		// 3+(1, 2) = (4, 5)
		QObject actual = _intp.evalPlus(r3, vector);
		
		assertEquals(2, actual.getLength());
		assertEquals(createInt(4), actual.get(0));
		assertEquals(createInt(5), actual.get(1));
	}
	
	@Test
	public void test_evalSublist_multiple() throws RecognitionException
	{
		// (XXSUBLIST (XXSUB1 1) (XXSUB1 2) (XXSUB1 3))
		Tree subList = buildSubList("c(1, 2, 3)");
		QObject ret = _intp.evalSubList(subList);

		assertVector123(ret);
	}

	@Test
	public void test_evalSublist_single() throws RecognitionException
	{
		// (XXSUBLIST (XXSUB1 1))
		Tree subList = buildSubList("c(1)");
		QInterpreter intp = createInterpreter();
		QObject actual = intp.evalSubList(subList);

		assertEquals("list", actual.getMode());
		assertEquals(1, actual.getLength());
		assertEquals(QObject.createInt(1), actual.get(0));
	}
	
	private void assertVector123(QObject ret) {
		assertEquals(3, ret.getLength());
		assertEquals(QObject.createInt(1), ret.get(0));
		assertEquals(QObject.createInt(2), ret.get(1));
		assertEquals(QObject.createInt(3), ret.get(2));
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
		assertEquals("list", args.getMode());
		assertEquals(1, args.get(0).getValue());
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

	private QObject evalSimpleBinary(String code) throws RecognitionException {
		Tree t = parseExpression(code);
		
		return _intp.evalBinary(t.getChild(0), t.getChild(1), t.getChild(2));
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
		QObject expected = QObject.createInt(1);
		Environment env = new Environment(null);
		env.put("a", expected);
		
		assertNull(env.get("dummy"));
		assertEquals(expected,env.get("a"));
	}
	
	@Test
	public void test_Environment_chain()
	{
		QObject expectedA = QObject.createInt(1);
		QObject expectedB = QObject.createInt(2);

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
