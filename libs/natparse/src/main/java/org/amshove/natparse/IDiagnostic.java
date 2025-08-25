package org.amshove.natparse;

import java.util.Locale;

public interface IDiagnostic extends IPosition
{
	String id();

	String message();

	DiagnosticSeverity severity();

	ReadOnlyList<AdditionalDiagnosticInfo> additionalInfo();

	/**
	 * A URL to the documentation of this diagnostic.
	 */
	default String descriptionUrl()
	{
		return "https://nat-ls.github.io/diagnostics/%s".formatted(id().toLowerCase(Locale.ROOT));
	}
}
