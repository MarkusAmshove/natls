package org.amshove.natparse.natural;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;

public interface ISystemVariableNode extends ITokenNode, IOperandNode, ITypeInferable
{
	SyntaxKind systemVariable();

	@Override
	default IDataType inferType()
	{
		return BuiltInFunctionTable.getDefinition(systemVariable()).type();
	}
}
