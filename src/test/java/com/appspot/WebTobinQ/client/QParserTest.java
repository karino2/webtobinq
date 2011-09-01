package com.appspot.WebTobinQ.client;

import static org.junit.Assert.*;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.junit.Test;

import com.appspot.WebTobinQ.client.QParser.expr_or_assign_return;
import com.appspot.WebTobinQ.client.QParser.expr_return;
import com.appspot.WebTobinQ.client.QParser.formlist_return;
import com.appspot.WebTobinQ.client.QParser.prog_return;


public class QParserTest {
	public static CommonTree parseProg(String code) throws RecognitionException {
		QParser parser = createParser(code);
		
 		prog_return actual = parser.prog();
 	    CommonTree actual_tree = (CommonTree)actual.getTree();
		return actual_tree;
	}
	
	@Test
	public void test_prog_twoAssign() throws RecognitionException
	{
		CommonTree actual = parseProg("x<-c(1,2,3)\ny<-c(5,76)");
		assertEquals(2, actual.getChildCount());
		assertEquals(QParser.XXVALUE, actual.getChild(0).getType());
		assertEquals(QParser.XXVALUE, actual.getChild(1).getType());
	}

	@Test
	public void test_prog_threeAssign() throws RecognitionException
	{
		CommonTree actual = parseProg("x<-c(1,2,3)\ny<-c(5,76)\nz<-c(2, 3)");
		assertEquals(3, actual.getChildCount());
		assertEquals(QParser.XXVALUE, actual.getChild(0).getType());
		assertEquals(QParser.XXVALUE, actual.getChild(1).getType());
		assertEquals(QParser.XXVALUE, actual.getChild(2).getType());
	}
	
	
	public static CommonTree parseExpression(String code) throws RecognitionException {
		QParser parser = createParser(code);
		
 		expr_return actual = parser.expr();
 	    CommonTree actual_tree = (CommonTree)actual.getTree();
		return actual_tree;
	}
	
	public static CommonTree parseExpressionOrAssign(String code) throws RecognitionException {
		QParser parser = createParser(code);
		
 		expr_or_assign_return actual = parser.expr_or_assign();
 	    CommonTree actual_tree = (CommonTree)actual.getTree();
		return actual_tree;
	}
	
	@Test
	public void test_expr_plusBinary() throws RecognitionException
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
	public void test_formlist_nullArg() throws RecognitionException
	{
		String code = "";
		CommonTree actual_tree = parseFormList(code);
 	    
 	    assertEquals(actual_tree.getType(), QParser.XXFORMALLIST);
 	    assertEquals(actual_tree.getChildCount(), 0);
	}
	
	@Test
	public void test_formlist_oneArgSymbol() throws RecognitionException
	{
		String code = "x";
		CommonTree actual_tree = parseFormList(code);
		
 	    assertEquals(actual_tree.getType(), QParser.XXFORMALLIST);
 	    assertEquals(actual_tree.getChildCount(), 1);
	}


	public static CommonTree parseFormList(String code) throws RecognitionException {
		QParser parser = createParser(code);
		
 		formlist_return actual = parser.formlist();
 	    CommonTree actual_tree = (CommonTree)actual.getTree();
		return actual_tree;
	}

	public static QParser createParser(String code) {
		CharStream codes = new ANTLRStringStream(code);
		QLexer lex = new QLexer(codes);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		QParser parser = new QParser(tokens);
		return parser;
	}

}
