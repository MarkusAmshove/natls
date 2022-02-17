package org.amshove.natparse;

public interface IDiagnostic extends IPosition
{
	String id();
	String message();
	DiagnosticSeverity severity();

	/**
	 * <strong>You almost always want to use the Diagnostics own position instead of this method.</strong> </br>
	 *
	 * Returns the original position where the Diagnostic appeared for purposes of <strong>printing both locations</strong>. </br>
	 * This only differs from the Diagnostics own position if the diagnostic was e.g. raised
	 * in a copycode. </br>
	 * In that case, the Diagnostics own position should be on the INCLUDE, while the originalPosition
	 * should return the position within the copy code where the issue was raised. </br>
	 * If no original position is present, this should return the diagnostics position itself. </br>
	 */
	IPosition originalPosition();

	boolean hasOriginalPosition();

	default String toVerboseString()
	{
		return "Diagnostic{line=%d, id='%s', severity=%s}".formatted(line(), id(), severity());
	}
}
