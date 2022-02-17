package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ddm.IDdmField;

import javax.annotation.Nullable;

public interface IDefineData extends ISyntaxNode
{
	ReadOnlyList<IUsingNode> usings();
	ReadOnlyList<IUsingNode> localUsings();
	ReadOnlyList<IUsingNode> parameterUsings();
	ReadOnlyList<IUsingNode> globalUsings();

	ReadOnlyList<IVariableNode> variables();

	@Nullable IVariableNode findVariable(String symbolName);
	@Nullable IDdmField findDdmField(String symbolName);
}
