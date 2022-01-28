package org.amshove.natlint.api;

@FunctionalInterface
public interface IDiagnosticReporter
{
	void report(LinterDiagnostic diagnostic);
}
