package org.amshove.natls.references;

import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.ReferenceContext;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class FindReferencesTests extends LanguageServerTest
{
	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("findreferences") LspTestContext context)
	{
		testContext = context;
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@TestFactory
	Stream<DynamicContainer> testReferences() throws IOException
	{
		var allFiles = testContext.project().libraries().stream().flatMap(l -> l.files().stream()).toList();
		var referenceTests = new ArrayList<ReferenceTest>();
		for (var file : allFiles)
		{
			var lines = Files.readAllLines(file.getPath());
			var lineNumber = 0;
			for (var line : lines)
			{
				if (line.contains("!ref"))
				{
					referenceTests.add(parseReferenceTest(file, line, lineNumber));
				}
				lineNumber++;
			}
		}
		return referenceTests.stream()
			.map(rT -> DynamicContainer.dynamicContainer(rT.file.getReferableName(), rT.expectedReferences.stream().map(ref ->
			{
				var theModule = rT.file.findNaturalModule(ref.modulename);

				var params = new ReferenceParams();
				params.setTextDocument(rT.identifier());
				params.setPosition(rT.position());
				params.setContext(new ReferenceContext(true));

				try
				{
					var references = testContext.languageService().findReferences(params).get();

					return dynamicTest(
						"%d:%d -> %s:%d:%d".formatted(rT.line, rT.col, ref.modulename, ref.line, ref.col),
						() -> assertThat(references)
							.as("No given reference matches expected location")
							.anyMatch(
								l -> l.getUri().equals(theModule.file().getPath().toUri().toString())
									&& l.getRange().getStart().getLine() == ref.line
									&& l.getRange().getStart().getCharacter() == ref.col
							)
					);
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			})));
	}

	private ReferenceTest parseReferenceTest(LanguageServerFile file, String line, int lineNumber)
	{
		// Format: !ref(<column in line>){<MODULENAME>,<LINE>,<COLUMN>;...}
		var comment = line.substring(line.indexOf("/*") + 2).trim();
		var column = comment.substring("!ref(".length(), comment.indexOf(")"));
		var definedExpectedReferences = comment.substring(comment.indexOf("{") + 1, comment.indexOf("}")).split(";");

		return new ReferenceTest(file, lineNumber, Integer.parseInt(column), Arrays.stream(definedExpectedReferences).map(ExpectedReference::fromCsv).toList());
	}

	record ExpectedReference(int line, int col, String modulename)
	{
		static ExpectedReference fromCsv(String csv)
		{
			var split = csv.split(",");
			return new ExpectedReference(
				Integer.parseInt(split[1]),
				Integer.parseInt(split[2]),
				split[0]
			);
		}
	}

	record ReferenceTest(LanguageServerFile file, int line, int col, List<ExpectedReference> expectedReferences)
	{
		TextDocumentIdentifier identifier()
		{
			return new TextDocumentIdentifier(file.getUri());
		}

		Position position()
		{
			return new Position(line, col);
		}
	}
}
