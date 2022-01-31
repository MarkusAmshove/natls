package org.amshove.natlint.api;

import org.amshove.natparse.ReadOnlyList;

public abstract class AbstractAnalyzer
{
	/**
	 * This returns all {@link DiagnosticDescription}s that an analyzer can raise.
	 * @return all {@link DiagnosticDescription}s this analyzer may raise
	 */
	public abstract ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions();

	public abstract void initialize(ILinterContext context);

	/**
	 * This method gets called before the analyzer is run.
	 * It can be used to initialized stuff based on the context.
	 */
	public void beforeAnalyzing(IAnalyzeContext context)
	{

	}

	/**
	 * This method gets called after a module has been fully analyzed.
	 * It can be used for clean up and aggregating stuff.
	 */
	public void afterAnalyzing(IAnalyzeContext context)
	{

	}
}
