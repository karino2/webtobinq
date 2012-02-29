package com.appspot.WebTobinQ.client;

import java.util.HashMap;

import org.antlr.runtime.tree.Tree;

public class Environment {
	class Pair {
		Pair(QObject obj, Tree sexp) {
			_obj = obj;
			_sexp = sexp;
		}
		QObject _obj;
		Tree _sexp;
	}
	HashMap<String, Pair> _curEnv;
	Environment _parent;
	public Environment(Environment parent)
	{
		_parent = parent;
		_curEnv = new HashMap<String, Pair>();
	}
	
	public QObject get(String key)
	{
		if(_curEnv.containsKey(key))
			return _curEnv.get(key)._obj;
		if(_parent == null)
			return null;
		return _parent.get(key);
	}
	
	public Tree getSexp(String key)
	{
		if(_curEnv.containsKey(key))
			return _curEnv.get(key)._sexp;
		if(_parent == null)
			return null;
		return _parent.getSexp(key);
	}

	
	public void put(String key, QObject obj)
	{
		_curEnv.put(key, new Pair(obj, null));
	}
	public void put(String key, QObject obj, Tree sexp)
	{
		_curEnv.put(key, new Pair(obj, sexp));
	}
}
