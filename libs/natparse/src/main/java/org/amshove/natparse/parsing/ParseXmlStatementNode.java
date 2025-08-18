package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IParseXmlStatementNode;

class ParseXmlStatementNode extends StatementWithBodyNode implements IParseXmlStatementNode
{
	private IOperandNode xmlDocument;
	private IOperandNode xmlElementPath;
	private IOperandNode xmlElementName;
	private IOperandNode xmlElementValue;
	private IOperandNode xmlNamespace;
	private IOperandNode xmlPrefix;

	@Override
	public IOperandNode xmlDocument()
	{
		return xmlDocument;
	}

	@Override
	public IOperandNode xmlElementPath()
	{
		return xmlElementPath;
	}

	@Override
	public IOperandNode xmlElementName()
	{
		return xmlElementName;
	}

	@Override
	public IOperandNode xmlElementValue()
	{
		return xmlElementValue;
	}

	@Override
	public IOperandNode xmlNamespace()
	{
		return xmlNamespace;
	}

	@Override
	public IOperandNode xmlPrefix()
	{
		return xmlPrefix;
	}

	void setXmlDocument(IOperandNode xmlDocument)
	{
		this.xmlDocument = xmlDocument;
	}

	void setXmlElementPath(IOperandNode xmlElementPath)
	{
		this.xmlElementPath = xmlElementPath;
	}

	void setXmlElementName(IOperandNode xmlElementName)
	{
		this.xmlElementName = xmlElementName;
	}

	void setXmlElementValue(IOperandNode xmlElementValue)
	{
		this.xmlElementValue = xmlElementValue;
	}

	void setXmlNamespace(IOperandNode xmlNamespace)
	{
		this.xmlNamespace = xmlNamespace;
	}

	void setXmlPrefix(IOperandNode xmlPrefix)
	{
		this.xmlPrefix = xmlPrefix;
	}
}
