package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;

import java.util.ArrayList;
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

		if (operand instanceof IArithmeticExpressionNode arithmeticExpressionNode)
		{
			return inferArithmeticType(arithmeticExpressionNode);
		}

		return Optional.empty();
	}

	private static Optional<IDataType> inferArithmeticType(IArithmeticExpressionNode arithmetic)
	{
		var collectedTypes = new ArrayList<IDataType>();
		inferType(arithmetic.left()).ifPresent(collectedTypes::add);
		inferType(arithmetic.right()).ifPresent(collectedTypes::add);
		if (collectedTypes.isEmpty())
		{
			return Optional.empty();
		}

		IDataType biggestType = null;
		for (var collectedType : collectedTypes)
		{
			if (biggestType == null || collectedType.byteSize() > biggestType.byteSize())
			{
				biggestType = collectedType;
			}
		}

		return Optional.of(biggestType);
	}
}
