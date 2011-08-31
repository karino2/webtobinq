package com.appspot.WebTobinQ.client;

public class RInt extends RObject {
	int _val;
	public RInt(int val)
	{
		this();
		_val = val;
	}
	
	// should be length 0, fix later.
	public RInt()
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
		for(RObject obj : _vector)
		{
			if(buf.length() != 0)
				buf.append(" ");
			if(obj == RObject.NA)
				buf.append("NA");
			else
				buf.append(((RInt)obj).getValue());
		}
		return buf.toString();
	}
	
	
	public RObject RShallowClone()
	{
		return new RInt(_val);
	}
	
	public void set(int i, RObject arg)
	{
		if(i == 0){
			if(arg == RObject.NA) // can't support now...
				_val = 0;
			else
			{
				_val = ((RInt)arg).getValue();
			}
		}
		super.set(i, arg);
	}

	// R equals is binary operater who handles vector.
	// this is Java equals, it might be confusing. and almost only for test.
	public boolean equals(Object arg)
	{
		RObject robj = (RObject)arg;

		if(robj == null)
			return false;
		if(robj.getMode() != "numeric")
			return false;
		RInt rval = (RInt)robj;
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

	private boolean equalOne(RObject l, RObject r) {
		if(l.getMode() != "numeric" || r.getMode() != "numeric")
			return false;
		
		return ((RInt)l).getValue() == ((RInt)r).getValue();
	}
}
