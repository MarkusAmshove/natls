package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IOperandAttributeNode;
import org.amshove.natparse.natural.IOperandNode;

class OperandAttributeNode extends BaseSyntaxNode implements IOperandAttributeNode
{
	private final SyntaxKind kind;
	private IOperandNode operand;

	OperandAttributeNode(SyntaxToken token)
	{
		addNode(new TokenNode(token));
		var splitByEqual = token.source().split("=");
		kind = SyntaxKind.valueOf(splitByEqual[0].toUpperCase());
	}

	@Override
	public SyntaxKind kind()
	{
		return kind;
	}

	@Override
	public IOperandNode operand()
	{
		return operand;
	}

	void setOperand(IOperandNode operand)
	{
		this.operand = operand;
	}
}
