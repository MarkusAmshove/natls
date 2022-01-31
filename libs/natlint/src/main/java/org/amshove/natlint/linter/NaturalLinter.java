package org.amshove.natlint.linter;

import org.amshove.natlint.api.LinterDiagnostic;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.ISyntaxTree;

import java.util.ArrayList;

public class NaturalLinter
{
	public ReadOnlyList<LinterDiagnostic> lint(INaturalModule module)
	{
		var linterContext = LinterContext.INSTANCE;
		var diagnostics = new ArrayList<LinterDiagnostic>();
		var analyzeContext = new AnalyzeContext(module, diagnostics::add);

		linterContext.beforeAnalyzing(analyzeContext);

		analyze(module.syntaxTree(), analyzeContext, linterContext);

		linterContext.afterAnalyzing(analyzeContext);

		return ReadOnlyList.from(diagnostics);
	}

	private void analyze(ISyntaxTree syntaxTree, AnalyzeContext analyzeContext, LinterContext linterContext)
	{
		for (var descendant : syntaxTree.descendants())
		{
			linterContext.analyze(descendant, analyzeContext);
			analyze(descendant, analyzeContext, linterContext);
		}
	}
}
