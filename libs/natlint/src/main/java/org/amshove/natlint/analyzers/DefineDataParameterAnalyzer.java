package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.IScopeNode;

public class DefineDataParameterAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription USE_OF_INLINE_PARAMETER_IS_DISCOURAGED = DiagnosticDescription.create(
		"NL031",
		"Use of inline parameter is discouraged.",
		DiagnosticSeverity.WARNING
	);

	private boolean isParameterAnalyserOff;

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(USE_OF_INLINE_PARAMETER_IS_DISCOURAGED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(IScopeNode.class, this::analyzeDefineDataParameter);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		isParameterAnalyserOff = !context.getConfiguration(context.getModule().file(), "natls.style.disallowinlineparameters", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
	}

	private void analyzeDefineDataParameter(ISyntaxNode node, IAnalyzeContext context)
	{
		if (isParameterAnalyserOff)
		{
			return;
		}

		if (UNWANTED_FILETYPES.contains(context.getModule().file().getFiletype()))
		{
			return;
		}

		if (!NodeUtil.moduleContainsNode(context.getModule(), node))
		{
			return;
		}

		var scope = (IScopeNode) node;
		if (scope.scope().isParameter())
		{
			context.report(USE_OF_INLINE_PARAMETER_IS_DISCOURAGED.createDiagnostic(node));
		}
	}
}
