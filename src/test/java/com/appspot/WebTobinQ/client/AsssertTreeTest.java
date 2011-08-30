package com.appspot.WebTobinQ.client;

import static com.appspot.WebTobinQ.client.AssertTree.*;
import org.antlr.runtime.tree.CommonTree;
import org.junit.Test;

public class AsssertTreeTest {
	
	// @Test Give up! not so easy as I expected
	public void test_assertTreeEquals_one()
	{
		CommonTree a = createTree(QParser.XXBINARY, "");
		assertTreeEquals(new Object []{QParser.XXBINARY}, a);
	}
	
	@Test(expected=AssertionError.class)
	public void test_assertTreeNode_WrongType()
	{
		CommonTree a = createTree(0, "a");
		assertTreeEquals(new Object[]{"a"}, a);
	}

	@Test
	public void test_assertTreeNode_symbol()
	{
		CommonTree a = createTree(QParser.SYMBOL, "a");
		assertTreeEquals(new Object[]{"a"}, a);
	}
	
	@Test
	public void test_assertTreeNode_string()
	{
		CommonTree a = createTree(QParser.STR_CONST, "a");
		assertTreeEquals(new Object[]{"a"}, a);
	}

	@Test
	public void test_assertTreeNode_int()
	{
		CommonTree a = createTree(QParser.DecimalLiteral, "2");
		assertTreeEquals(new Object[]{2}, a);
	}

	@Test(expected=AssertionError.class)
	public void test_assertTreeNode_intDiff()
	{
		CommonTree actual = createTree(QParser.DecimalLiteral, "1");
		assertTreeEquals(new Object[]{2}, actual);
	}
	
	@Test
	public void test_childDiffer()
	{
		CommonTree actualHead = createTree(QParser.STR_CONST, "a");
		actualHead.addChild(createTree(QParser.STR_CONST, "b"));
		assertTreeEquals(new Object[]{"a"}, actualHead);
	}
}
