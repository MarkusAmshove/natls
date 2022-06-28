package org.amshove.natls.quickfixes;

import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natparse.parsing.ParserError;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

import java.util.Arrays;
import java.util.List;

public class AmbiguousReferenceQuickFix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerMultipleQuickFixes(ParserError.AMBIGUOUS_VARIABLE_REFERENCE.id(), this::fixAmbiguousReference);
	}

	private List<CodeAction> fixAmbiguousReference(QuickFixContext quickFixContext)
	{
		var diagnostic = quickFixContext.diagnostic();
		var messageParts = diagnostic.getMessage().split(":");
		var suggestionPart = messageParts[1];
		var suggestions = extractSuggestions(suggestionPart);

		return suggestions.stream()
			.map(suggestion -> new CodeActionBuilder("Use %s".formatted(suggestion), CodeActionKind.QuickFix)
				.fixesDiagnostic(diagnostic)
				.appliesWorkspaceEdit(new WorkspaceEditBuilder()
					.changesText(quickFixContext.fileUri(), diagnostic.getRange(), suggestion))
				.build())
			.toList();
	}

	private static List<String> extractSuggestions(String suggestionPart)
	{
		return Arrays.stream(suggestionPart.trim().split(" ")).toList();
	}
}
