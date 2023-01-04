package org.amshove.natlint.api;

import org.amshove.natparse.natural.INaturalModule;

public interface IAnalyzeContext
{
	INaturalModule getModule();

	void report(LinterDiagnostic diagnostic);
}
