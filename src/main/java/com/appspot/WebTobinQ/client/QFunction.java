package com.appspot.WebTobinQ.client;

import org.antlr.runtime.tree.Tree;

public class QFunction extends QObject {
	Tree _body;
	Tree _formalList;
	
	public QFunction(Tree formalList, Tree body)
	{
		super("function");
		_formalList = formalList;
		_body = body;
	}
	
	public Tree getFormalList() { return _formalList; }
	public Tree getBody() { return _body; }
	
	public boolean isPrimitive() { return false; }
	public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
	{
		return null;
	}

	// "c"
	public static QFunction createConcatinate()
	{
		return new QFunction(null, null){
			public boolean isPrimitive() {return true; }
			public QObject callPrimitive(Environment funcEnv, QInterpreter intp)
			{
				QObject args = funcEnv.get("...");
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
