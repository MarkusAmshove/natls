package org.amshove.natls.codeactions;

import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxNode;
import org.eclipse.lsp4j.Diagnostic;

import java.util.List;

public record CodeActionContext(String fileUri, INaturalModule module, SyntaxToken tokenUnderCursor, ISyntaxNode nodeAtPosition, List<Diagnostic> diagnosticsAtPosition)
{

}
