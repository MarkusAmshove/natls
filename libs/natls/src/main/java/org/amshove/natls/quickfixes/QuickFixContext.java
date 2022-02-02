package org.amshove.natls.quickfixes;

import org.amshove.natls.codeactions.CodeActionContext;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;
import org.eclipse.lsp4j.Diagnostic;

public record QuickFixContext(String fileUri, INaturalModule module, SyntaxToken tokenUnderCursor, ISyntaxNode nodeAtPosition, Diagnostic diagnostic)
{
	public static QuickFixContext fromCodeActionContext(CodeActionContext context, Diagnostic diagnostic)
	{
		return new QuickFixContext(
			context.fileUri(),
			context.module(),
			context.tokenUnderCursor(),
			context.nodeAtPosition(),
			diagnostic
		);
	}
}
