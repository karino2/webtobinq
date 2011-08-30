package com.appspot.WebTobinQ.client;

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

public class AssertTree {
	static CommonTree createTree(int type, String text)
	{
		return new CommonTree(new CommonToken(type, text));
	}
	

	@SuppressWarnings("unchecked")
	public static void assertTreeNode(Object expect, Tree actual)
	{
		if(String.class.isInstance(expect))
		{
			assertThat(actual.getType(), anyOf(is(QParser.STR_CONST), is(QParser.SYMBOL)));
			assertEquals(expect, actual.getText());
		}
		else if(Integer.class.isInstance(expect))
		{
			assertNotSame("", actual.getText());
			assertEquals(expect, Integer.valueOf(actual.getText()));
		}
	}

	// ["XXVALUE" ["XXBINARY" 2 3]]
	public static void assertTreeEquals(Object[] expects, Tree actual) {
		assertNotSame(0, expects.length);
		assertTreeNode(expects[0], actual);
		if(expects.length > 1)
		{
			assertEquals(expects.length, actual.getChildCount()+1);
			for(int i = 0; i < expects.length-1; i++)
			{
				Object childExpect = expects[i+1];
				Tree childActual = actual.getChild(i);
				if(childExpect.getClass().isArray())
					assertTreeEquals((Object[])childExpect, childActual);
				else
					assertTreeNode(childExpect, childActual);
			}
		}
	}

}
