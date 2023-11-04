package org.amshove.natls.completion;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.IHasDefineData;
import org.eclipse.lsp4j.Position;

public record CodeCompletionContext(SemanticPosition semanticPosition, SyntaxToken previousToken)
{
	public static CodeCompletionContext create(LanguageServerFile file, Position position)
	{
		var module = file.module();
		var semanticPosition = SemanticPosition.STATEMENTS;
		if (module instanceof IHasDefineData hasDefineData && hasDefineData.defineData() != null)
		{
			if (hasDefineData.defineData().enclosesPosition(position.getLine(), position.getCharacter()))
			{
				semanticPosition = SemanticPosition.DEFINE_DATA;
			}
		}

		var tokenAtPosition = NodeUtil.findTokenOnOrBeforePosition(file.tokens().toList(), position.getLine(), position.getCharacter());
		return new CodeCompletionContext(semanticPosition, tokenAtPosition);

	}

	public boolean completesDataArea()
	{
		return semanticPosition == SemanticPosition.DEFINE_DATA && previousToken.kind() == SyntaxKind.USING;
	}
}
