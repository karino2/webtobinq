package com.appspot.WebTobinQ.client;

import java.util.HashMap;

public class Environment {
	HashMap<String, RObject> _curEnv;
	Environment _parent;
	public Environment(Environment parent)
	{
		_parent = parent;
		_curEnv = new HashMap<String, RObject>();
	}
	
	public RObject get(String key)
	{
		if(_curEnv.containsKey(key))
			return _curEnv.get(key);
		if(_parent == null)
			return null;
		return _parent.get(key);
	}
	
	public void put(String key, RObject obj)
	{
		_curEnv.put(key, obj);
	}
}
