package org.amshove.natlint.analyzers;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.SyntaxToken;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFileType;

public class LowercaseCodeAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription LOWERCASE_CODE_IS_DISCOURAGED = DiagnosticDescription.create(
		"NL039",
		"Code in lower/mixed case is discouraged",
		DiagnosticSeverity.INFO
	);

	private static final Pattern PATTERN_LOWERCASE = Pattern.compile(".*\\p{Ll}.*");
	private boolean isLowercaseCodeOff;

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(LOWERCASE_CODE_IS_DISCOURAGED);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerModuleAnalyzer(this::analyzeModule);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		isLowercaseCodeOff = !context.getConfiguration(context.getModule().file(), "natls.style.discourage_lowercase_code", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
	}

	private void analyzeModule(INaturalModule module, IAnalyzeContext context)
	{
		if (isLowercaseCodeOff)
		{
			return;
		}

		if (module.file().getFiletype().equals(NaturalFileType.DDM))
		{
			return;
		}

		for (SyntaxToken token : module.tokens())
		{
			if (token.kind() == SyntaxKind.STRING_LITERAL)
			{
				continue;
			}

			Matcher matches = PATTERN_LOWERCASE.matcher(token.source());
			if (matches.matches())
			{
				context.report(LOWERCASE_CODE_IS_DISCOURAGED.createDiagnostic(token));
				return;
			}
		}
	}
}
