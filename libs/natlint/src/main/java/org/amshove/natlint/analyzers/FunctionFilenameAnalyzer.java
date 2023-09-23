package org.amshove.natlint.analyzers;

import org.amshove.natlint.api.AbstractAnalyzer;
import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.ILinterContext;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.natural.IFunction;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFileType;

public class FunctionFilenameAnalyzer extends AbstractAnalyzer
{
	public static final DiagnosticDescription FUNCTION_FILE_NAME_MISMATCH = DiagnosticDescription.create(
		"NL020",
		"File name %s and function name %s differ. This can lead to confusion",
		DiagnosticSeverity.INFO
	);

	@Override
	public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
	{
		return ReadOnlyList.of(FUNCTION_FILE_NAME_MISMATCH);
	}

	@Override
	public void initialize(ILinterContext context)
	{
		context.registerModuleAnalyzer(this::analyzeFunction);
	}

	private void analyzeFunction(INaturalModule module, IAnalyzeContext context)
	{
		if (module.file().getFiletype() != NaturalFileType.FUNCTION || !(module instanceof IFunction function))
		{
			return;
		}

		var functionName = function.functionName();

		if (functionName == null)
		{
			return;
		}

		var filename = module.file().getFilenameWithoutExtension();

		if (!filename.equals(functionName.symbolName()))
		{
			context.report(
				FUNCTION_FILE_NAME_MISMATCH.createFormattedDiagnostic(
					functionName,
					filename,
					functionName.symbolName()
				)
			);
		}
	}
}
