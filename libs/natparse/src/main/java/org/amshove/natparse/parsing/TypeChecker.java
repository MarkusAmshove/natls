package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;
import org.amshove.natparse.natural.builtin.IBuiltinFunctionDefinition;
import org.amshove.natparse.natural.builtin.SystemVariableDefinition;
import org.amshove.natparse.natural.conditionals.ISpecifiedCriteriaNode;

import java.util.ArrayList;
import java.util.List;

final class TypeChecker implements ISyntaxNodeVisitor
{
	private final List<IDiagnostic> diagnostics = new ArrayList<>();

	public ReadOnlyList<IDiagnostic> check(ISyntaxTree tree)
	{
		tree.acceptNodeVisitor(this);

		return ReadOnlyList.from(diagnostics);
	}

	@Override
	public void visit(ISyntaxNode node)
	{
		try
		{
			if (node instanceof IStatementNode statementNode)
			{
				checkStatement(statementNode);
			}
			else
			{
				checkNode(node);
			}
		}
		catch (Exception e)
		{
			report(
				ParserErrors.internalError(
					"Error while type checking for node: %s. %s"
						.formatted(
							"(%s,%d:%d)".formatted(node.getClass().getSimpleName(), node.position().line(), node.position().offsetInLine()),
							e.getMessage()
						),
					node
				)
			);
		}
	}

	private void checkStatement(IStatementNode statement)
	{
		if (statement instanceof IMutateVariables mutator)
		{
			ensureMutable(mutator);
		}

		if (statement instanceof IAssignmentStatementNode assignment)
		{
			checkAssign(assignment);
			return;
		}

		if (statement instanceof IDivideStatementNode divide)
		{
			checkDivide(divide);
			return;
		}

		if (statement instanceof IWriteWorkNode writeWork)
		{
			checkWriteWork(writeWork);
			return;
		}

		if (statement instanceof IDecideOnNode decideOn)
		{
			checkDecideOnBranches(decideOn);
			return;
		}

		if (statement instanceof ICompressStatementNode compress)
		{
			checkCompress(compress);
		}
	}

	private void checkCompress(ICompressStatementNode compress)
	{
		for (var operand : compress.operands())
		{
			var operandType = inferDataType(operand);
			if (operandType.format() == DataFormat.LOGIC || operandType.format() == DataFormat.CONTROL)
			{
				report(ParserErrors.typeMismatch("COMPRESS operand can't be of type %s".formatted(operandType.format().identifier()), operand));
			}
		}

		var targetType = inferDataType(compress.intoTarget());
		if (targetType.format() != DataFormat.ALPHANUMERIC
			&& targetType.format() != DataFormat.BINARY
			&& targetType.format() != DataFormat.UNICODE)
		{
			report(
				ParserErrors.typeMismatch(
					"COMPRESS target needs to have type A, B or U but got %s".formatted(targetType.toShortString()),
					compress.intoTarget()
				)
			);
		}
	}

	private void checkAssign(IAssignmentStatementNode assignment)
	{
		if (!(assignment.target()instanceof IVariableReferenceNode targetRef
			&& targetRef.reference()instanceof ITypedVariableNode typedTarget
			&& typedTarget.type() != null))
		{
			return;
		}

		if (assignment.operand() instanceof IArithmeticExpressionNode)
		{
			return;
		}

		var targetType = assignment.target()instanceof ITypeInferable inferableTarget
			? inferableTarget.inferType()
			: null;

		var operandType = assignment.operand()instanceof ITypeInferable inferableOperand
			? inferableOperand.inferType()
			: null;

		if (targetType == null || operandType == null || targetType == IDataType.UNTYPED || operandType == IDataType.UNTYPED)
		{
			return;
		}

		if (assignment.operand()instanceof ILiteralNode literal)
		{
			operandType = literal.reInferType(targetType);
		}

		if (assignment.operand()instanceof IVariableReferenceNode refOperand
			&& refOperand.reference()instanceof ITypedVariableNode refVariable
			&& refVariable.type() != null
			&& refVariable.type().isConstant()
			&& refVariable.type().initialValue() != null)

		{
			operandType = inferDataType(refVariable.type().initialValue());
		}

		if (assignment.operand() instanceof ILiteralNode)
		// Only do this for literals
		// #N5 := #N10 is legal compiler wise, but might result in a runtime error
		{
			checkTypeCompatible(operandType, targetType, assignment.operand());
			return;
		}

		checkTypeConvertable(operandType, targetType, assignment.operand());
	}

