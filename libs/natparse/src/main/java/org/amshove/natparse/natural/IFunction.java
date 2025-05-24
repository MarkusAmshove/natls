package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxToken;

import javax.annotation.Nullable;

public non-sealed interface IFunction extends INaturalModule, IModuleWithBody, IHasDefineData
{
	@Nullable
	IDataType returnType();

	SyntaxToken functionName();
}
