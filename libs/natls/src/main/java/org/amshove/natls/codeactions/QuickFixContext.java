package org.amshove.natls.codeactions;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;
import org.eclipse.lsp4j.Diagnostic;

public record QuickFixContext(String fileUri, INaturalModule module, LanguageServerFile file, SyntaxToken tokenUnderCursor, ISyntaxNode nodeAtPosition, Diagnostic diagnostic)
{
	public static QuickFixContext fromCodeActionContext(RefactoringContext context, Diagnostic diagnostic)
	{
		return new QuickFixContext(
			context.fileUri(),
			context.module(),
			context.file(),
			context.tokenUnderCursor(),
			context.nodeAtStartPosition(),
			diagnostic
		);
	}
}