	private void checkTypeConvertable(IDataType operandType, IDataType targetType, ISyntaxNode location)
	{
		if (!operandType.hasSameFamily(targetType) && !operandType.hasCompatibleFormat(targetType))
		{
			report(
				ParserErrors.typeMismatch(
					"Type mismatch: Inferred type %s is not implicitly convertable to target type %s".formatted(
						operandType.toShortString(),
						targetType.toShortString()
					),
					location
				)
			);
		}
	}

	private void checkTypeCompatible(IDataType operandType, IDataType targetType, ISyntaxNode location)
	{
		if (operandType.fitsInto(targetType))
		{
			return;
		}

		if (!operandType.hasCompatibleFormat(targetType))
		{
			report(
				ParserErrors.typeMismatch(
					"Type mismatch: Inferred type %s is not compatible with target type %s".formatted(
						operandType.toShortString(),
						targetType.toShortString()
					),
					location
				)
			);
		}
	}

	private void checkNode(ISyntaxNode node)
	{
		if (node instanceof ITypedVariableNode typedVariableNode
			&& typedVariableNode.type() != null
			&& typedVariableNode.type().initialValue() != null)
		{
			checkVariableInitType(typedVariableNode);
			return;
		}

		if (node instanceof IVariableReferenceNode variableReference)
		{
			checkVariableReference(variableReference);
			return;
		}

		if (node instanceof ISystemFunctionNode sysFuncNode)
		{
			checkSystemFunctionParameter(sysFuncNode);
			return;
		}

		if (node instanceof IProcessingLoopFunctionNode function)
		{
			checkProcessingLoopFunctions(function);
		}

		if (node instanceof IMathFunctionOperandNode function)
		{
			checkMathematicalSystemFunctions(function);
			return;
		}

		checkAlphaSystemFunctions(node);
	}

	private void checkProcessingLoopFunctions(IProcessingLoopFunctionNode operand)
	{
		var type = inferDataType(operand.parameter());
		if (operand.parameter() instanceof ILiteralNode || type.format() == DataFormat.NONE)
		{
			report(ParserErrors.typeMismatch("Parameter must be a typed variable of any format, but is %s".formatted(type.toShortString()), operand));
		}
	}

	private void checkMathematicalSystemFunctions(IMathFunctionOperandNode operand)
	{
		var type = inferDataType(operand.parameter());
		if (type.format() != DataFormat.NONE && !type.isNumericFamily())
		{
			report(ParserErrors.typeMismatch("Parameter must be of type N, P, I or F, but is %s".formatted(type.toShortString()), operand));
		}
	}

	private boolean checkAlphaSystemFunctions(ISyntaxNode node)
	{
		IDataType type;

		if (node instanceof ISortKeyOperandNode sortKeyNode)
		{
			type = inferDataType(sortKeyNode.variable());
			if (type.format() != DataFormat.ALPHANUMERIC || type.length() > 253 || type.hasDynamicLength())
			{
				report(ParserErrors.typeMismatch("Parameter must be of type A with a maximum length of 253, but is %s".formatted(type.toShortString()), node));
			}
		}

		if (node instanceof IValOperandNode valNode)
		{
			type = inferDataType(valNode.parameter());
			if (valNode.parameter() instanceof ILiteralNode)
			{
				return false;
			}

			if (type.format() != DataFormat.NONE && type.format() != DataFormat.ALPHANUMERIC && type.format() != DataFormat.UNICODE)
			{
				report(ParserErrors.typeMismatch("Parameter must be of type A or U, but is %s".formatted(type.toShortString()), node));
			}
		}

		return true;
	}

