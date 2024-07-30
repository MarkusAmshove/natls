package org.amshove.natls.languageserver;

import org.eclipse.lsp4j.TextDocumentIdentifier;

@SuppressWarnings("all")
public class FindConstantsParams
{
	private TextDocumentIdentifier identifier;

	public FindConstantsParams(TextDocumentIdentifier identifier)
	{
		this.identifier = identifier;
	}

	public TextDocumentIdentifier getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(TextDocumentIdentifier identifier)
	{
		this.identifier = identifier;
	}
}
