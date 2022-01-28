package org.amshove.natlint.linter;

import org.amshove.natlint.api.IAnalyzeContext;
import org.amshove.natlint.api.IDiagnosticReporter;
import org.amshove.natlint.api.LinterDiagnostic;
import org.amshove.natparse.natural.INaturalModule;

class AnalyzeContext implements IAnalyzeContext
{
	private final INaturalModule module;
	private final IDiagnosticReporter diagnosticReporter;

	AnalyzeContext(INaturalModule module, IDiagnosticReporter diagnosticReporter)
	{
		this.module = module;
		this.diagnosticReporter = diagnosticReporter;
	}

	@Override
	public INaturalModule getModule()
	{
		return module;
	}

	@Override
	public void report(LinterDiagnostic diagnostic)
	{
		diagnosticReporter.report(diagnostic);
	}
}
