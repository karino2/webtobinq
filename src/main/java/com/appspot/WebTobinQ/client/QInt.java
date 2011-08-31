package com.appspot.WebTobinQ.client;

public class QInt extends QObject {
	int _val;
	public QInt(int val)
	{
		this();
		_val = val;
	}
	
	// should be length 0, fix later.
	public QInt()
	{
		super("numeric");
	}
	
	public int getValue()
	{
		return _val;
	}
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		ensureVector();
		for(QObject obj : _vector)
		{
			if(buf.length() != 0)
				buf.append(" ");
			if(obj == QObject.NA)
				buf.append("NA");
			else
				buf.append(((QInt)obj).getValue());
		}
		return buf.toString();
	}
	
	
	public QObject RShallowClone()
	{
		return new QInt(_val);
	}
	
	public void set(int i, QObject arg)
	{
		if(i == 0){
			if(arg == QObject.NA) // can't support now...
				_val = 0;
			else
			{
				_val = ((QInt)arg).getValue();
			}
		}
		super.set(i, arg);
	}

	// R equals is binary operater who handles vector.
	// this is Java equals, it might be confusing. and almost only for test.
	public boolean equals(Object arg)
	{
		QObject robj = (QObject)arg;

		if(robj == null)
			return false;
		if(robj.getMode() != "numeric")
			return false;
		QInt rval = (QInt)robj;
		if(rval.getLength() != getLength())
			return false;
		if(getLength() == 1)
		{
			return equalOne(this, rval);
		}
		for(int i = 0; i < rval.getLength(); i++)
		{
			// treat NA as false.
			if(!equalOne(get(i), rval.get(i)))
				return false;
		}
		return true;
	}

	private boolean equalOne(QObject l, QObject r) {
		if(l.getMode() != "numeric" || r.getMode() != "numeric")
			return false;
		
		return ((QInt)l).getValue() == ((QInt)r).getValue();
	}
}
