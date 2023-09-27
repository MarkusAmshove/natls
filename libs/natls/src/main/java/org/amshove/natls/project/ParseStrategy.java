package org.amshove.natls.project;

public enum ParseStrategy
{
	/**
	 * Tells the parser to also parse all dependants <strong>if needed</strong>.<br/>
	 * The parser will determine itself if the callers have to be parsed.
	 */
	WITH_CALLERS,

	/**
	 * Force the parser to skip parsing callers.
	 */
	WITHOUT_CALLERS;
}
