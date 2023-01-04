package org.amshove.natlint.linter;

import org.amshove.natlint.api.DiagnosticDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class AnalyzerAcceptanceTest
{
	@BeforeEach
	void beforeEach()
	{
		LinterContext.INSTANCE.reinitialize();
	}

	@TestFactory
	Stream<DynamicTest> allAnalyzersShouldExportDiagnosticDescriptions()
	{
		return LinterContext.INSTANCE.registeredAnalyzers().stream()
			.map(
				a -> dynamicTest(
					"%s should export diagnostic descriptions".formatted(a.getClass().getSimpleName()),
					() -> assertThat(a.getDiagnosticDescriptions()).isNotEmpty()
				)
			);
	}

	@TestFactory
	Stream<DynamicTest> allDiagnosticDescriptionIdsShouldBeUnique()
	{
		var allAnalyzers = LinterContext.INSTANCE.registeredAnalyzers();
		return allAnalyzers.stream()
			.flatMap(a -> a.getDiagnosticDescriptions().stream())
			.map(
				testedDescription -> dynamicTest(
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
				)
			);
	}

	@TestFactory
	Stream<DynamicTest> allDiagnosticDescriptionIdsShouldStartWithNL()
	{
		return LinterContext.INSTANCE.registeredAnalyzers().stream()
			.flatMap(a -> a.getDiagnosticDescriptions().stream())
			.map(
				dd -> dynamicTest(
					"%s should start with NL".formatted(dd.getId()),
					() -> assertThat(dd.getId()).startsWith("NL")
				)
			);
	}

	@Test
	void thereShouldBeNoGapsBetweenDiagnosticIds()
	{
		var allNumericIds = LinterContext.INSTANCE.registeredAnalyzers().stream()
			.flatMap(a -> a.getDiagnosticDescriptions().stream().map(DiagnosticDescription::getId))
			.map(id -> id.substring(2))
			.map(Integer::parseInt)
			.sorted()
			.toList();
		allNumericIds.stream().max(Integer::compareTo)
			.ifPresent(highestId ->
			{
				for (var i = 1; i <= highestId; i++)
				{
					assertThat(allNumericIds).contains(i);
				}
			});
	}
}
