package org.amshove.natls.completion;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IHasDefineData;
import org.eclipse.lsp4j.Position;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public record CodeCompletionContext(
	SemanticPosition semanticPosition,
	@Nullable SyntaxToken currentToken,
	@Nullable SyntaxToken previousToken,
	List<String> previousTexts,
	Position originalPosition
)
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

		var previousTexts = new ArrayList<String>();
		addTokenIfNotNull(previousToken, previousTexts);
		addTokenIfNotNull(tokenAtPosition, previousTexts);

		return new CodeCompletionContext(semanticPosition, tokenAtPosition, previousToken, previousTexts, position);
	}

	private static void addTokenIfNotNull(SyntaxToken token, ArrayList<String> sources)
	{
		if (token != null)
		{
			sources.add(token.source().toUpperCase());
		}
	}

	public boolean completesDataArea()
	{
		return semanticPosition == SemanticPosition.DEFINE_DATA && (isCurrentTokenKind(SyntaxKind.USING) || isPreviousTokenKind(SyntaxKind.USING));
	}

	public boolean completesPerform()
	{
		return isCurrentTokenKind(SyntaxKind.PERFORM) || isPreviousTokenKind(SyntaxKind.PERFORM);
	}

	public boolean completesCallnat()
	{
		return isCurrentTokenKind(SyntaxKind.CALLNAT) || isPreviousTokenKind(SyntaxKind.CALLNAT);
	}

	public boolean isCurrentTokenKind(SyntaxKind kind)
	{
		return currentToken != null && currentToken.kind() == kind;
	}

	public boolean isPreviousTokenKind(SyntaxKind kind)
	{
		return previousToken != null && previousToken.kind() == kind;
	}

	/**
	 * Checks if the position of the current token is really within the cursor position.<br/>
	 * If it is e.g. `PERFORM SUB ${}$` then this will return false.
	 */
	public boolean cursorIsExactlyOnCurrentToken()
	{
		if (currentToken == null)
		{
			return false;
		}

		return originalPosition.getCharacter() >= currentToken.offsetInLine() && originalPosition.getCharacter() <= currentToken.endOffset();
	}

	public boolean completesParameter()
	{
		if (!completesPerform() && !completesCallnat())
		{
			return false;
		}

		// the current token can't be the call indication keyword, because the identifier of the called modules has to be present.
		// CALLNAT 'MOD' ${}$
		// PERFORM SUB ${}$
		// want to complete parameter, but
		// CALLNAT ${}$
		// PERFORM ${}
		// don't.
		return !isCurrentTokenKind(SyntaxKind.PERFORM) && !isCurrentTokenKind(SyntaxKind.CALLNAT) && !cursorIsExactlyOnCurrentToken();
	}
}
