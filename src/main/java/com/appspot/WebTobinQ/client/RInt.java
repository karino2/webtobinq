package com.appspot.WebTobinQ.client;

public class RInt extends RObject {
	int _val;
	public RInt(int val)
	{
		this();
		_val = val;
		set(0, this);
	}
	
	// should be length 0, fix later.
	public RInt()
	{
		super("numeric");
		ensureVector();
	}
	
	public int getValue()
	{
		return _val;
	}
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		for(RObject obj : _vector)
		{
			if(buf.length() != 0)
				buf.append(" ");
			buf.append(((RInt)obj).getValue());
		}
		return buf.toString();
	}

	// R equals is binary operater who handles vector.
	// this is Java equals, it might be confusing.
	public boolean equals(Object arg)
	{
		RObject robj = (RObject)arg;

		if(robj == null)
			return false;
		if(robj.getMode() == "numeric")
			return ((RInt)robj).getValue() == _val;
		return false;
	}
}
