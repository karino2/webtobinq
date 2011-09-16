package com.appspot.WebTobinQ.client;


public class ForestNode <E>{
	public interface Traversable<E> {
		public E getChild(E elem, int i);
		public E getParent(E elem);
		public int getChildCount(E elem);
		public int getChildIndex(E elem);
	}
	
	public enum Edge
	{
		Leading,
		Trailing
	}
	private Edge _edge;
	private E _node;
	
	private Traversable<E> _traversable;
	public ForestNode(Traversable<E> trav, Edge e, E elem)
	{
		_traversable = trav;
		_edge = e;
		_node = elem;		
	}
	
	public ForestNode(Edge edge, E node)
	{
		this(null, edge, node);
	}
	public E getElement()
	{
		return _node;
	}
	public Edge getEdge()
	{
		return _edge;
	}
	private ForestNode<E> createNode(Edge e, E elem)
	{
		return new ForestNode<E>(_traversable, e, elem);
	}
	public ForestNode<E> getChild(Edge e, int i) {
		return createNode(e, _traversable.getChild(_node, i));
	}
	public ForestNode<E> getParent(Edge e) {
		E parent = _traversable.getParent(_node);
		if(parent == null)
			return null;
		return createNode(e, parent);
	}
	public ForestNode<E> newEdge(Edge newE)
	{
		return createNode(newE, _node);
	}
	public int getChildCount() { return _traversable.getChildCount(_node); }
	public int getChildIndex() { return _traversable.getChildIndex(_node); }
}
