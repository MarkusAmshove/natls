package org.amshove.natparse;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiagnosticSeverityShould
{
	@TestFactory
	Iterable<DynamicTest> mapSeveritiesByStringRepresentation()
	{
		return List.of(
			testMapping("WARN", DiagnosticSeverity.WARNING),
			testMapping("WARNING", DiagnosticSeverity.WARNING),
			testMapping("INFO", DiagnosticSeverity.INFO),
			testMapping("ERROR", DiagnosticSeverity.ERROR),
			testMapping("warn", DiagnosticSeverity.WARNING),
			testMapping("warning", DiagnosticSeverity.WARNING),
			testMapping("info", DiagnosticSeverity.INFO),
			testMapping("error", DiagnosticSeverity.ERROR)
		);
	}

	@Test
	void throwOnUnknownSeverityConversion()
	{
		var message = assertThrows(IllegalArgumentException.class, () -> DiagnosticSeverity.fromString("unknown"))
			.getMessage();
		assertThat(message).isEqualTo("Invalid severity: unknown");
	}
	private DynamicTest testMapping(String text, DiagnosticSeverity severity)
	{
		return DynamicTest.dynamicTest("%s -> %s".formatted(text, severity), () ->
			assertThat(DiagnosticSeverity.fromString(text)).isEqualTo(severity)
		);
	}
}
