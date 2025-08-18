package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IParseJsonStatementNode;

class ParseJsonStatementNode extends StatementWithBodyNode implements IParseJsonStatementNode
{
	private IOperandNode jsonDocument;
	private IOperandNode codePage;
	private IOperandNode jsonPath;
	private IOperandNode jsonPathSeparator;
	private IOperandNode jsonAttributeName;
	private IOperandNode jsonAttributeValue;
	private IOperandNode giving;
	private IOperandNode subcode;

	@Override
	public IOperandNode jsonDocument()
	{
		return jsonDocument;
	}

	@Override
	public IOperandNode codePage()
	{
		return codePage;
	}

	@Override
	public IOperandNode jsonPath()
	{
		return jsonPath;
	}

	@Override
	public IOperandNode jsonPathSeparator()
	{
		return jsonPathSeparator;
	}

	@Override
	public IOperandNode jsonAttributeName()
	{
		return jsonAttributeName;
	}

	@Override
	public IOperandNode jsonAttributeValue()
	{
		return jsonAttributeValue;
	}

	@Override
	public IOperandNode giving()
	{
		return giving;
	}

	@Override
	public IOperandNode subcode()
	{
		return subcode;
	}

	void setJsonDocument(IOperandNode jsonDocument)
	{
		this.jsonDocument = jsonDocument;
	}

	void setCodePage(IOperandNode codePage)
	{
		this.codePage = codePage;
	}

	void setJsonPath(IOperandNode jsonPath)
	{
		this.jsonPath = jsonPath;
	}

	void setJsonPathSeparator(IOperandNode jsonPathSeparator)
	{
		this.jsonPathSeparator = jsonPathSeparator;
	}

	void setJsonAttributeName(IOperandNode jsonAttributeName)
	{
		this.jsonAttributeName = jsonAttributeName;
	}

	void setJsonAttributeValue(IOperandNode jsonAttributeValue)
	{
		this.jsonAttributeValue = jsonAttributeValue;
	}

	void setGiving(IOperandNode giving)
	{
		this.giving = giving;
	}

	void setSubcode(IOperandNode subcode)
	{
		this.subcode = subcode;
	}
}
