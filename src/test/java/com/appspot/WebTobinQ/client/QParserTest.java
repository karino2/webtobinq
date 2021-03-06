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
import com.appspot.WebTobinQ.client.QParser.lexpr_return;
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
	
	public static CommonTree parseLExpression(String code) throws RecognitionException {
		QParser parser = createParser(code);
		
 		lexpr_return actual = parser.lexpr();
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
	
	@Test
	public void test_expr_subscript() throws RecognitionException
	{
		// (XXSUBSCRIPT [ a (XXSUBLIST (XXSUB1 1)))
		CommonTree actual_tree = parseExpression("a[1]");
		assertEquals(QParser.XXSUBSCRIPT, actual_tree.getType());
		assertEqualsNoType("[", actual_tree.getChild(0));
		assertEquals(QParser.XXSUBLIST, actual_tree.getChild(2).getType());
	}
	
	@Test
	public void test_expr_subscript_row() throws RecognitionException
	{
		// (XXSUBSCRIPT [ a (XXSUBLIST (XXSUB1 1) XXSUB0))
		CommonTree actual_tree = parseExpression("a[1, ]");
		assertEquals("(XXSUBSCRIPT [ a (XXSUBLIST (XXSUB1 1) XXSUB0))", actual_tree.toStringTree());
	}
	
	@Test
	public void test_expr_subscript_subscript() throws RecognitionException
	{
		CommonTree actual_tree = parseExpression("a[[1]][2]");
		assertEquals("(XXSUBSCRIPT [ (XXSUBSCRIPT [[ a (XXSUBLIST (XXSUB1 1))) (XXSUBLIST (XXSUB1 2)))", actual_tree.toStringTree());
	}
	
	@Test
	public void test_expr_lvalSubscript() throws RecognitionException
	{
		CommonTree actual_tree = parseExpression("a[1]-b[1]");
		// (XXBINARY - (XXSUBSCRIPT [ a (XXSUBLIST (XXSUB1 1))) (XXSUBSCRIPT [ b (XXSUBLIST (XXSUB1 1))))
		assertEquals(3, actual_tree.getChildCount());
	}
	
	@Test
	public void test_expr_function() throws RecognitionException
	{
		CommonTree actual_tree = parseExpression("function(){1; 2}");
		assertEquals("(XXDEFUN XXFORMALLIST (XXEXPRLIST 1 2))", actual_tree.toStringTree());
	}
	
	@Test
	public void test_expr_function_newline() throws RecognitionException
	{
		CommonTree expect_tree = parseExpression("function(){1; 2}");
		CommonTree actual_tree = parseExpression("function(){\n1; 2}");
		assertEquals(expect_tree.toStringTree(), actual_tree.toStringTree());
	}
	
	public void debP(CommonTree tree)
	{
		System.out.println(tree.toStringTree());		
	}
	
	@Test
	public void test_lexpr_for() throws RecognitionException
	{
		CommonTree actual_tree = parseLExpression("for(i in 1:10){ b <- i*2; e <- i*13;}");
		assertEquals("(XXFOR (XXFORCOND i (XXBINARY : 1 10)) (XXEXPRLIST (XXBINARY <- b (XXBINARY * i 2)) (XXBINARY <- e (XXBINARY * i 13))))", actual_tree.toStringTree());
	}
	
	
	@Test
	public void test_expr_paren() throws RecognitionException
	{
		CommonTree actual_tree = parseExpression("(1+2)/3");
		// (XXBINARY / (XXPAREN (XXBINARY + 1 2)) 3)
		assertEquals(QParser.XXPAREN, actual_tree.getChild(1).getType());
	}
	
	@Test
	public void test_expr_dotsymbol() throws RecognitionException
	{
		CommonTree actual = parseExpression("gdp.year");
		assertEquals("gdp.year", actual.getText());
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
		QLexer lex = createLexer(code);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		QParser parser = new QParser(tokens);
		return parser;
	}

	private static QLexer createLexer(String code) {
		CharStream codes = new ANTLRStringStream(code);
		QLexer lex = new QLexer(codes);
		return lex;
	}

}
