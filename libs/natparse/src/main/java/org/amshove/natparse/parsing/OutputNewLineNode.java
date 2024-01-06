package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IOutputNewLineNode;

class OutputNewLineNode extends TokenNode implements IOutputNewLineNode
{
	OutputNewLineNode(SyntaxToken token)
	{
		super(token);
	}
}
