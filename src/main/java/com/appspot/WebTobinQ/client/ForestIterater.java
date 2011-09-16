package com.appspot.WebTobinQ.client;

import java.util.Iterator;

import com.appspot.WebTobinQ.client.ForestNode.Edge;

public class ForestIterater<E> implements Iterable<ForestNode<E>>, Iterator<ForestNode<E>>{

	ForestNode<E> _root;
	ForestNode<E> _current;
	
	public ForestIterater(ForestNode<E> root)
	{
		_root = root;
	}
		

	public Iterator<ForestNode<E>> iterator() {
		return this;
	}

	public boolean hasNext() {
		return (_current == null) || !(_current.getEdge() == Edge.Trailing &&
				_current.getElement() == _root.getElement());
	}
	
	public void skipChildren()
	{
		_current = _current.newEdge(Edge.Trailing);
	}

	public ForestNode<E> next() {
		if(_current == null)
		{
			_current = _root.newEdge(Edge.Leading);
			return _current;
		}
		if(_current.getEdge() == Edge.Leading)
		{
			if(_current.getChildCount() == 0)
			{
				_current = _current.newEdge(Edge.Trailing);
				return _current;
			}
			_current = _current.getChild(Edge.Leading, 0);
			return _current;
		}
		ForestNode<E> parent = _current.getParent(Edge.Trailing);
		if(parent ==null)
			throw new RuntimeException("No next node, never reached here");
		int curIndex = _current.getChildIndex();
		if(curIndex < parent.getChildCount()-1)
		{
			_current = parent.getChild(Edge.Leading, curIndex+1);
			return _current;
		}
		// last sibling, go up ward.
		_current = parent;
		return _current;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
