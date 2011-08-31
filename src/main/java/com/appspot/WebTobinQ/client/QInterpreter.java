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
	Environment _curEnv;
	Environment _rootEnv;
	
	public void registerBuiltinFunction()
	{
		_curEnv.put("c", RFunction.createConcatinate());
	}
	
	public QInterpreter(Writable console) {
		_curEnv = _rootEnv = new Environment(null);
		this._console = console;
		registerBuiltinFunction();
	}
	
	public void println(String str)
	{
		_console.write(str + "\n");
	}
	
	void debugPrint(String str)
	{
		println("<deb#>" + str);
	}
	
	public Object eval(String codestext)
	{
		Object ret = null;
		try {
			Tree tree = buildTree(codestext);
			debugPrint(tree.toStringTree());
			ret = evalTree(tree);
			println(ret.toString());
		} catch (RecognitionException e) {
			debugPrint("#parse Error!");
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

	// (XXVALUE (XXBINARY + 2 3)), evalValue seems bad name.
	public RObject evalValue(Tree t) {
		RObject ret = null;
		if(t.getChildCount() == 0)
			return ret;
		return evalTerm(t.getChild(0));
	}
	
	public RObject evalTerm(Tree term)
	{
		if(term.getType() == QParser.DecimalLiteral)
			return new RInt(Integer.valueOf(term.getText()));
		if(term.getType() == QParser.XXBINARY) // recursive call now.
			return evalBinary(term.getChild(0), term.getChild(1), term.getChild(2));
		if(term.getType() == QParser.SYMBOL)
			return _curEnv.get(term.getText());
		if(term.getType() == QParser.XXFUNCALL)
			return evalCallFunction(term);
		System.out.println(term.getType());
		throw new RuntimeException("NYI");
	}

	// (XXFUNCALL c (XXSUBLIST (XXSUB1 1) (XXSUB1 2)))
	RObject evalCallFunction(Tree term) {
		RObject funcCand = (RObject)_curEnv.get(term.getChild(0).getText());
		if(funcCand.getMode() == "function")
		{
			RFunction func = (RFunction)funcCand;
			Environment outer = _curEnv;
			_curEnv = new Environment(_curEnv);
			assignToFormalList(term.getChild(1), func.getFormalList(), _curEnv);
			RObject ret;
			if(func.isPrimitive())
				ret = func.callPrimitive(_curEnv, this);
			else
				ret = evalValue(func.getBody());
			_curEnv = outer;
			return ret;
		}
		// error handling, can I use exception in JS env?
		_console.write("right value of func call is not function. After investigate exception, I'll handle.");
		return null;
	}
	
	// (XXSUBLIST (XXSUB1 1) (XXSUB1 2))
	void assignToFormalList(Tree subList, Tree formalList,
			Environment funcEnv) {
		RObject args = evalSubList(subList);
		// may be we should use another symbol, but use this for a while.
		funcEnv.put("...", args);
	}

	// (XXSUBLIST (XXSUB1 1) (XXSUB1 2))
	// Currently, tmp implementation.
	RObject evalSubList(Tree subList) {
		RObject args = new RInt();
		for(int i = 0; i < subList.getChildCount(); i++)
		{
			RObject arg = evalTerm(subList.getChild(i).getChild(0));
			args.set(i, arg);
		}
		return args;
	}

	RObject evalPlus(Object arg1, Object arg2)
	{
		RInt r1 = (RInt)arg1;
		RInt r2 = (RInt)arg2;
		return new RInt(r1.getValue()+r2.getValue());
	}

	public RObject evalBinary(Tree op, Tree arg1, Tree arg2) {
		if("+".equals(op.getText()))
		{
			Object term1 = evalTerm(arg1);
			Object term2 = evalTerm(arg2);
			return evalPlus(term1, term2);
		}
		throw new RuntimeException("NYI");
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
