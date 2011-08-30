package com.appspot.WebTobinQ.client;

import static org.junit.Assert.*;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.junit.Test;

import com.appspot.WebTobinQ.client.QParser.expr_return;
import com.appspot.WebTobinQ.client.QParser.formlist_return;


public class QParserTest {
	
	public static CommonTree parseExpression(String code) throws RecognitionException {
		QParser parser = createParser(code);
		
 		expr_return actual = parser.expr();
 	    CommonTree actual_tree = (CommonTree)actual.getTree();
		return actual_tree;
	}
	
	@Test
	public void test_plusBinary() throws RecognitionException
	{
		String code = "2+3";
		CommonTree actual_tree = parseExpression(code);

		assertEquals(QParser.XXBINARY, actual_tree.getType());
		// + is not symbol in R.
		assertEqualsNoType("+", actual_tree.getChild(0));
		assertIntEquals(2, actual_tree.getChild(1));
		assertIntEquals(3, actual_tree.getChild(2));		
		
	}
	
	private void assertEqualsNoType(String expect, Tree actual) {
		assertEquals(expect, actual.getText());
	}
	

	public void assertStringEquals(String expect, Tree actual) {
		assertStringOrSymbol(expect, actual, QParser.STR_CONST);
	}
	
	public void assertSymbolEquals(String expect, Tree actual) {
		assertStringOrSymbol(expect, actual, QParser.SYMBOL);
	}

	private void assertStringOrSymbol(String expect, Tree actual, int expectType) {
		assertEquals(expectType, actual.getType());
		assertEquals(expect, actual.getText());
	}

	private void assertIntEquals(int expect, Tree actual) {
		assertEquals(QParser.DecimalLiteral, actual.getType());
		assertEquals(expect, (int)Integer.valueOf(actual.getText()));
	}
	
	@Test
	public void test_nullArg() throws RecognitionException
	{
		String code = "";
		CommonTree actual_tree = parseFormList(code);
 	    
 	    assertEquals(actual_tree.getType(), QParser.XXFORMALLIST);
 	    assertEquals(actual_tree.getChildCount(), 0);
	}
	
	// @Test
	public void test_oneArgSymbol() throws RecognitionException
	{
		String code = "x";
		CommonTree actual_tree = parseFormList(code);
		
		System.out.println(actual_tree.toStringTree());
 	    
 	    assertEquals(actual_tree.getType(), QParser.XXFORMALLIST);
 	    assertEquals(actual_tree.getChildCount(), 1);
	}


	private static CommonTree parseFormList(String code) throws RecognitionException {
		QParser parser = createParser(code);
		
 		formlist_return actual = parser.formlist();
 	    CommonTree actual_tree = (CommonTree)actual.getTree();
		return actual_tree;
	}

	private static QParser createParser(String code) {
		CharStream codes = new ANTLRStringStream(code);
		QLexer lex = new QLexer(codes);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		QParser parser = new QParser(tokens);
		return parser;
	}

}
