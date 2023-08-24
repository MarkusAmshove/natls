package org.amshove.natls.quickfixes;

import org.amshove.natlint.analyzers.BooleanOperatorAnalyzer;
import org.amshove.natls.WorkspaceEditBuilder;
import org.amshove.natls.codeactions.AbstractQuickFix;
import org.amshove.natls.codeactions.QuickFixContext;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natparse.ReadOnlyList;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;

import java.util.HashMap;
import java.util.Map;

public class BooleanOperatorQuickfix extends AbstractQuickFix
{
	private static final Map<String, String> OPERATOR_TRANSLATION = new HashMap<>();

	public BooleanOperatorQuickfix()
	{
		OPERATOR_TRANSLATION.put("<", "LT");
		OPERATOR_TRANSLATION.put("LT", "<");
		OPERATOR_TRANSLATION.put("<=", "LE");
		OPERATOR_TRANSLATION.put("LE", "<=");
		OPERATOR_TRANSLATION.put(">", "GT");
		OPERATOR_TRANSLATION.put("GT", ">");
		OPERATOR_TRANSLATION.put(">=", "GE");
		OPERATOR_TRANSLATION.put("GE", ">=");
		OPERATOR_TRANSLATION.put("<>", "NE");
		OPERATOR_TRANSLATION.put("NE", "<>");
		OPERATOR_TRANSLATION.put("=", "EQ");
		OPERATOR_TRANSLATION.put("EQ", "=");
	}

	@Override
	protected void registerQuickfixes()
	{
		registerQuickFix(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR, this::fixOperator);
		registerQuickFix(BooleanOperatorAnalyzer.INVALID_NATUNIT_COMPARISON_OPERATOR, this::fixNatUnitComparison);
		registerFixAll(BooleanOperatorAnalyzer.DISCOURAGED_BOOLEAN_OPERATOR.getId(), this::fixAllOperators);
	}

	private CodeAction fixAllOperators(LanguageServerFile file, ReadOnlyList<Diagnostic> diagnostics)
	{
		var editBuilder = new WorkspaceEditBuilder();

		for (var diagnostic : diagnostics)
		{
			var message = diagnostic.getMessage();
			var discouragedOperator = message.split(" ")[1];

			var preferredOperator = OPERATOR_TRANSLATION.get(discouragedOperator);
			editBuilder.changesText(file.getUri(), diagnostic.getRange(), preferredOperator);
		}

		return new CodeActionBuilder("Fix all operators in %s".formatted(file.getReferableName()), CodeActionKind.QuickFix)
			.appliesWorkspaceEdit(editBuilder)
			.build();
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

		var preferredOperator = OPERATOR_TRANSLATION.get(discouragedOperator);

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
