package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IConstantMaskOperandNode;
import org.amshove.natparse.natural.IOperandNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ConstantMaskOperandNode extends BaseSyntaxNode implements IConstantMaskOperandNode
{
	private final List<SyntaxToken> contents = new ArrayList<>();
	private IOperandNode checkedOperand;

	@Override
	public ReadOnlyList<SyntaxToken> maskContents()
	{
		return ReadOnlyList.from(contents);
	}

	@Override
	public Optional<IOperandNode> checkedOperand()
	{
		return Optional.ofNullable(checkedOperand);
	}

	void addContent(SyntaxToken token)
	{
		contents.add(token);
	}

	void setCheckedOperand(IOperandNode op)
	{
		checkedOperand = op;
	}
}
