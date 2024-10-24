package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.amshove.natparse.natural.IEndTransactionNode;
import org.amshove.natparse.natural.IGetTransactionNode;
import org.amshove.natparse.natural.IBackoutTransactionNode;

public class HiddenTransactionAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription HIDDEN_TRANSACTION_STATEMENT_IS_DISCOURAGED = DiagnosticDescription.create(
		"NL032",
		"Use of [END|GET|BACKOUT] TRANSACTION statement outside Program is discouraged.",
		DiagnosticSeverity.WARNING
	);

	private boolean isTransactionAnalyzerOff;

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(HIDDEN_TRANSACTION_STATEMENT_IS_DISCOURAGED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IEndTransactionNode.class, this::analyzeTransaction);
		context.registerNodeAnalyzer(IGetTransactionNode.class, this::analyzeTransaction);
		context.registerNodeAnalyzer(IBackoutTransactionNode.class, this::analyzeTransaction);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		isTransactionAnalyzerOff = !context.getConfiguration(context.getModule().file(), "natls.style.discourage_hiddentransactions", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
	}

	private void analyzeTransaction(ISyntaxNode node, IAnalyzeContext context)
	{
		if (isTransactionAnalyzerOff)
		{
			return;
		}

		if (context.getModule().file().getFiletype() == NaturalFileType.PROGRAM)
		{
			return;
		}

		if (!NodeUtil.moduleContainsNode(context.getModule(), node))
		{
			return;
		}

		context.report(HIDDEN_TRANSACTION_STATEMENT_IS_DISCOURAGED.createDiagnostic(node));
	}
}
