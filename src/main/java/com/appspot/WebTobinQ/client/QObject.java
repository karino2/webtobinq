package com.appspot.WebTobinQ.client;

import java.util.ArrayList;

public class QObject {
	public static QObject NA = new QObject("logical");
	public static QObject Null = new QObject("NULL");

	
	Object _val;
	String _mode;
	ArrayList<QObject> _vector = null;
	
	
	public QObject(String mode, Object val)
	{
		this(mode);
		_val = val;
	}
	
	public QObject(String mode)
	{
		_mode = mode;
	}
	
	public static QObject createInt(int val)
	{
		return new QObject("integer", val);
	}
	
	public static QObject createNumeric(double val)
	{
		return new QObject("numeric", val);
	}
	
	public String getMode()
	{
		return _mode;
	}
	
	public Object getValue()
	{
		return _val;
	}
	
	
	public String toString()
	{
		if(this == NA)
			return "NA";
		StringBuffer buf = new StringBuffer();
		ensureVector();
		for(QObject obj : _vector)
		{
			if(buf.length() != 0)
				buf.append(" ");
			if(obj == QObject.NA)
				buf.append("NA");
			else
				buf.append(obj._val.toString());
		}
		return buf.toString();
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
		QObject ret = shallowClone();
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
		if(getMode() == "list")
		{
			setToList(i, qObject);
			return;
		}
		
		if(getLength() < i+1)
		{
			extendVectorAndFillNA(i+1);
		}
		if(i == 0) {
			// something strange.
			_val = qObject._val;
			_vector.set(i, qObject.shallowClone());
		}
		else
			_vector.set(i, qObject);		
	}

	private void setToList(int i, QObject qObject) {
		if(getLength() < i+1)
			extendVectorAndFillValue(i+1, QObject.Null);
		_vector.set(i, qObject);
	}

	public void extendVectorAndFillNA(int upto) {
		extendVectorAndFillValue(upto, NA);
	}
	
	// slow.
	public void extendVectorAndFillValue(int upto, QObject obj)
	{
		for(int i = _vector.size(); i < upto; i++)
		{
			_vector.add(i, obj);
		}		
	}

	void ensureVector() {
		if(_vector == null)
		{
			_vector = new ArrayList<QObject>();
			if(getMode() == "list")
				return;
			if(_vector.size() == 0)
				_vector.add(0, shallowClone());
			else
				_vector.set(0, shallowClone());
		}
	}
	
	public QObject shallowClone()
	{
		return new QObject(getMode(), _val);
	}

	// R equals is binary operater who handles vector.
	// this is Java equals, it might be confusing. and almost only for test.
	public boolean equals(Object arg)
	{
		QObject robj = (QObject)arg;

		if(robj == null)
			return false;
		if(robj.getLength() != getLength())
			return false;		
		if(getLength() == 1)
		{
			return equalOne(this, robj);
		}
		for(int i = 0; i < robj.getLength(); i++)
		{
			// treat NA as false.
			if(!equalOne(get(i), robj.get(i)))
				return false;
		}
		return true;
	}

	private boolean equalOne(QObject l, QObject r) {
		return l.getMode() == r.getMode()
		 		&&(l.getValue() == r.getValue());
	}
}
