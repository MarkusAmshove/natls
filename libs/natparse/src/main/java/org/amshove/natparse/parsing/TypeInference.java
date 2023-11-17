package org.amshove.natparse.parsing;

import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.lexing.SyntaxToken;
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

		if (operand instanceof IFunctionCallNode functionCall && functionCall.reference()instanceof IFunction function)
		{
			return Optional.ofNullable(function.returnType());
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

	public static Optional<IDataType> inferTypeForTokenInStatement(SyntaxToken token, IStatementNode statement)
	{
		if (token == null || statement == null)
		{
			return Optional.empty();
		}

		var tokenNode = NodeUtil.findTokenNodeForToken(token, statement);
		if (tokenNode == null)
		{
			return Optional.empty();
		}

		if (statement instanceof IAssignmentStatementNode assign && assign.target() == tokenNode)
		{
			return inferType(assign.operand());
		}

		if (statement instanceof IBasicMathStatementNode mathStatement
			&& (!(mathStatement instanceof IDivideStatementNode divide) || divide.isRounded())
			&& mathStatement.target() == tokenNode)
		{
			return Optional.of(new DataType(DataFormat.INTEGER, 4));
		}

		if (statement instanceof IDivideStatementNode divide && divide.target() == tokenNode)
		{
			return Optional.of(new DataType(DataFormat.FLOAT, 8));
		}

		if (statement instanceof IForLoopNode forLoop && forLoop.loopControl() == tokenNode)
		{
			return Optional.of(new DataType(DataFormat.INTEGER, 4));
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

			if (!biggestType.isFloating() && collectedType.isFloating())
			{
				biggestType = collectedType;
			}
		}

		return Optional.ofNullable(biggestType);
	}
}
