package com.appspot.WebTobinQ.client;

public class QObjectBuilder
{
	QObject _ret = null;
	int _i = 0;
	public void add(QObject o)
	{
		if(_ret == null)
			_ret = o.shallowClone();
		else
			_ret.set(_i, o);
		_i++;
	}
	public QObject result() { return _ret; }
}