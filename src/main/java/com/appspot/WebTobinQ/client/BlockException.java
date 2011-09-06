package com.appspot.WebTobinQ.client;

import org.antlr.runtime.tree.Tree;

public class BlockException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 725554607307755260L;

	Tree _currentValueNode;
	public BlockException(Tree currentValue) {
		_currentValueNode = currentValue;
	}
	public BlockException() {
		this(null);
	}
}
