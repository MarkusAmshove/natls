package org.amshove.natls.codeactions;

import org.amshove.natls.languageserver.NaturalLanguageService;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.IStatementNode;
import org.amshove.natparse.natural.ISyntaxNode;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;

import java.util.List;

public record RefactoringContext(
	String fileUri,
	INaturalModule module,
	LanguageServerFile file,
	NaturalLanguageService service, // TODO: Remove again?
	SyntaxToken tokenUnderCursor,
	Range originalRange,
	ISyntaxNode nodeAtStartPosition,
	ISyntaxNode nodeAtEndPosition,
	IStatementNode statementAtPosition,
	List<Diagnostic> diagnosticsAtPosition
)
{
	public boolean isMultiSelect()
	{
		return nodeAtStartPosition != nodeAtEndPosition || !originalRange.getStart().equals(originalRange.getEnd());
	}
}
