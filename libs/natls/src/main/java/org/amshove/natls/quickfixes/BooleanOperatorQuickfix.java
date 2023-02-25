package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.BooleanOperatorAnalyzer;
import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natparse.lexing.SyntaxKind;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;

public class BooleanOperatorQuickfix extends AbstractQuickFix
{
	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR, this::fixOperator);
		registerQuickFix(BooleanOperatorAnalyzer.INVALID_NATUNIT_COMPARISON_OPERATOR, this::fixNatUnitComparison);
	}

	private CodeAction fixNatUnitComparison(QuickFixContext quickFixContext)
	{
		var diagnostic = quickFixContext.diagnostic();
		return new CodeActionBuilder("Change operator to EQ", CodeActionKind.QuickFix)
			.fixesDiagnostic(diagnostic)
			.appliesWorkspaceEdit(
				new WorkspaceEditBuilder()
					.changesText(quickFixContext.fileUri(), diagnostic.getRange(), "EQ")
			)
			.build();
	}

	private CodeAction fixOperator(QuickFixContext quickFixContext)
	{
		var diagnostic = quickFixContext.diagnostic();

		var message = diagnostic.getMessage();
		var discouragedOperator = message.split(" ")[1];

		var preferredOperator = BooleanOperatorAnalyzer.PREFERRED_OPERATOR_SIGNS.get(SyntaxKind.valueOf(discouragedOperator));

		return new CodeActionBuilder(
			"Change operator to %s".formatted(preferredOperator),
			CodeActionKind.QuickFix
		)
			.appliesWorkspaceEdit(
				new WorkspaceEditBuilder()
					.changesText(quickFixContext.fileUri(), quickFixContext.diagnostic().getRange(), preferredOperator)
			)
			.fixesDiagnostic(diagnostic)
			.build();
	}
}
