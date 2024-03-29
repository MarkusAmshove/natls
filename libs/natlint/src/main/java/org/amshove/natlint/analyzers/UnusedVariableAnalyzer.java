package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IGroupNode;
import org.amshove.natparse.natural.IRedefinitionNode;
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
		if (UNWANTED_FILETYPES.contains(context.getModule().file().getFiletype()))
		{
			return;
		}

		if (!NodeUtil.moduleContainsNode(context.getModule(), syntaxNode))
		{
			return;
		}

		var variable = (IVariableNode) syntaxNode;
		if (computeReferenceCount(variable) == 0
			&& computeParentReferenceCount(variable) == 0
			&& (!(variable.parent()instanceof IRedefinitionNode redefine) || noMembersAfterAreReferenced(redefine, variable)))
		{
			context.report(UNUSED_VARIABLE.createFormattedDiagnostic(variable.position(), variable.name()));

		}
	}

	private boolean noMembersAfterAreReferenced(IRedefinitionNode redefine, IVariableNode variable)
	{
		var redefineMembers = redefine.variables();
		var memberIndex = redefineMembers.indexOf(variable);
		for (var i = memberIndex + 1; i < redefineMembers.size(); i++)
		{
			if (redefineMembers.get(i).references().size() > 0)
			{
				return false;
			}
		}

		return true;
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
