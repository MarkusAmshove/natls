package org.amshove.natparse.natural;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;

public interface ISystemFunctionNode extends IOperandNode, ITypeInferable
{
	SyntaxKind systemFunction();

	ReadOnlyList<IOperandNode> parameter();

	@Override
	default IDataType inferType()
	{
		return BuiltInFunctionTable.getDefinition(systemFunction()).type();
	}
}
