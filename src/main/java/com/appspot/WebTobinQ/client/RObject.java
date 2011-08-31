package com.appspot.WebTobinQ.client;

import java.util.ArrayList;

public class RObject {
	private String _name;
	String _mode;
	ArrayList<RObject> _vector = null;
	public RObject(String name, String mode)
	{
		_name = name;
		_mode = mode;
	}
	
	public RObject(String mode) {
		this("", mode);
	}
	
	public String getMode()
	{
		return _mode;
	}
	
	public static RObject NA = new RObject("NA", "logical");
	
	public String toString()
	{
		return _name;
	}
	
	public int getLength()
	{
		if(_vector == null)
			return 1;
		return _vector.size();
	}
	
	public RObject recycle(int upto)
	{
		if(upto < getLength())
			return this;
		ensureVector();
		RObject ret = RShallowClone();
		int index = 0;
		for(int i = 0; i < upto; i++, index++)
		{
			if(index == getLength())
				index = 0;
			ret.set(i, get(index));
		}
		return ret;
	}
	
	public RObject get(int i)
	{
		if(_vector == null)
			return null;
		return _vector.get(i);
	}

	public void set(int i, RObject rObject) {
		ensureVector();
		if(getLength() < i+1)
		{
			extendVectorAndFillNA(i+1);
		}
		if(i == 0)
			_vector.set(i, rObject.RShallowClone());
		else
			_vector.set(i, rObject);		
	}

	// slow.
	private void extendVectorAndFillNA(int upto) {
		for(int i = _vector.size(); i < upto; i++)
		{
			_vector.add(i, NA);
		}
	}

	protected void ensureVector() {
		if(_vector == null)
		{
			_vector = new ArrayList<RObject>();
			if(_vector.size() == 0)
				_vector.add(0, RShallowClone());
			else
				_vector.set(0, RShallowClone());
		}
	}
	
	public RObject RShallowClone()
	{
		return this;
	}

}
