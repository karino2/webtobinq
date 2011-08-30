package com.appspot.WebTobinQ.client;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;


import com.appspot.WebTobinQ.client.ForestNode.Edge;

public class QInterpreter {
	
	Writable _console;	
	public Writable getConsole(){ return _console; }
	
	public QInterpreter(Writable console) {
		this._console = console;
	}
	
	public Object eval(String codestext)
	{
		Object ret = null;
		try {
			Tree tree = buildTree(codestext);
			ret = evalTree(tree);
			_console.write("tree="+tree.toStringTree());
		} catch (RecognitionException e) {
			_console.write("#parse Error!\n");
			_console.write(e.toString());
			e.printStackTrace();
		}
		return ret;
	}

	Object evalTree(Tree tree) {
		Object ret = null;
		ForestIterater iter = new ForestIterater(tree);
		while(iter.hasNext())
		{
			ForestNode node = iter.next();
			if(node.getEdge() == Edge.Trailing)
				continue;
			Tree t = node.getElement();
			if(t.getType() == QParser.XXVALUE)
			{
				ret = evalValue(t);
				iter.skipChildren();
			}
		}
		return ret;
	}

	// (XXVALUE (XXBINARY + 2 3))
	public Object evalValue(Tree t) {
		Object ret = null;
		if(t.getChildCount() == 0)
			return ret;
		Tree c = t.getChild(0);
		if(c.getType() == QParser.XXBINARY)
			return evalBinary(c.getChild(0), c.getChild(1), c.getChild(2));
		return ret;
	}
	
	public Object evalTerm(Tree term)
	{
		if(term.getType() == QParser.DecimalLiteral)
			return Integer.valueOf(term.getText());
		return null; // NYI
	}

	public Object evalBinary(Tree op, Tree arg1, Tree arg2) {
		if("+".equals(op.getText()))
		{
			Object term1 = evalTerm(arg1);
			Object term2 = evalTerm(arg2);
			return ((int)((Integer)term1))+((int)((Integer)term2));
		}
		return null;
	}

	private Tree buildTree(String codestext) throws RecognitionException {
		CharStream codes = new ANTLRStringStream(codestext);
		QLexer lex = new QLexer(codes);
		CommonTokenStream tokens = new CommonTokenStream(lex);
		QParser parser = new QParser(tokens);
	 	QParser.prog_return r = parser.prog();
		Tree tree = (Tree)r.getTree();
		return tree;
	}
}
