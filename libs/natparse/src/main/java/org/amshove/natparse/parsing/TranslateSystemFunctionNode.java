package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.IOperandNode;
import org.amshove.natparse.natural.ITranslateSystemFunctionNode;
import org.amshove.natparse.natural.IVariableReferenceNode;

class TranslateSystemFunctionNode extends BaseSyntaxNode implements ITranslateSystemFunctionNode
{
	private IVariableReferenceNode toTranslate;
	private boolean isToUpper;

	@Override
	public SyntaxKind systemFunction()
	{
		return SyntaxKind.TRANSLATE;
	}

	@Override
	public ReadOnlyList<IOperandNode> parameter()
	{
		return ReadOnlyList.of(toTranslate);
	}

	@Override
	public IVariableReferenceNode toTranslate()
	{
		return toTranslate;
	}

	@Override
	public boolean isToUpper()
	{
		return isToUpper;
	}

	void setToUpper(boolean toUpper)
	{
		isToUpper = toUpper;
	}

	void setToTranslate(IVariableReferenceNode toTranslate)
	{
		this.toTranslate = toTranslate;
	}
}
