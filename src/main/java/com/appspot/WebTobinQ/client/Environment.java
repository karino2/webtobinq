package com.appspot.WebTobinQ.client;

import java.util.HashMap;

import org.antlr.runtime.tree.Tree;

public class Environment {
	class Tetra {
		Tetra(QObject obj, Tree sexp) {
			_obj = obj;
			_sexp = sexp;
			_defaultVal = QObject.Null;
		}
		QObject _obj;
		Tree _sexp;
		QObject _defaultVal;
	}
	HashMap<String, Tetra> _curEnv;
	Environment _parent;
	public Environment(Environment parent)
	{
		_parent = parent;
		_curEnv = new HashMap<String, Tetra>();
	}
	
	public QObject get(String key)
	{
		Tetra ent = getEntry(key);
		if(ent != null)
			return ent._obj;
		return null;
	}
	
	public Tetra getEntry(String key)
	{
		if(_curEnv.containsKey(key))
			return _curEnv.get(key);
		if(_parent == null)
			return null;
		return _parent.getEntry(key);
		
	}
	
	public Tree getSexp(String key)
	{
		Tetra ent = getEntry(key);
		if(ent != null)
			return ent._sexp;
		return null;
	}
	
	public QObject getDefaultValue(String key)
	{
		Tetra ent = getEntry(key);
		if(ent != null)
			return ent._defaultVal;
		return null;
	}

	
	public void put(String key, QObject obj)
	{
		_curEnv.put(key, new Tetra(obj, null));
	}
	public void put(String key, QObject obj, Tree sexp)
	{
		_curEnv.put(key, new Tetra(obj, sexp));
	}
	public void putDefault(String key, QObject defVal)
	{
		Tetra ent = getEntry(key);
		ent._defaultVal = defVal;
	}
}
