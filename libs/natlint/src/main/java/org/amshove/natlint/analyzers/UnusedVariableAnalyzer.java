package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IGroupNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IVariableNode;

public class UnusedVariableAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription UNUSED_VARIABLE = DiagnosticDescription.create(
		"NL001",
		"Variable %s is unused",
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
		switch (context.getModule().file().getFiletype())
		{
			case LDA:
			case DDM:
			case PDA:
			case GDA:
			case MAP:
			case COPYCODE:
				return;
		}

		if (!NodeUtil.moduleContainsNode(context.getModule(), syntaxNode))
		{
			return;
		}

		var variable = (IVariableNode) syntaxNode;
		if (computeReferenceCount(variable) == 0 && computeParentReferenceCount(variable) == 0)
		{
			context.report(UNUSED_VARIABLE.createFormattedDiagnostic(variable.position(), variable.name()));
		}
	}

	private static int computeParentReferenceCount(IVariableNode variable)
	{
		if (variable.level() == 1)
		{
			return variable.references().size();
		}

		var level = variable.level();
		var references = 0;
		while (level > 1)
		{
			var parent = variable.parent();
			if (!(parent instanceof IVariableNode parentVariable))
			{
				break;
			}

			level = parentVariable.level();
			references += parentVariable.references().size();
			variable = parentVariable;
		}

		return references;
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
