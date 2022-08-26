package org.amshove.natls.lsp;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DocumentSymbolsShould extends LanguageServerTest
{

	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("documentsymbols") LspTestContext testContext)
	{
		DocumentSymbolsShould.testContext = testContext;
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@TestFactory
	Iterable<DynamicTest> includeSubroutinesInDocumentSymbols()
	{
		var symbols = testContext.languageService().findSymbolsInFile(textDocumentIdentifier("LIBONE", "MANYSYM"));
		return List.of(
			testThatSymbolIsIncluded("#FIRST", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("#SECOND", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("#THIRD", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("#FOURTH", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("#FIFTH", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("#SIXTH", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("#SEVENTH", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("FIRST-SUB", SymbolKind.Method, symbols),
			testThatSymbolIsIncluded("SECOND-SUB", SymbolKind.Method, symbols)
		);
	}

	@TestFactory
	Iterable<DynamicTest> includeImportedSymbolsNoMatterTheNesting()
	{
		var symbols = testContext.languageService().findSymbolsInFile(textDocumentIdentifier("LIBONE", "IMPORTER"));
		return List.of(
			testThatSymbolIsIncluded("#INLDA-1", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("#INLDA-2", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("#INLDA-3", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("#INLDA-4", SymbolKind.Variable, symbols),
			testThatSymbolIsIncluded("INSIDE-INSIDE-COPYCODE", SymbolKind.Method, symbols)
		);
	}

	private DynamicTest testThatSymbolIsIncluded(String symbolName, SymbolKind kind, List<SymbolInformation> informationList)
	{
		return DynamicTest.dynamicTest("%s: %s".formatted(kind, symbolName), () -> {
			assertThat(informationList)
				.as("Expected symbol to be present")
				.anyMatch(si -> si.getKind() == kind && si.getName().equals(symbolName));
		});
	}
}
