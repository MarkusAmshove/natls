package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IGroupNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IVariableNode;

public class UnusedVariableAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription UNUSED_VARIABLE = DiagnosticDescription.create(
		"NL001",
		"Variable is unused",
		DiagnosticSeverity.WARNING
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(UNUSED_VARIABLE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IVariableNode.class, this::analyzeVariable);
	}

	private void analyzeVariable(ISyntaxNode syntaxNode, IAnalyzeContext context)
	{
		var variable = (IVariableNode) syntaxNode;
		if (computeReferenceCount(variable) == 0)
		{
			context.report(UNUSED_VARIABLE.createDiagnostic(variable.position()));
		}
	}

	private static int computeReferenceCount(IVariableNode variable)
	{
		if (variable instanceof IGroupNode groupNode)
		{
			return variable.references().size() + groupNode.variables().stream().mapToInt(UnusedVariableAnalyzer::computeReferenceCount).sum();
		}

		return variable.references().size();
	}
}
