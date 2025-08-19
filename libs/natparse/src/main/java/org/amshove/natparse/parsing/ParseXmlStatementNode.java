package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.IParseXmlStatementNode;
import org.jspecify.annotations.Nullable;

class ParseXmlStatementNode extends StatementWithBodyNode implements IParseXmlStatementNode, ILabelIdentifierSettable
{
	private IOperandNode xmlDocument;
	private IOperandNode xmlElementPath;
	private IOperandNode xmlElementName;
	private IOperandNode xmlElementValue;
	private IOperandNode xmlNamespace;
	private IOperandNode xmlPrefix;
	private SyntaxToken labelIdentifier;

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

	@Override
	public ReadOnlyList<IOperandNode> mutations()
	{
		return ReadOnlyList.ofExcludingNull(xmlDocument, xmlElementPath, xmlElementName, xmlElementValue, xmlNamespace, xmlPrefix);
	}

	@Override
	public @Nullable SyntaxToken labelIdentifier()
	{
		return labelIdentifier;
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

	@Override
	public void setLabelIdentifier(SyntaxToken labelIdentifier)
	{
		this.labelIdentifier = labelIdentifier;
	}
}
