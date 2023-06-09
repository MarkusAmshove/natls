package org.amshove.natlint.api;

import org.amshove.natparse.ReadOnlyList;

public abstract class AbstractAnalyzer
{
	public static final String OPTION_TRUE = "true";
	public static final String OPTION_FALSE = "false";

	/**
	 * This returns all {@link DiagnosticDescription}s that an analyzer can raise.
	 *
	 * @return all {@link DiagnosticDescription}s this analyzer may raise
	 */
	public abstract ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions();

	/**
	 * This method is used and called when the Analyzer is recognized and added to the context of the linter.<br/>
	 * It is run during startup before any analysis happens.<br/>
	 * Use this to register the tokens or nodes you want to analyze through the {@code context} parameter.<br/>
	 * <br/>
	 * The {@code .editorconfig} file is not yet accessible here.
	 */
	public abstract void initialize(ILinterContext context);

	/**
	 * This method gets called before the analyzer is run for a module. An instance of an Analyzer is always shared
	 * between all runs, therefor any state that is saved should be identifiable with the module. It can happen that an
	 * Analyzer instance might analyze two modules in parallel.
	 */
	public void beforeAnalyzing(IAnalyzeContext context)
	{

	}

	/**
	 * This method gets called after a module has been fully analyzed. It can be used for e.g. clean up and aggregating
	 * metrics.
	 */
	public void afterAnalyzing(IAnalyzeContext context)
	{

	}
}
