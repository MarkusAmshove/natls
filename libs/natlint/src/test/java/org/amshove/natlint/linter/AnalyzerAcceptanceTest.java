package org.amshove.natlint.linter;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class AnalyzerAcceptanceTest
{

	@TestFactory
	Stream<DynamicTest> allAnalyzersShouldExportDiagnosticDescriptions()
	{
		return LinterContext.INSTANCE.registeredAnalyzers().stream()
			.map( a -> dynamicTest(
				"%s should export diagnostic descriptions".formatted(a.getClass().getSimpleName()),
				() -> assertThat(a.getDiagnosticDescriptions()).isNotEmpty())
			);
	}

	@TestFactory
	Stream<DynamicTest> allDiagnosticDescriptionIdsShouldBeUnique()
	{
		var allAnalyzers = LinterContext.INSTANCE.registeredAnalyzers();
		return allAnalyzers.stream()
			.flatMap(a -> a.getDiagnosticDescriptions().stream())
			.map(testedDescription -> dynamicTest(
				"Diagnostic id %s should be unique".formatted(testedDescription.getId()),
				() -> assertThat(
					allAnalyzers.stream()
						.flatMap(a -> a.getDiagnosticDescriptions().stream())
						.filter(dDescription -> dDescription.getId().equals(testedDescription.getId()))
						.count()
				)
					.as(
						"Diagnostic id is exported by the following analyzers:  %s",
							allAnalyzers.stream()
								.filter(a -> a.getDiagnosticDescriptions().stream().anyMatch(dDescription -> dDescription.getId().equals(testedDescription.getId())))
								.map(a -> a.getClass().getSimpleName())
								.collect(Collectors.joining(", "))
						)
					.isEqualTo(1L)
			));
	}
}