	private void checkSystemFunctionParameter(ISystemFunctionNode sysFuncNode)
	{
		if (sysFuncNode.systemFunction() == SyntaxKind.SV_LENGTH)
		{
			for (var parameter : sysFuncNode.parameter())
			{
				var type = inferDataType(parameter);
				if (type == null)
				{
					continue;
				}

				if (type.format() != DataFormat.ALPHANUMERIC && type.format() != DataFormat.UNICODE && type.format() != DataFormat.BINARY)
				{
					report(ParserErrors.typeMismatch("Parameter to *LENGTH must be of type A, B or U but is %s".formatted(type.toShortString()), parameter));
				}

				if (!type.hasDynamicLength())
				{
					report(ParserErrors.typeMismatch("Parameter to *LENGTH must have dynamic length (e.g. A DYNAMIC) but is %s".formatted(type.toShortString()), parameter));
				}
			}
		}

		if (sysFuncNode.systemFunction() == SyntaxKind.TRIM && sysFuncNode.parameter().hasItems())
		{
			var parameter = sysFuncNode.parameter().first();

			var type = inferDataType(parameter);
			if (type != null && type.format() != DataFormat.NONE && type.format() != DataFormat.ALPHANUMERIC && type.format() != DataFormat.UNICODE && type.format() != DataFormat.BINARY)
			{
				report(ParserErrors.typeMismatch("Parameter to *TRIM must be of type A, B or U but is %s".formatted(type.toShortString()), parameter));
			}
		}
	}

	private void checkDecideOnBranches(IDecideOnNode decideOn)
	{
		if (!(decideOn.operand()instanceof IVariableReferenceNode target)
			|| !(target.reference()instanceof ITypedVariableNode typedTarget)
			|| typedTarget.type() == null)
		{
			return;
		}

		for (var branch : decideOn.branches())
		{
			for (var value : branch.values())
			{
				var inferredType = inferDataType(value);
				if (inferredType.format() != DataFormat.NONE && !inferredType.hasCompatibleFormat(typedTarget.type()))
				{
					report(
						ParserErrors.typeMismatch(
							"Inferred format %s is not compatible with %s (%s)".formatted(inferredType.format(), typedTarget.declaration().symbolName(), typedTarget.type().format()),
							value
						)
					);
				}
			}
		}
	}

	private void checkVariableReference(IVariableReferenceNode variableReference)
	{
		if (!(variableReference.reference()instanceof IVariableNode target))
		{
			return;
		}

		if (NodeUtil.findFirstParentOfType(variableReference, IFindNode.class) != null)
		{
			// Can't correctly type check DDM fields yet, because DDM fields don't have their type loaded
			return;
		}

		if (variableReference.dimensions().hasItems() && !target.isArray())
		{
			if (!isPeriodicGroup(target))// periodic groups need to have their index specified. not allowed for "normal" groups
			{
				diagnostics.add(
					ParserErrors.invalidArrayAccess(
						variableReference.referencingToken(),
						"Using index access for a reference to non-array %s".formatted(target.name())
					)
				);
			}
		}

		if (variableReference.dimensions().isEmpty() && (target.isArray() || isPeriodicGroup(target)))
		{
			if (!doesNotNeedDimensionInParentStatement(variableReference))
			{
				var message = isPeriodicGroup(target) ? "a periodic group" : "an array";
				diagnostics.add(
					ParserErrors.invalidArrayAccess(
						variableReference.referencingToken(),
						"Missing index access, because %s is %s".formatted(target.name(), message)
					)
				);
			}
		}

		if (variableReference.dimensions().hasItems() && target.dimensions().hasItems()
			&& variableReference.dimensions().size() != target.dimensions().size())
		{
			diagnostics.add(
				ParserErrors.invalidArrayAccess(
					variableReference.referencingToken(),
					"Missing dimensions in array access. Got %d dimensions but %s has %d".formatted(
						variableReference.dimensions().size(),
						target.name(),
						target.dimensions().size()
					)
				)
			);
		}
	}

