package org.amshove.natqube.ruletranslator;

import org.amshove.natlint.api.DiagnosticDescription;
import org.amshove.natlint.linter.LinterContext;
import org.amshove.natparse.parsing.ParserError;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class DiagnosticMappingAcceptanceTests
{
	private static final List<String> VALID_PRIORITIES = List.of("BLOCKER", "CRITICAL", "MAJOR", "MINOR");

	// https://docs.sonarqube.org/latest/user-guide/built-in-rule-tags/
	private static final List<String> VALID_TAGS = List.of("natparse-internal", "performance", "brain-overload", "bad-practice", "cert", "clumsy", "confusing", "convention", "cwe", "design", "lock-in", "owasp", "pitfall", "sans-top25", "suspicious", "unpredictable", "unused", "user-experience", "compile-time", "runtime-error");
	private static final List<String> VALID_TYPES = List.of("CODE_SMELL", "BUG", "VULNERABILITY", "SECURITY_HOTSPOT");

	@TestFactory
	Stream<DynamicTest> everyDiagnosticShouldHaveAMapping()
	{
		var parserErrorIds = Arrays.stream(ParserError.values()).map(ParserError::id);
		var linterDiagnosticIds = LinterContext.INSTANCE.registeredAnalyzers().stream()
			.flatMap(a -> a.getDiagnosticDescriptions().stream())
			.map(DiagnosticDescription::getId);

		var rules = RuleRepository.getRuleMappings();
		return Stream.concat(parserErrorIds, linterDiagnosticIds)
			.map(
				id -> DynamicTest.dynamicTest(
					"%s should have a mapping to SonarQube".formatted(id), () -> assertThat(rules)
						.containsKey(id)
				)
			);
	}

	@TestFactory
	Stream<DynamicContainer> everyMappingShouldBringAllNeededProperties()
	{
		var rules = RuleRepository.getRules();
		return rules.stream()
			.map(
				r -> DynamicContainer.dynamicContainer(
					r.key(), Stream.of(
						DynamicTest.dynamicTest(
							"name should be filled", () -> assertThat(r.name()).isNotBlank()
						),
						DynamicTest.dynamicTest(
							"priority should be filled and valid", () -> assertAll(
								() -> assertThat(r.priority()).isNotBlank(),
								() -> assertThat(VALID_PRIORITIES).contains(r.priority())
							)
						),
						DynamicTest.dynamicTest(
							"tags should be filled and valid", () -> assertAll(
								() -> assertThat(r.tags()).isNotEmpty(),
								() -> assertThat(VALID_TAGS).containsAll(r.tags())
							)
						),
						DynamicTest.dynamicTest(
							"type should be filled and valid", () -> assertAll(
								() -> assertThat(r.type()).isNotBlank(),
								() -> assertThat(VALID_TYPES).contains(r.type())
							)
						),
						DynamicTest.dynamicTest(
							"description should be filled", () -> assertThat(r.description()).isNotBlank()
						)
					)
				)
			);
	}

