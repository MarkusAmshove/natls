package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;
import org.amshove.natparse.natural.builtin.IBuiltinFunctionDefinition;
import org.amshove.natparse.natural.builtin.SystemVariableDefinition;

import java.util.ArrayList;
import java.util.List;

final class TypeChecker implements ISyntaxNodeVisitor
{
	private final List<IDiagnostic> diagnostics = new ArrayList<>();

	public ReadOnlyList<IDiagnostic> check(ISyntaxTree tree)
	{
		tree.accept(this);

		return ReadOnlyList.from(diagnostics);
	}

	@Override
	public void visit(ISyntaxNode node)
	{
		try
		{
			checkNode(node);
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

	private void checkNode(ISyntaxNode node)
	{
		if (node instanceof IMutateVariables mutator)
		{
			ensureMutable(mutator);
		}

		if (node instanceof IDivideStatementNode divide)
		{
			checkDivide(divide);
		}

		if (node instanceof IWriteWorkNode writeWork)
		{
			checkWriteWork(writeWork);
		}

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
	}

	private void checkVariableReference(IVariableReferenceNode variableReference)
	{
		if (!(variableReference.reference()instanceof IVariableNode target))
		{
			return;
		}

		if (variableReference.dimensions().hasItems() && !target.isArray())
		{
			diagnostics.add(
				ParserErrors.invalidArrayAccess(
					variableReference.referencingToken(),
					"Using index access for a reference to non-array %s".formatted(target.name())
				)
			);
		}

		if (variableReference.dimensions().isEmpty() && target.isArray())
		{
			if (!doesNotNeedDimensionInParentStatement(variableReference))
			{
				diagnostics.add(
					ParserErrors.invalidArrayAccess(
						variableReference.referencingToken(),
						"Missing index access, because %s is an array".formatted(target.name())
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

	private boolean doesNotNeedDimensionInParentStatement(IVariableReferenceNode reference)
	{
		var parent = reference.parent();
		if (parent instanceof ISystemFunctionNode systemFunction)
		{
			var theFunction = systemFunction.systemFunction();
			return theFunction == SyntaxKind.OCC || theFunction == SyntaxKind.OCCURRENCE;
		}

		return parent instanceof IExpandArrayNode || parent instanceof IReduceArrayNode || parent instanceof IResizeArrayNode;
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

		report(ParserErrors.referenceNotMutable("Operand is not modifiable by statement", operand));
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
}
