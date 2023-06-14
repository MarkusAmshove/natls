package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.builtin.*;
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

		if (targetType == null || operandType == null)
		{
			return;
		}

		if (assignment.operand()instanceof ILiteralNode literal)
		{
			operandType = literal.reInferType(targetType);
		}

		if (!operandType.fitsInto(targetType))
		{
			report(
				ParserErrors.typeMismatch(
					"Type mismatch: Inferred type %s is not compatible with target type %s".formatted(
						operandType.toShortString(),
						targetType.toShortString()
					),
					assignment.operand()
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
			checkAlphanumericInitLength(typedVariableNode);
		}

		if (node instanceof IVariableReferenceNode variableReference)
		{
			checkVariableReference(variableReference);
		}

		if (node instanceof ISystemFunctionNode sysFuncNode)
		{
			checkSystemFunctionParameter(sysFuncNode);
		}
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
				if (inferredType.format() != DataFormat.NONE && !inferredType.hasSameFamily(typedTarget.type()))
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

	private void checkAlphanumericInitLength(ITypedVariableNode typedVariable)
	{
		if (typedVariable.type().hasDynamicLength())
		{
			return;
		}

		if (typedVariable.type().format() == DataFormat.ALPHANUMERIC
			&& typedVariable.type().initialValue().kind() == SyntaxKind.STRING_LITERAL
			&& typedVariable.type().initialValue().stringValue().length() > typedVariable.type().length()) // TODO: The initializer has to be a IOperandNode
		{
			report(
				ParserErrors.typeMismatch(
					"Initializer literal length %d is longer than data type length %d"
						.formatted(typedVariable.type().initialValue().stringValue().length(), (int) typedVariable.type().length()),
					typedVariable.identifierNode()
				)
			);
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
		if (operand instanceof IVariableReferenceNode variable && variable.reference()instanceof ITypedVariableNode typedRef)
		{
			return typedRef.type();
		}

		if (operand instanceof ILiteralNode literal)
		{
			return literal.inferType();
		}

		if (operand instanceof ISystemFunctionNode sysFunction)
		{
			return BuiltInFunctionTable.getDefinition(sysFunction.systemFunction()).type();
		}

		if (operand instanceof ISystemVariableNode sysVar)
		{
			return BuiltInFunctionTable.getDefinition(sysVar.systemVariable()).type();
		}

		return new DataType(DataFormat.NONE, IDataType.ONE_GIGABYTE); // couldn't infer, don't raise something yet
	}

}
