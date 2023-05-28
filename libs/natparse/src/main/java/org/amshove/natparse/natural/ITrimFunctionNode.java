package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;

import javax.annotation.Nullable;

public interface ITrimFunctionNode extends ISystemFunctionNode
{
	SyntaxKind systemFunction();

	@Nullable
	SyntaxKind option();
}
