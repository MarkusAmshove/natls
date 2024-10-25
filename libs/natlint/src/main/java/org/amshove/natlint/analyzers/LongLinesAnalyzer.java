package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.PlainPosition;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.lexing.text.SourceTextScanner;

public class LongLinesAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription MAINFRAME_LONG_LINE = DiagnosticDescription.create(
		"NL033",
		"Long line found, it will not be visible immediately on Mainframes",
		DiagnosticSeverity.INFO
	);

	private boolean isMainframeLongLineOff;

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(MAINFRAME_LONG_LINE);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerModuleAnalyzer(this::analyzeModule);
	}

	@Override
	public void beforeAnalyzing(IAnalyzeContext context)
	{
		isMainframeLongLineOff = !context.getConfiguration(context.getModule().file(), "natls.style.mark_mainframelongline", OPTION_FALSE).equalsIgnoreCase(OPTION_TRUE);
	}

	private void analyzeModule(INaturalModule module, IAnalyzeContext context)
	{
		if (isMainframeLongLineOff)
		{
			return;
		}

		var fileContent = new ActualFilesystem().readFile(module.file().getPath());
		var scanner = new SourceTextScanner(fileContent);
		scanner.start();
		int lineCnt = 0;
		int offset = 0;

		while (!scanner.isAtEnd())
		{
			int advanceBy = (scanner.peek() == '\r' && scanner.peek(1) == '\n') ? 2 : (scanner.peek() == '\n') ? 1 : 0;
			if (advanceBy > 0)
			{
				if (scanner.lexemeLength() > 72)
				{
					context.report(MAINFRAME_LONG_LINE.createDiagnostic(new PlainPosition(offset, 0, lineCnt, 73, module.file().getPath())));
					break;
				}
				lineCnt++;
				offset += advanceBy;
				scanner.advance(advanceBy);
				scanner.start();
			}
			else
			{
				offset++;
				scanner.advance();
			}
		}
	}
}
