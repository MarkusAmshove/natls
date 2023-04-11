package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalProgrammingMode;

public class NaturalSourceHeaderAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription MISSING_SOURCE_HEADER = DiagnosticDescription.create(
		"NL016",
		"Module is missing the source header. This might cause problems when compiling.",
		DiagnosticSeverity.ERROR
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(MISSING_SOURCE_HEADER);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerModuleAnalyzer(this::analyzeModule);
	}

	private void analyzeModule(INaturalModule module, IAnalyzeContext context)
	{
		if (module.programmingMode() == NaturalProgrammingMode.UNKNOWN)
		{
			context.report(MISSING_SOURCE_HEADER.createModuleDiagnostic(module));
		}
	}
}
