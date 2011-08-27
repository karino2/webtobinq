package com.appspot.WebTobinQ.client;

import java.util.Iterator;

import org.antlr.runtime.tree.Tree;

import com.appspot.WebTobinQ.client.ForestNode.Edge;

public class ForestIterater implements Iterable<ForestNode>, Iterator<ForestNode>{

	Tree _root;
	ForestNode _current;
	
	public ForestIterater(Tree root)
	{
		_root = root;
		_current = null;
	}
	

	public Iterator<ForestNode> iterator() {
		return this;
	}

	public boolean hasNext() {
		return (_current == null) || !(_current.getEdge() == Edge.Trailing &&
				_current.getNode() == _root);
	}

	public ForestNode next() {
		if(_current == null)
		{
			_current = new ForestNode(Edge.Leading, _root);
			return _current;
		}
		Tree node = _current.getNode();
		if(_current.getEdge() == Edge.Leading)
		{
			if(node.getChildCount() == 0)
			{
				_current = new ForestNode(Edge.Trailing, node);
				return _current;
			}
			_current = new ForestNode(Edge.Leading, node.getChild(0));
			return _current;
		}
		Tree parent = node.getParent();
		if(parent ==null)
			throw new RuntimeException("No nexxt node, never reached here");
		int curIndex = node.getChildIndex();
		if(curIndex < parent.getChildCount()-1)
		{
			_current = new ForestNode(Edge.Leading, parent.getChild(curIndex+1));
			return _current;
		}
		// last sibling, go up ward.
		_current = new ForestNode(Edge.Trailing, parent);
		return _current;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
