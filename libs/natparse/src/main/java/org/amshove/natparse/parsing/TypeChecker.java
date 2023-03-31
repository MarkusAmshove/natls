package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
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
		if (node instanceof IMutateVariables mutator)
		{
			ensureMutable(mutator);
		}

		if (node instanceof IDivideStatementNode divide)
		{
			checkDivide(divide);
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
}