	private boolean isPeriodicGroup(IVariableNode variable)
	{
		if (!(variable instanceof IGroupNode group))
		{
			return false;
		}

		if (!group.isInView())
		{
			return false;
		}

		var nextLevel = variable.level() + 1;
		for (var periodicMember : group.variables())
		{
			if (periodicMember.level() == nextLevel && !periodicMember.isArray())
			{
				return false;
			}
		}

		return true;
	}

	private boolean doesNotNeedDimensionInParentStatement(IVariableReferenceNode reference)
	{
		var parent = reference.parent();
		if (parent instanceof ISystemFunctionNode systemFunction)
		{
			var theFunction = systemFunction.systemFunction();
			return theFunction == SyntaxKind.OCC || theFunction == SyntaxKind.OCCURRENCE || theFunction == SyntaxKind.UBOUND || theFunction == SyntaxKind.LBOUND;
		}

		return parent instanceof IExpandArrayNode
			|| parent instanceof IReduceArrayNode
			|| parent instanceof IResizeArrayNode
			|| parent instanceof ISpecifiedCriteriaNode;
	}

	private void checkWriteWork(IWriteWorkNode writeWork)
	{
		if (writeWork.isVariable())
		{
			return;
		}

		for (var operand : writeWork.operands())
		{
			if (operand instanceof IVariableReferenceNode variableReference
				&& variableReference.reference()instanceof ITypedVariableNode typedVariable
				&& typedVariable.type() != null)
			{
				if (typedVariable.type().hasDynamicLength())
				{
					report(ParserErrors.typeMismatch("Can't use operand with dynamic length if WRITE WORK misses the VARIABLE keyword", variableReference));
				}
				else
					if (typedVariable.isArray())
					{
						for (var dimension : variableReference.dimensions())
						{
							if (dimension instanceof IRangedArrayAccessNode ranged && containsDynamicDimension(ranged))
							{
								report(ParserErrors.typeMismatch("Can't use operand with dynamic array access if WRITE WORK misses the VARIABLE keyword", variableReference));
							}
						}
					}
			}
		}
	}

	private void checkVariableInitType(ITypedVariableNode typedVariable)
	{
		var initialNode = typedVariable.type().initialValue();
		if (typedVariable.type().hasDynamicLength() || initialNode == null ||
			!(initialNode instanceof ILiteralNode || initialNode instanceof IStringConcatOperandNode))
		{
			return;
		}

		var inferredInitialType = initialNode instanceof ILiteralNode literal
			? literal.reInferType(typedVariable.type())
			: ((IStringConcatOperandNode) initialNode).inferType();

		if (inferredInitialType.format() == typedVariable.type().format() && !inferredInitialType.fitsInto(typedVariable.type()))
		{
			// This check is special for initializers, because the Natural compiler only treats same types which don't fit as errors.
			// Others are happily truncated ¯\_()_/¯
			var initialSource = initialNode instanceof ILiteralNode literal
				? literal.token().source()
				: "'%s'".formatted(((IStringConcatOperandNode) initialNode).stringValue());
			report(
				ParserErrors.typeMismatch(
					"Type mismatch: Initializer %s (inferred %s) does not fit into %s"
						.formatted(initialSource, inferredInitialType.toShortString(), typedVariable.type().toShortString()),
					initialNode
				)
			);
		}
		else
		{
			checkTypeCompatible(inferredInitialType, typedVariable.type(), initialNode);
		}
	}

	private void ensureMutable(IMutateVariables mutator)
	{
		for (var mutation : mutator.mutations())
		{
			ensureMutable(mutation);
		}
	}

