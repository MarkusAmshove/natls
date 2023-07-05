package org.amshove.natls.codeactions;

import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.quickfixes.CodeActionBuilder;
import org.amshove.natparse.natural.IGroupNode;
import org.amshove.natparse.natural.ITypedVariableNode;
import org.amshove.natparse.natural.IVariableReferenceNode;
import org.amshove.natparse.natural.VariableScope;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

import java.util.List;

public class DumpGroupVariableAction implements ICodeActionProvider
{
	@Override
	public boolean isApplicable(RefactoringContext context)
	{
		return context.file().getType().canHaveBody()
			&& (context.nodeAtPosition() instanceof IGroupNode
				|| context.nodeAtPosition()instanceof IVariableReferenceNode varRef && varRef.reference() instanceof IGroupNode);
	}

	private static IGroupNode getGroupNode(RefactoringContext context)
	{
		if (context.nodeAtPosition()instanceof IGroupNode grp)
		{
			return grp;
		}

		if (context.nodeAtPosition()instanceof IVariableReferenceNode varRef
			&& varRef.reference()instanceof IGroupNode grp)
		{
			return grp;
		}

		throw new IllegalStateException("Can't find group variable to refactor");
	}

	@Override
	public List<CodeAction> createCodeAction(RefactoringContext context)
	{
		var group = getGroupNode(context);
		var assignments = new StringBuilder();
		var dumpVariable = "#%s-DUMP".formatted(group.name());
		assignments.append("RESET %s%n%n".formatted(dumpVariable));
		for (var variable : group.variables())
		{
			if (!(variable instanceof ITypedVariableNode typedVar))
			{
				continue;
			}

			if (typedVar.type().format().isNumericFamily())
			{
				assignments.append(
					"COMPRESS NUMERIC %s '%s := ' %s H'0A' INTO %s LEAVING SPACE%n".formatted(
						dumpVariable,
						typedVar.qualifiedName(),
						typedVar.qualifiedName(),
						dumpVariable
					)
				);
			}
			else
			{
				assignments.append(
					"COMPRESS %s '%s := ''' %s '''' H'0A' INTO %s LEAVING NO SPACE%n".formatted(
						dumpVariable,
						typedVar.qualifiedName(),
						typedVar.qualifiedName(),
						dumpVariable
					)
				);
			}
		}

		return List.of(
			new CodeActionBuilder("Generate group dump subroutine", CodeActionKind.RefactorExtract)
				.appliesWorkspaceEdit(
					new WorkspaceEditBuilder()
						.addsVariable(context.file(), dumpVariable, "(A) DYNAMIC", VariableScope.LOCAL)
						.addsSubroutine(context.file(), "DUMP-%s".formatted(group.name()), assignments.toString())
				).build()
		);
	}
}
