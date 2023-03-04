package org.amshove.natls.snippets;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTest;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natparse.natural.project.NaturalFileType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@LspTest
public class StatementSnippetProviderShould extends LanguageServerTest
{
	private static LspTestContext testContext;
	private static LanguageServerFile fileUnderTest;

	private final StatementSnippetProvider sut = new StatementSnippetProvider();

	private static final List<String> EXPECTED_SNIPPETS = List.of(
		"subr", "if", "decideForFirstCondition", "decideOnFirstValue", "decideOnEveryValue", "for", "sourceHeader", "resize", "resizeReset",
		"compress"
	);

	@BeforeAll
	static void beforeAll(@LspProjectName("modrefparser") LspTestContext context)
	{
		testContext = context;
		fileUnderTest = context.server().getLanguageService().findNaturalFile("LIBONE", "SUB");
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@TestFactory
	Stream<DynamicTest> provideEachSnippetWhenTheFileTypeAllowsStatements()
	{
		return EXPECTED_SNIPPETS.stream()
			.flatMap(
				expectedSnippet -> Arrays.stream(NaturalFileType.values())
					.filter(NaturalFileType::canHaveBody)
					.map(
						ft -> dynamicTest(
							"%s should be applicable to %s".formatted(expectedSnippet, ft), () -> assertThat(sut.provideSnippets(fileUnderTest))
								.as("Expected snippet %s not found".formatted(expectedSnippet))
								.anyMatch(ci -> ci.getLabel().equals(expectedSnippet))
						)
					)
			);
	}

	@TestFactory
	Stream<DynamicTest> notProvideTheSnippetWhenTheFileTypeDoesntAllowStatements()
	{
		return EXPECTED_SNIPPETS.stream()
			.flatMap(
				expectedSnippet -> Arrays.stream(NaturalFileType.values())
					.filter(not(NaturalFileType::canHaveBody))
					.map(
						ft -> dynamicTest(
							"%s should not be applicable to %s, but is".formatted(expectedSnippet, ft), () -> assertThat(sut.provideSnippets(fileUnderTest))
								.as("Expected snippet %s not found".formatted(expectedSnippet))
								.anyMatch(ci -> ci.getLabel().equals(expectedSnippet))
						)
					)
			);
	}
}
