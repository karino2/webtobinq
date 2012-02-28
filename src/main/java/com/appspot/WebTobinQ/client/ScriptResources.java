package com.appspot.WebTobinQ.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface ScriptResources extends ClientBundle {
	public static final ScriptResources INSTANCE = GWT.create(ScriptResources.class);
	@Source("scripts/hpfilter.R")
	TextResource hpfilter();
}
