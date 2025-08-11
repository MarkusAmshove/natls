package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;
import org.jspecify.annotations.Nullable;

public interface ITrimFunctionNode extends ISystemFunctionNode
{
	SyntaxKind systemFunction();

	@Nullable
	SyntaxKind option();
}
