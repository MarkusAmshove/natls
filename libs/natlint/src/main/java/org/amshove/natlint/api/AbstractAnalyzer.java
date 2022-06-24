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
	 * This method gets called before the analyzer is run for a module.
	 * An instance of an Analyzer is always shared between all runs, therefor
	 * any state that is saved should be identifiable with the module.
	 * It can happen that an Analyzer instance might analyze two modules in parallel.
	 */
	public void beforeAnalyzing(IAnalyzeContext context)
	{

	}

	/**
	 * This method gets called after a module has been fully analyzed.
	 * It can be used for e.g. clean up and aggregating metrics.
	 */
	public void afterAnalyzing(IAnalyzeContext context)
	{

	}
}
