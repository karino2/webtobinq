package com.appspot.WebTobinQ.client;

import java.util.ArrayList;

public class QObject {
	private String _name;
	String _mode;
	ArrayList<QObject> _vector = null;
	public QObject(String name, String mode)
	{
		_name = name;
		_mode = mode;
	}
	
	public QObject(String mode) {
		this("", mode);
	}
	
	public String getMode()
	{
		return _mode;
	}
	
	public static QObject NA = new QObject("NA", "logical");
	
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
	
	public QObject recycle(int upto)
	{
		if(upto < getLength())
			return this;
		ensureVector();
		QObject ret = RShallowClone();
		int index = 0;
		for(int i = 0; i < upto; i++, index++)
		{
			if(index == getLength())
				index = 0;
			ret.set(i, get(index));
		}
		return ret;
	}
	
	public QObject get(int i)
	{
		if(_vector == null)
			return null;
		return _vector.get(i);
	}

	public void set(int i, QObject qObject) {
		ensureVector();
		if(getLength() < i+1)
		{
			extendVectorAndFillNA(i+1);
		}
		if(i == 0)
			_vector.set(i, qObject.RShallowClone());
		else
			_vector.set(i, qObject);		
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
			_vector = new ArrayList<QObject>();
			if(_vector.size() == 0)
				_vector.add(0, RShallowClone());
			else
				_vector.set(0, RShallowClone());
		}
	}
	
	public QObject RShallowClone()
	{
		return this;
	}

}
