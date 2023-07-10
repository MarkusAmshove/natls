package org.amshove.natls.quickfixes;

import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.natural.IInternalPerformNode;
import org.amshove.natparse.parsing.ParserError;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

public class CreateUnresolvedSubroutineQuickFix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(ParserError.UNRESOLVED_IMPORT.id(), this::createUnresolvedSubroutine);
	}

	private CodeAction createUnresolvedSubroutine(QuickFixContext context)
	{
		if (!context.file().getType().canHaveBody())
		{
			return null;
		}

		var perform = NodeUtil.findNodeOfTypeUpwards(context.nodeAtPosition(), IInternalPerformNode.class);

		if (perform == null)
		{
			return null;
		}

		return new CodeActionBuilder("Declare inline subroutine %s".formatted(perform.referencingToken().symbolName()), CodeActionKind.QuickFix)
			.fixesDiagnostic(context.diagnostic())
			.appliesWorkspaceEdit(
				new WorkspaceEditBuilder()
					.addsSubroutine(context.file(), perform.referencingToken().symbolName(), "IGNORE")
			)
			.build();
	}
}
