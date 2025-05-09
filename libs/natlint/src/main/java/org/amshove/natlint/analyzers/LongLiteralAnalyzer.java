package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.NodeUtil;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.ISyntaxNode;
import org.amshove.natparse.natural.ILiteralNode;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class LongLiteralAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription LONG_LITERAL_DETECTED = DiagnosticDescription.create(
		"NL038",
		"A long literal containing lowercase detected.",
		DiagnosticSeverity.WARNING
	);

	private static final Pattern PATTERN_LOWERCASE = Pattern.compile(".*\\p{Ll}.*");

	private boolean isLongLiteralAnalyserOff;

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(LONG_LITERAL_DETECTED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerNodeAnalyzer(ILiteralNode.class, this::analyzeLongLiteral);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		isLongLiteralAnalyserOff = !context.getConfiguration(context.getModule().file(), "natls.style.discourage_long_literals", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
	}

	private void analyzeLongLiteral(ISyntaxNode node, IAnalyzeContext context)
	{
		if (isLongLiteralAnalyserOff)
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

		var literal = (ILiteralNode) node;
		// literal.token().source() includes the enclosing quotes
		if (literal.token().source().length() >= 7)
		{
			Matcher matches = PATTERN_LOWERCASE.matcher(literal.token().source());
			if (matches.matches())
			{
				context.report(LONG_LITERAL_DETECTED.createDiagnostic(node));
			}
		}
	}
}
