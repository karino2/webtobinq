package com.appspot.WebTobinQ.client;

import static org.junit.Assert.*;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.junit.Test;

import org.antlr.runtime.Token;

import org.antlr.runtime.CommonToken;

import com.appspot.WebTobinQ.client.ForestNode.Edge;



public class ForestIteraterTest {
	class TreeForTest extends CommonTree
	{
		String _tag;
		TreeForTest(String a, Token token)
		{
			super(token);
			_tag = a;
		}
	}
	
	CommonTree createTree(String tag)
	{
		return new TreeForTest(tag, new CommonToken(0, tag));
	}
	@Test
	public void test_iterater()
	{
		/*
		 * a--b-+-d
		 *    | |
		 *    | +-e
		 *    c
		 */
		CommonTree a = createTree("a");
		CommonTree b = createTree("b");
		CommonTree c = createTree("c");
		CommonTree d = createTree("d");
		CommonTree e = createTree("e");
		a.addChild(b);
		a.addChild(c);
		b.addChild(d);
		b.addChild(e);
		
		ForestIterater iter = new ForestIterater(a);
		
		assertTrue(iter.hasMoreElements());
		assertNode(Edge.Leading, a, iter.nextElement());
		
		assertTrue(iter.hasMoreElements());
		assertNode(Edge.Leading, b, iter.nextElement());
		
		assertTrue(iter.hasMoreElements());
		assertNode(Edge.Leading, d, iter.nextElement());
		
		assertTrue(iter.hasMoreElements());
		assertNode(Edge.Trailing, d, iter.nextElement());
		
		assertTrue(iter.hasMoreElements());
		assertNode(Edge.Leading, e, iter.nextElement());
		
		assertTrue(iter.hasMoreElements());
		assertNode(Edge.Trailing, e, iter.nextElement());
		
		assertTrue(iter.hasMoreElements());
		assertNode(Edge.Trailing, b, iter.nextElement());
		
		assertTrue(iter.hasMoreElements());
		assertNode(Edge.Leading, c, iter.nextElement());
		
		assertTrue(iter.hasMoreElements());
		assertNode(Edge.Trailing, c, iter.nextElement());
		
		assertTrue(iter.hasMoreElements());
		assertNode(Edge.Trailing, a, iter.nextElement());
		
		assertFalse(iter.hasMoreElements());
	}
	
	void assertNode(Edge expectE, Tree expectNode, ForestNode actual)
	{
		assertEquals(expectE, actual.getEdge());
		assertEquals(expectNode, actual.getNode());
	}
}
