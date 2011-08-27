package com.appspot.WebTobinQ.client;

import org.antlr.runtime.tree.Tree;


public class ForestNode {
	public enum Edge
	{
		Leading,
		Trailing
	}
	private Edge _edge;
	private Tree _node;
	public ForestNode(Edge edge, Tree node)
	{
		_edge = edge;
		_node = node;
	}
	public Tree getNode()
	{
		return _node;
	}
	public Edge getEdge()
	{
		return _edge;
	}

}
