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

	public ForestIterater<Tree> createIterater(Tree root)
	{
		return QInterpreter.createIterater(root);
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
		
		ForestIterater<Tree> iter = createIterater(a);
		
		assertTrue(iter.hasNext());
		assertNode(Edge.Leading, a, iter.next());
		
		assertTrue(iter.hasNext());
		assertNode(Edge.Leading, b, iter.next());
		
		assertTrue(iter.hasNext());
		assertNode(Edge.Leading, d, iter.next());
		
		assertTrue(iter.hasNext());
		assertNode(Edge.Trailing, d, iter.next());
		
		assertTrue(iter.hasNext());
		assertNode(Edge.Leading, e, iter.next());
		
		assertTrue(iter.hasNext());
		assertNode(Edge.Trailing, e, iter.next());
		
		assertTrue(iter.hasNext());
		assertNode(Edge.Trailing, b, iter.next());
		
		assertTrue(iter.hasNext());
		assertNode(Edge.Leading, c, iter.next());
		
		assertTrue(iter.hasNext());
		assertNode(Edge.Trailing, c, iter.next());
		
		assertTrue(iter.hasNext());
		assertNode(Edge.Trailing, a, iter.next());
		
		assertFalse(iter.hasNext());
	}
	
	void assertNode(Edge expectE, Tree expectNode, ForestNode<Tree> actual)
	{
		assertEquals(expectE, actual.getEdge());
		assertEquals(expectNode, actual.getElement());
	}
}
