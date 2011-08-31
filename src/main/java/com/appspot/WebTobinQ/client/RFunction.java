package com.appspot.WebTobinQ.client;

import org.antlr.runtime.tree.Tree;

public class RFunction extends RObject {
	Tree _body;
	Tree _formalList;
	
	public RFunction(Tree formalList, Tree body)
	{
		super("function");
		_formalList = formalList;
		_body = body;
	}
	
	public Tree getFormalList() { return _formalList; }
	public Tree getBody() { return _body; }
	
	public boolean isPrimitive() { return false; }
	public RObject callPrimitive(Environment funcEnv, QInterpreter intp)
	{
		return null;
	}

	// "c"
	public static RFunction createConcatinate()
	{
		return new RFunction(null, null){
			public boolean isPrimitive() {return true; }
			public RObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				RObject args = funcEnv.get("...");
				// should validate args here.
				return args;
				
			}
		};
	}
	
	public String toString()
	{
		return "function ...";
	}
}