	private void ensureMutable(IOperandNode operand)
	{
		if (operand == null) // unresolved
		{
			return;
		}

		if (operand instanceof IVariableReferenceNode reference)
		{
			ensureMutable(reference);
			return;
		}

		if (operand instanceof ISystemVariableNode sysVar)
		{
			ensureMutable(sysVar, BuiltInFunctionTable.getDefinition(sysVar.systemVariable()));
			return;
		}

		if (operand instanceof ISystemFunctionNode sysFunction)
		{
			ensureMutable(sysFunction, BuiltInFunctionTable.getDefinition(sysFunction.systemFunction()));
			return;
		}

		if (operand instanceof ISubstringOperandNode substring)
		{
			ensureMutable(substring.operand());
			return;
		}

		if (operand instanceof ILiteralNode)
		{
			report(ParserErrors.referenceNotMutable("Operand is not modifiable by statement", operand));
		}
	}

	private void ensureMutable(IVariableReferenceNode variableReference)
	{
		if (variableReference.reference() == null)
		{
			return;
		}

		if (!(variableReference.reference()instanceof ITypedVariableNode typedVariable))
		{
			return;
		}

		if (typedVariable.type() != null && typedVariable.type().isConstant())
		{
			report(ParserErrors.referenceNotMutable("Variable can't be modified because it is CONST", variableReference.referencingToken()));
		}
	}

	private void ensureMutable(ISyntaxNode node, IBuiltinFunctionDefinition entry)
	{
		if (entry == null)
		{
			return;
		}

		if (entry instanceof SystemVariableDefinition sysVarDefinition && !sysVarDefinition.isModifiable())
		{
			report(ParserErrors.referenceNotMutable("Unmodifiable system variables can't be modified", node));
		}
	}

	private void checkDivide(IDivideStatementNode divide)
	{
		if (!divide.hasRemainder())
		{
			return;
		}

		checkThatOperandIsNotSpecifyingArrayRange(divide.target());
		checkThatOperandIsNotSpecifyingArrayRange(divide.giving());
		checkThatOperandIsNotSpecifyingArrayRange(divide.remainder());

		for (var operand : divide.operands())
		{
			checkThatOperandIsNotSpecifyingArrayRange(operand);
		}
	}

	private void checkThatOperandIsNotSpecifyingArrayRange(IOperandNode operandNode)
	{
		if (!(operandNode instanceof IVariableReferenceNode reference))
		{
			return;
		}

		for (var dimension : reference.dimensions())
		{
			if (dimension instanceof IRangedArrayAccessNode)
			{
				report(ParserErrors.typeMismatch("Operand can't specify array range in this context", dimension));
			}
		}
	}

	private void report(IDiagnostic diagnostic)
	{
		diagnostics.add(diagnostic);
	}

	private boolean containsDynamicDimension(IRangedArrayAccessNode ranged)
	{
		if (!(ranged.lowerBound()instanceof ILiteralNode lowerLiteral) || !(ranged.upperBound()instanceof ILiteralNode upperLiteral))
		{
			return true;
		}

		return lowerLiteral != upperLiteral // on e.g. #ARR(*) the ASTERISK token is used for both upper and lower
			&& (lowerLiteral.token().kind() == SyntaxKind.ASTERISK
				|| upperLiteral.token().kind() == SyntaxKind.ASTERISK);
	}

	private IDataType inferDataType(IOperandNode operand)
	{
		if (operand instanceof IVariableReferenceNode variable && variable.reference()instanceof ITypedVariableNode typedRef && typedRef.type() != null)
		{
			return typedRef.type();
		}

		if (operand instanceof ILiteralNode literal)
		{
			return literal.inferType();
		}

		if (operand instanceof IStringConcatOperandNode stringConcat)
		{
			return stringConcat.inferType();
		}

		if (operand instanceof ISystemFunctionNode sysFunction)
		{
			return BuiltInFunctionTable.getDefinition(sysFunction.systemFunction()).type();
		}

		if (operand instanceof ISystemVariableNode sysVar)
		{
			return BuiltInFunctionTable.getDefinition(sysVar.systemVariable()).type();
		}

		if (operand instanceof ISubstringOperandNode substr)
		{
			return inferDataType(substr.operand());
		}

		return new DataType(DataFormat.NONE, IDataType.ONE_GIGABYTE); // couldn't infer, don't raise something yet
	}

}
