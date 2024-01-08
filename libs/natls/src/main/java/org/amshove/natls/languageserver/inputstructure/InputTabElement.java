package org.amshove.natls.languageserver.inputstructure;

public class InputTabElement extends InputResponseElement
{
	private int tabs;

	protected InputTabElement(int tabs)
	{
		super("tabs"); // TODO: tabposition/column
		this.tabs = tabs;
	}

	public int getTabs()
	{
		return tabs;
	}

	public void setTabs(int tabs)
	{
		this.tabs = tabs;
	}
}
