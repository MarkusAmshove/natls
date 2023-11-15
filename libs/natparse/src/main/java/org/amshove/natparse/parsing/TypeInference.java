package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;

import java.util.Optional;

public class TypeInference
{
	private TypeInference()
	{}

	public static Optional<IDataType> inferType(IOperandNode operand)
	{

		if (operand instanceof IVariableReferenceNode variable && variable.reference()instanceof ITypedVariableNode typedRef && typedRef.type() != null)
		{
			return Optional.of(typedRef.type());
		}

		if (operand instanceof ILiteralNode literal)
		{
			return Optional.of(literal.inferType());
		}

		if (operand instanceof IStringConcatOperandNode stringConcat)
		{
			return Optional.of(stringConcat.inferType());
		}

		if (operand instanceof ISystemFunctionNode sysFunction)
		{
			return Optional.of(BuiltInFunctionTable.getDefinition(sysFunction.systemFunction()).type());
		}

		if (operand instanceof ISystemVariableNode sysVar)
		{
			return Optional.of(BuiltInFunctionTable.getDefinition(sysVar.systemVariable()).type());
		}

		if (operand instanceof ISubstringOperandNode substr)
		{
			return inferType(substr.operand());
		}

		return Optional.empty();
	}
}
