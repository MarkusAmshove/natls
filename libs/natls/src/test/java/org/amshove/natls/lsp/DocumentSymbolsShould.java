package org.amshove.natls.lsp;

import org.amshove.natls.documentsymbol.SymbolInformationProvider;
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

@SuppressWarnings("deprecation")
class DocumentSymbolsShould extends LanguageServerTest
{

	private static LspTestContext testContext;
	private SymbolInformationProvider sut = new SymbolInformationProvider();

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
		var file = testContext.project().findFileByReferableName("MANYSYM");
		var symbols = sut.provideSymbols(file.module());
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
		var file = testContext.project().findFileByReferableName("IMPORTER");
		var symbols = sut.provideSymbols(file.module());
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
