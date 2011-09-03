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

	public static void assertNumericEquals(QObject expected, QObject actual)
	{
		assertEquals(expected.getMode(), actual.getMode());
		assertEquals(expected.getValue(), actual.getValue());
	}
	
	@Test
	public void test_eval_plus()
	{
		QObject expected = QObject.createNumeric(5);

		QObject actual = _intp.eval("2+3");
		assertNumericEquals(expected, actual);
	}
	
	@Test
	public void test_eval_paren()
	{
		QObject expected = QObject.createNumeric(3);
		
		QObject actual = _intp.eval("(4+8)/4");
		assertNumericEquals(expected, actual);		
	}
	
	@Test
	public void test_evalLE_oneLe_true()
	{
		QObject expected = createLogical(true);

		QObject arg1 = createNumeric(1);
		QObject arg2 = createNumeric(2);
		QObject actual = _intp.evalLE(arg1, arg2);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void test_evalLE_oneLe_false()
	{
		QObject expected = createLogical(false);

		QObject arg1 = createNumeric(2);
		QObject arg2 = createNumeric(1);
		QObject actual = _intp.evalLE(arg1, arg2);
		
		assertEquals(expected, actual);		
	}
	
	@Test
	public void test_evalLE_vectorLe()
	{
		
		QObject arg1 = createNumeric(1);
		QObject q3 = createNumeric(3);
		arg1.set(1, q3);
		
		QObject arg2 = createNumeric(2);
		
		QObject actual = _intp.evalLE(arg1, arg2);

		assertEquals(2, actual.getLength());
		assertEquals(createLogical(true), actual.get(0));
		assertEquals(createLogical(false), actual.get(1));
	}
	
	private QObject createLogical(boolean b) {
		return QObject.createLogical(b);
	}

	@Test
	public void test_evalExpr_int()
	{
		QObject expected = QObject.createNumeric(3);

		QInterpreter intp = createInterpreter();
		CommonTree arg = createIntTree("3");
		
		QObject actual = intp.evalExpr(arg);
		assertNumericEquals(expected, actual);
	}
	
	@Test
	public void test_evalExpr_nullConst()
	{
		QObject expected = QObject.Null;

		CommonTree arg = createTree(QParser.NULL_CONST, null);		
		QObject actual = _intp.evalExpr(arg);
		
		assertNumericEquals(expected, actual);
	}
	
	
	@Test
	public void test_evalExpr_XXBINARY()
	{
		QObject expected = QObject.createNumeric(3);
		
		QInterpreter intp = createInterpreter();
		CommonTree parent = createTree(QParser.XXBINARY, "");
		// "+" is not STR_CONST, but do this for the times being.
		parent.addChild(createTree(QParser.STR_CONST, "+"));
		parent.addChild(createIntTree("1"));
		parent.addChild(createIntTree("2"));
		
		QObject actual = intp.evalExpr(parent);
		
		assertNumericEquals(expected, actual);
	}
	
	@Test
	public void test_getInt_numeric()
	{
		int expected = 3;		
		int actual = _intp.getInt(createNumeric(3));
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void test_evalExpr_subscript_one() throws RecognitionException
	{
		QObject expected = createNumeric(2);
		
		QObject target = createNumeric(1);
		target.set(1, expected);
		_intp._curEnv.put("a", target);
		
		Tree tree = parseExpression("a[2]");
		QObject actual = _intp.evalExpr(tree);
		
		assertNumericEquals(expected, actual);
	}
	
	@Test
	public void test_eval_subscript_logical() throws RecognitionException
	{
		QObject expected = createNumeric(2);
		CommonTree tree = parseExpression("(1:3)[c(FALSE, TRUE, FALSE)]");
		QObject actual = _intp.evalExpr(tree);
		assertNumericEquals(expected, actual);
	}
	
	@Test
	public void test_eval_subscript_logical_firstElement()
	{
		QObject expected = createNumeric(1);
		QObject actual = _intp.eval("(1:3)[c(TRUE, FALSE, FALSE)]");
		assertNumericEquals(expected, actual);
	}
	
	@Test
	public void test_eval_mean()
	{
		QObject expected = createNumeric(2);
		QObject actual = _intp.eval("mean(1:3)");
		assertNumericEquals(expected, actual);
	}
	
	
	@Test
	public void test_evalExpr_subscript_two() throws RecognitionException
	{
		QObject q2 = createNumeric(2);
		
		QObject target = createNumeric(1);
		target.set(1, q2);
		_intp._curEnv.put("a", target);
		
		Tree tree = parseExpression("a[1:2]");
		QObject actual = _intp.evalExpr(tree);

		assertEquals(2, actual.getLength());
		assertNumericEquals(createNumeric(1), actual.get(0));
		assertNumericEquals(createNumeric(2), actual.get(1));
	}
	
	CommonTree createTree(int type, String val)
	{
		return new CommonTree(new CommonToken(type, val));
	}
	
	@Test
	public void test_evalExpr_symbol()
	{
		QInterpreter intp = createInterpreter();
		Object expected = intp._curEnv.get("c");
		
		Object actual = intp.evalExpr(createTree(QParser.SYMBOL, "c"));
		assertEquals(expected, actual);
	}

	private CommonTree createIntTree(String val) {
		return createTree(QParser.DecimalLiteral, val);
	}
	
	@Test
	public void test_eval_plus_2statements()
	{
		QObject expected = QObject.createNumeric(9);
		QInterpreter intp = createInterpreter();
		QObject actual = intp.eval("2+3\n4+5");
		assertNumericEquals(expected, actual);
	}
	
	@Test
	public void test_eval_plus_3terms()
	{
		QObject expected = QObject.createNumeric(6);
		QInterpreter intp = createInterpreter();
		QObject actual = intp.eval("1+2+3");
		assertNumericEquals(expected, actual);
	}
	
	@Test
	public void test_evalBinary_assign() throws RecognitionException
	{
		Tree t = parseExpression("x<-3");

		assertNull(_intp._curEnv.get("x"));

		QObject ret = _intp.evalBinary(t.getChild(0), t.getChild(1), t.getChild(2));
		assertEquals(QObject.Null, ret);
		
		assertNumericEquals(createNumeric(3), _intp._curEnv.get("x"));
	}
	
	@Test
	public void test_evalBinary_eqAssign() throws RecognitionException
	{
		Tree t = parseExpressionOrAssign("x=3");

		//the same as test_evalBinary_assign()...
		assertNull(_intp._curEnv.get("x"));

		QObject ret = _intp.evalBinary(t.getChild(0), t.getChild(1), t.getChild(2));
		assertEquals(QObject.Null, ret);
		
		assertNumericEquals(createNumeric(3), _intp._curEnv.get("x"));
	}
	
	@Test
	public void test_evalBinary_plus() throws RecognitionException
	{
		QObject expected = QObject.createNumeric(5);
		QObject actual = evalSimpleBinary("2+3");
		assertNumericEquals(expected, actual);
	}
	
	@Test
	public void test_evalBinary_seq() throws RecognitionException
	{
		QObject actual = evalSimpleBinary("3:5");

		assertEquals(3, actual.getLength());
		assertNumericEquals(createNumeric(3), actual.get(0));
		assertNumericEquals(createNumeric(4), actual.get(1));
		assertNumericEquals(createNumeric(5), actual.get(2));
		
	}
	
	@Test
	public void test_evalPlus_normal()
	{
		QObject expected = QObject.createNumeric(3);
		
		QObject arg1 = QObject.createInt(1);
		QObject arg2 = QObject.createInt(2);
		QObject actual = _intp.evalPlus(arg1, arg2);
		assertNumericEquals(expected, actual);
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
		assertNumericEquals(createNumeric(4), actual.get(0));
		assertNumericEquals(createNumeric(5), actual.get(1));
	}
		
	private QObject createNumeric(int i) {
		return QObject.createNumeric(i);
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
		assertNumericEquals(createNumeric(4), actual.get(0));
		assertNumericEquals(createNumeric(5), actual.get(1));
	}
	
	@Test
	public void test_assignToFormalList_nullFormalList_multipleArg() throws RecognitionException
	{
		Environment target = callAssignToFormalListWithNullFormalList("c(1, 2, 3)");
		QObject ret = target.get(ARGNAME);
		
		assertVector123(ret);
	}

	// code should include funcname, like "c(1, 2, 3)"
	private Environment callAssignToFormalListWithNullFormalList(String code) throws RecognitionException {
		Tree subList = buildSubList(code);
		
		Environment target = new Environment(null);
		_intp.assignToFormalList(subList, null, target);
		return target;
	}
	
	private Environment callAssignToFormalList(String formalListCode, String code) throws RecognitionException {
		Tree formalList = parseFormList(formalListCode);
		Tree subList = buildSubList(code);
		
		Environment target = new Environment(null);
		_intp.assignToFormalList(subList, formalList, target);
		return target;
	}
	
	@Test
	public void test_assignToFormalList_nullFormalList_single() throws RecognitionException
	{
		Environment target = callAssignToFormalListWithNullFormalList("c(1)");
		QObject actual = target.get(ARGNAME);
		
		assertEquals("list", actual.getMode());
		assertEquals(1, actual.getLength());
		assertNumericEquals(QObject.createNumeric(1), actual.get(0));
	}
	
	private void assertVector123(QObject ret) {
		assertEquals(3, ret.getLength());
		assertNumericEquals(QObject.createNumeric(1), ret.get(0));
		assertNumericEquals(QObject.createNumeric(2), ret.get(1));
		assertNumericEquals(QObject.createNumeric(3), ret.get(2));
	}
	
	public final String ARGNAME = "__arg__";
	
	@Test
	public void test_assignToFormalList() throws RecognitionException
	{
		Tree subList = buildSubList("c(1)");
		
		Environment target = new Environment(null);
		_intp.assignToFormalList(subList, null, target);
		QObject args = target.get(ARGNAME);
		
		assertNotNull(args);
		assertEquals("list", args.getMode());
		assertEquals(1.0, args.get(0).getValue());
	}

	@Test
	public void test_assignToFormalList_begEnd() throws RecognitionException
	{
		Environment target = callAssignToFormalList("beg, end", "seq(1, 10)");
		
		QObject beg = target.get("beg");
		QObject end = target.get("end");
		
		assertNotNull(beg);
		assertNotNull(end);
		assertNumericEquals(createNumeric(1), beg);
		assertNumericEquals(createNumeric(10), end);
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
