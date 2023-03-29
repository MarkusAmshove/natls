package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.builtin.BuiltInFunctionTable;
import org.amshove.natparse.natural.builtin.SystemVariableDefinition;

import java.util.ArrayList;
import java.util.List;

class TypeChecker implements ISyntaxNodeVisitor
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
		if (node instanceof IAssignmentStatementNode assignStatement)
		{
			var target = assignStatement.target();
			if (target == null)
			{
				return;
			}
			if (target instanceof IVariableReferenceNode variableReferenceNode)
			{
				ensureMutable(variableReferenceNode);
			}
		}
	}

	private void ensureMutable(IMutateVariables mutator)
	{
		for (var mutation : mutator.mutations())
		{
			if (mutation == null) // unresolved
			{
				continue;
			}

			if (mutation instanceof IVariableReferenceNode reference)
			{
				ensureMutable(reference);
				continue;
			}

			if (mutation instanceof ISystemVariableNode sysVar)
			{
				ensureMutable(sysVar);
			}
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
			report(ParserErrors.internal("Not mutable variable", variableReference.referencingToken()));
		}
	}

	private void ensureMutable(ISystemVariableNode sysVar)
	{
		var entry = BuiltInFunctionTable.getDefinition(sysVar.systemVariable());
		if (entry == null)
		{
			return;
		}

		if (entry instanceof SystemVariableDefinition sysVarDefinition && !sysVarDefinition.isModifiable())
		{
			report(ParserErrors.internal("Not mutable sys var", sysVar.token()));
		}
	}

	private void report(IDiagnostic diagnostic)
	{
		diagnostics.add(diagnostic);
	}
}
