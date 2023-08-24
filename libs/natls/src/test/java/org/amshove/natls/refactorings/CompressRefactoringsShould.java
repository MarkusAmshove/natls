package org.amshove.natls.refactorings;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CompressRefactoringsShould extends CodeActionTest
{
	private static LspTestContext testContext;

	@Test
	void offerToAddFullIfACompressDoesntHaveFullYet()
	{
		assertCodeActionWithTitle(
			"Add FULL to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				    DEFINE DATA
				    LOCAL
				    1 #VAR (A10)
				    1 #TEXT (A) DYNAMIC
				    END-DEFINE
					COM${}$PRESS #VAR INTO #TEXT
				    END
				"""
		);
	}

	@Test
	void notOfferToAddFullWhenFullIsAlreadyPresent()
	{
		assertNoCodeActionWithTitle(
			"Add FULL to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			""" 
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS FULL #VAR INTO #TEXT
				END
				"""
		);
	}

	@Test
	void addFullToCompressIfApplied()
	{
		assertCodeActionWithTitle(
			"Add FULL to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT
				END
				"""
		)
			.resultsApplied(
				"""
					DEFINE DATA
					LOCAL
					1 #VAR (A10)
					1 #TEXT (A) DYNAMIC
					END-DEFINE
					COMPRESS FULL #VAR INTO #TEXT
					END
					"""
			);
	}

	@Test
	void offerToAddNumericIfACompressDoesntHaveNumericYet()
	{
		assertCodeActionWithTitle(
			"Add NUMERIC to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT
				END
				"""
		);
	}

	@Test
	void notOfferToAddNumericWhenNumericIsAlreadyPresent()
	{
		assertNoCodeActionWithTitle(
			"Add NUMERIC to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			""" 
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS NUMERIC #VAR INTO #TEXT
				END
				"""
		);
	}

	@Test
	void addNumericToCompressIfApplied()
	{
		assertCodeActionWithTitle(
			"Add NUMERIC to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT
				END
				"""
		)
			.resultsApplied(
				"""
					DEFINE DATA
					LOCAL
					1 #VAR (A10)
					1 #TEXT (A) DYNAMIC
					END-DEFINE
					COMPRESS NUMERIC #VAR INTO #TEXT
					END
					"""
			);
	}

	@Test
	void addNumericToCompressAfterFullIfApplied()
	{
		assertCodeActionWithTitle(
			"Add NUMERIC to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS FULL #VAR INTO #TEXT
				END
				"""
		)
			.resultsApplied(
				"""
					DEFINE DATA
					LOCAL
					1 #VAR (A10)
					1 #TEXT (A) DYNAMIC
					END-DEFINE
					COMPRESS NUMERIC FULL #VAR INTO #TEXT
					END
					"""
			);
	}

	@Test
	void addFullToCompressBeforeNumericIfApplied()
	{
		assertCodeActionWithTitle(
			"Add FULL to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS NUMERIC #VAR INTO #TEXT
				END
				"""
		)
			.resultsApplied(
				"""
					DEFINE DATA
					LOCAL
					1 #VAR (A10)
					1 #TEXT (A) DYNAMIC
					END-DEFINE
					COMPRESS NUMERIC FULL #VAR INTO #TEXT
					END
					"""
			);
	}

	@Test
	void offerToAddDelimitersIfNotPresent()
	{
		assertCodeActionWithTitle(
			"Add WITH DELIMITERS to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT
				END
				"""
		);
	}

	@Test
	void notOfferToAddDelimitersIfAlreadyPresent()
	{
		assertNoCodeActionWithTitle(
			"Add WITH DELIMITERS to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			""" 
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT WITH DELIMITERS ';'
				END
				"""
		);
	}

	@Test
	void addWithDelimiterIfApplied()
	{
		assertCodeActionWithTitle(
			"Add WITH DELIMITERS to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT
				END
				"""
		)
			.resultsApplied(
				"""
					DEFINE DATA
					LOCAL
					1 #VAR (A10)
					1 #TEXT (A) DYNAMIC
					END-DEFINE
					COMPRESS #VAR INTO #TEXT WITH DELIMITERS ';'
					END
					"""
			);
	}

	@Test
	void offerToAddLeavingNoIfNotPresent()
	{
		assertCodeActionWithTitle(
			"Add LEAVING NO SPACE to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT
				END
				"""
		);
	}

	@Test
	void notOfferToAddLeavingNoIfAlreadyPresent()
	{
		assertNoCodeActionWithTitle(
			"Add LEAVING NO SPACE to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			""" 
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT LEAVING NO
				END
				"""
		);
	}

	@Test
	void notOfferToAddLeavingNoSpaceIfAlreadyPresent()
	{
		assertNoCodeActionWithTitle(
			"Add LEAVING NO SPACE to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			""" 
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT LEAVING NO SPACE
				END
				"""
		);
	}

	@Test
	void addLeavingNoIfApplied()
	{
		assertCodeActionWithTitle(
			"Add LEAVING NO SPACE to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT
				END
				"""
		)
			.resultsApplied(
				"""
					DEFINE DATA
					LOCAL
					1 #VAR (A10)
					1 #TEXT (A) DYNAMIC
					END-DEFINE
					COMPRESS #VAR INTO #TEXT LEAVING NO SPACE
					END
					"""
			);
	}

	@Test
	void offerToAddAllDelimitersIfNotPresent()
	{
		assertCodeActionWithTitle(
			"Add WITH ALL DELIMITERS to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				    DEFINE DATA
				    LOCAL
				    1 #VAR (A10)
				    1 #TEXT (A) DYNAMIC
				    END-DEFINE
					COM${}$PRESS #VAR INTO #TEXT
				    END
				"""
		);
	}

	@Test
	void notOfferToAddAllDelimitersIfAlreadyPresent()
	{
		assertNoCodeActionWithTitle(
			"Add WITH ALL DELIMITERS to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			""" 
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT WITH ALL DELIMITERS ';'
				END
				"""
		);
	}

	@Test
	void addWithAllDelimiterIfApplied()
	{
		assertCodeActionWithTitle(
			"Add WITH ALL DELIMITERS to COMPRESS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT
				END
				"""
		)
			.resultsApplied(
				"""
					DEFINE DATA
					LOCAL
					1 #VAR (A10)
					1 #TEXT (A) DYNAMIC
					END-DEFINE
					COMPRESS #VAR INTO #TEXT WITH ALL DELIMITERS ';'
					END
					"""
			);
	}

	@Test
	void offerToAddAllDelimitersIfNotPresentButWithDelimitersIsPresent()
	{
		assertCodeActionWithTitle(
			"Add ALL to DELIMITERS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT WITH DELIMITERS ';'
				END
				"""
		);
	}

	@Test
	void addWithAllDelimitersIfAppliedWhenWithDelimitersIsPresent()
	{
		assertCodeActionWithTitle(
			"Add ALL to DELIMITERS",
			"LIBONE",
			"SUBN.NSN",
			"""
				DEFINE DATA
				LOCAL
				1 #VAR (A10)
				1 #TEXT (A) DYNAMIC
				END-DEFINE
				COM${}$PRESS #VAR INTO #TEXT WITH DELIMITERS ';'
				END
				"""
		)
			.resultsApplied(
				"""
					DEFINE DATA
					LOCAL
					1 #VAR (A10)
					1 #TEXT (A) DYNAMIC
					END-DEFINE
					COMPRESS #VAR INTO #TEXT WITH ALL DELIMITERS ';'
					END
					"""
			);
	}

	@Test
	void offerAllRefactoringsIfTheCompressIsBoring()
	{
		var source = """
			DEFINE DATA
			LOCAL
			1 #VAR (A10)
			1 #TEXT (A) DYNAMIC
			END-DEFINE
			COM${}$PRESS #VAR INTO #TEXT
			END
			""";
		var library = "LIBONE";
		var module = "SUBN.NSN";

		assertCodeActionWithTitle("Add FULL to COMPRESS", library, module, source);
		assertCodeActionWithTitle("Add NUMERIC to COMPRESS", library, module, source);
		assertCodeActionWithTitle("Add WITH DELIMITERS to COMPRESS", library, module, source);
		assertCodeActionWithTitle("Add WITH ALL DELIMITERS to COMPRESS", library, module, source);
		assertCodeActionWithTitle("Add LEAVING NO SPACE to COMPRESS", library, module, source);
	}

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Override
	protected ICodeActionProvider getCodeActionUnderTest()
	{
		return new CompressRefactorings();
	}

}
