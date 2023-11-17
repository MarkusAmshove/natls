package org.amshove.natls.completion;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IHasDefineData;
import org.eclipse.lsp4j.Position;

import javax.annotation.Nullable;

public record CodeCompletionContext(SemanticPosition semanticPosition, @Nullable SyntaxToken currentToken, @Nullable SyntaxToken previousToken)
{
	public static CodeCompletionContext create(LanguageServerFile file, Position position)
	{
		var module = file.module();
		var semanticPosition = SemanticPosition.STATEMENTS;
		if (module instanceof IHasDefineData hasDefineData && hasDefineData.defineData() != null
			&& hasDefineData.defineData().enclosesPosition(position.getLine(), position.getCharacter()))
		{
			semanticPosition = SemanticPosition.DEFINE_DATA;
		}

		var tokens = file.tokens().toList();
		var tokenAtPosition = NodeUtil.findTokenOnOrBeforePosition(tokens, position.getLine(), position.getCharacter());
		var previousToken = NodeUtil.findTokenOnOrBeforePosition(tokens, tokenAtPosition.line(), tokenAtPosition.offsetInLine() - 1);
		if (tokenAtPosition.line() < position.getLine())
		{
			previousToken = tokenAtPosition;
			tokenAtPosition = null;
		}
		return new CodeCompletionContext(semanticPosition, tokenAtPosition, previousToken);
	}

	public boolean completesDataArea()
	{
		return semanticPosition == SemanticPosition.DEFINE_DATA && isCurrentTokenKind(SyntaxKind.USING);
	}

	public boolean isCurrentTokenKind(SyntaxKind kind)
	{
		return currentToken != null && currentToken.kind() == kind;
	}

	public boolean isPreviousTokenKind(SyntaxKind kind)
	{
		return previousToken != null && previousToken.kind() == kind;
	}
}
