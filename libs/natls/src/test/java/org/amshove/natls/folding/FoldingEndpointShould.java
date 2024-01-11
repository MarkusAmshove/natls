package org.amshove.natls.folding;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.amshove.natls.testlifecycle.EmptyProjectTest;
import org.eclipse.lsp4j.FoldingRange;
import org.eclipse.lsp4j.FoldingRangeRequestParams;
import org.junit.jupiter.api.Test;

class FoldingEndpointShould extends EmptyProjectTest
{
	@Test
	void provideFoldingForStatementsWithBlock()
	{
		var foldings = getFoldings("""
			DEFINE DATA
			LOCAL 1 #I (I4)
			END-DEFINE

			IF #I < 5
			IGNORE
			END-IF

			END
		""");

		assertFolding(4, 6, foldings);
	}

	@Test
	void provideFoldingForIfWithElseBranch()
	{
		var foldings = getFoldings("""
			DEFINE DATA
			LOCAL 1 #I (I4)
			END-DEFINE

			IF #I < 5
			IGNORE
			ELSE
			IGNORE
			END-IF

			END
		""");

		assertFolding(4, 8, foldings);
		assertFolding(4, 8, foldings);
	}

	@Test
	void provideFoldingsForNestedStatements()
	{
		var foldings = getFoldings("""
			DEFINE DATA
			LOCAL 1 #I (I4)
			END-DEFINE

			IF #I < 5
				DECIDE FOR FIRST CONDITION
					WHEN #I < 4
						IGNORE
					WHEN #I < 3
						IGNORE
					WHEN NONE
						IGNORE
				END-DECIDE
			END-IF

			END
		""");

		assertFolding(4, 13, foldings);
		assertFolding(5, 12, foldings);
		assertFolding(6, 7, foldings);
		assertFolding(8, 9, foldings);
	}

	@Test
	void provideFoldingsForDecideOn()
	{
		var foldings = getFoldings("""
			DEFINE DATA
			LOCAL 1 #I (I4)
			END-DEFINE

			DECIDE ON FIRST VALUE #I
				VALUE 5
					IGNORE
				VALUE 4
					IGNORE
				NONE
					IGNORE
			END-DECIDE

			END
		""");

		assertFolding(4, 11, foldings);
	}

	@Test
	void provideFoldingsForDefineData()
	{
		var foldings = getFoldings("""
			DEFINE DATA
			LOCAL 1 #I (I4)
			END-DEFINE
			IGNORE
			END
		""");

		assertFolding(0, 2, foldings);
	}

	private List<FoldingRange> getFoldings(String source)
	{
		try
		{
			var document = createOrSaveFile("LIBONE", "SUB.NSN", source);
			var params = new FoldingRangeRequestParams();
			params.setTextDocument(document);
			return getContext().documentService().foldingRange(params).get(5, TimeUnit.SECONDS);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private void assertFolding(int fromLine, int toLine, List<FoldingRange> ranges)
	{
		assertThat(ranges)
			.as("No folding found starting at line %d and ending at line %d", fromLine, toLine)
			.anyMatch(r -> r.getStartLine() == fromLine && r.getEndLine() == toLine);
	}
}
