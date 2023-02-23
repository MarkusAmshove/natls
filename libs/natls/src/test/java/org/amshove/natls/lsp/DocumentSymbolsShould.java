package org.amshove.natls.lsp;

import org.amshove.natls.testlifecycle.LanguageServerTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
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
		var symbols = findSymbols("MANYSYM");
		return List.of(
			DynamicTest.dynamicTest("Module root symbol should be included", () ->
			{
				assertThat(symbols.get(0).getKind()).isEqualTo(SymbolKind.Class);
				assertThat(symbols.get(0).getName()).isEqualTo("MANYSYM");
			}),
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
	Iterable<DynamicTest> includeNestedSubroutines()
	{
		var symbols = findSymbols("INTERNAL-SUB");
		return List.of(
			testThatSymbolIsIncluded("INTERNAL-SUB", SymbolKind.Method, symbols),
			testThatSymbolIsIncluded("NESTED-SUB", SymbolKind.Method, symbols)
		);
	}

	private List<DocumentSymbol> findSymbols(String moduleName)
	{
		var params = new DocumentSymbolParams(textDocumentIdentifier("LIBONE", moduleName));
		try
		{
			var result = testContext.documentService().documentSymbol(params).get();
			return result.stream().map(Either::getRight).toList();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private DynamicTest testThatSymbolIsIncluded(String symbolName, SymbolKind kind, List<DocumentSymbol> informationList)
	{
		return DynamicTest.dynamicTest("%s: %s".formatted(kind, symbolName), () ->
		{
			assertThat(informationList.stream().flatMap(ds -> ds.getChildren().stream()))
				.as("Expected symbol %s to be present".formatted(symbolName))
				.anyMatch(si -> si.getKind() == kind && si.getName().equals(symbolName));
		});
	}
}
