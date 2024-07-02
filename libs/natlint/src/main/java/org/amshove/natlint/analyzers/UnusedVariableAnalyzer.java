package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.*;
import org.amshove.natparse.AdditionalDiagnosticInfo;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.*;

public class UnusedVariableAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription UNUSED_VARIABLE = DiagnosticDescription.create(
		"NL001",
		"Variable %s is unused",
		DiagnosticSeverity.WARNING
	);
	public static final DiagnosticDescription VARIABLE_MODIFIED_ONLY = DiagnosticDescription.create(
		"NL101",
		"Variable %s is modified but never accessed",
		DiagnosticSeverity.INFO
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
		var references = computeReferenceCount(variable);
		if (references == 0
			&& computeParentReferenceCount(variable) == 0
			&& (!(variable.parent()instanceof IRedefinitionNode redefine) || noMembersAfterAreReferenced(redefine, variable)))
		{
			context.report(UNUSED_VARIABLE.createFormattedDiagnostic(variable.position(), variable.name()));

		}

		checkIfVariableIsMutatedOnly(variable, context);
	}

	private void checkIfVariableIsMutatedOnly(IVariableNode variable, IAnalyzeContext context)
	{
		if (variable.references().size() == 0)
		{
			return;
		}

		if (variable instanceof IGroupNode)
		{
			return;
		}

		if (!variable.scope().isLocal())
		{
			return;
		}

		if (variable.isInView())
		{
			return;
		}

		if (variable.level() > 1)
		{
			// Just assume that any parent group that is referenced is read
			for (var parent : variable.getVariableParentsAscending())
			{
				if (parent.references().size() > 0)
				{
					return;
				}
			}

			if (NodeUtil.findFirstParentOfType(variable, IRedefinitionNode.class)instanceof IRedefinitionNode redefine && redefine.reference().references().size() > 0)
			{
				return;
			}
		}

		for (var reference : variable.references())
		{
			// There are too many edge cases where a variable can be read that we can't grasp correctly.
			// We only look for common cases where you remove reads to a variable but forget writes, like RESET,
			// which would then prevent you from knowing that you can clean up the variable.

			var isReset = NodeUtil.findFirstParentOfType(reference, IResetStatementNode.class) != null;
			var isAssigned = NodeUtil.findFirstParentOfType(reference, IAssignmentStatementNode.class)instanceof IAssignmentStatementNode assignment && assignment.target() == reference
				|| NodeUtil.findFirstParentOfType(reference, IAssignStatementNode.class)instanceof IAssignmentStatementNode assign && assign.target() == reference;

			if (!isAssigned && !isReset)
			{
				return;
			}
		}

		var diagnostic = VARIABLE_MODIFIED_ONLY.createFormattedDiagnostic(variable.position(), variable.name());
		for (var reference : variable.references())
		{
			diagnostic.addAdditionalInfo(new AdditionalDiagnosticInfo("Modified here", reference.diagnosticPosition()));
		}

		context.report(diagnostic);
	}

	private boolean noMembersAfterAreReferenced(IRedefinitionNode redefine, IVariableNode variable)
	{
		var redefineMembers = redefine.variables();
		var memberIndex = redefineMembers.indexOf(variable);
		for (var i = memberIndex + 1; i < redefineMembers.size(); i++)
		{
			if (!redefineMembers.get(i).references().isEmpty())
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
