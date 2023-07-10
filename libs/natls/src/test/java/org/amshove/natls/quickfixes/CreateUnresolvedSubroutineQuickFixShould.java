package org.amshove.natls.quickfixes;

import org.amshove.natls.codeactions.ICodeActionProvider;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.amshove.natparse.parsing.ParserError;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CreateUnresolvedSubroutineQuickFixShould extends CodeActionTest
{
	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
	{
		testContext = context;
	}

	@Override
	protected ICodeActionProvider getCodeActionUnderTest()
	{
		return new CreateUnresolvedSubroutineQuickFix();
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	@Test
	void addASubroutine()
	{
		assertCodeActionWithTitle("Declare inline subroutine MY-SUB", "LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			
			PERFORM MY-S${}$UB
			
			END
			""")
			.fixes(ParserError.UNRESOLVED_IMPORT.id())
			.resultsApplied("""
				DEFINE DATA LOCAL
				END-DEFINE
				
				PERFORM MY-SUB
				
				/***********************************************************************
				DEFINE SUBROUTINE MY-SUB
				/***********************************************************************

				IGNORE

				END-SUBROUTINE
				
				END
				""");
	}

	@Test
	void addASubroutineBeforeTheEndToken()
	{
		assertCodeActionWithTitle("Declare inline subroutine MY-SUB", "LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			
			PERFORM MY-S${}$UB
			IGNORE
			WRITE 'HI'
			
			END
			""")
			.fixes(ParserError.UNRESOLVED_IMPORT.id())
			.resultsApplied("""
				DEFINE DATA LOCAL
				END-DEFINE
				
				PERFORM MY-SUB
				IGNORE
				WRITE 'HI'
				
				/***********************************************************************
				DEFINE SUBROUTINE MY-SUB
				/***********************************************************************

				IGNORE

				END-SUBROUTINE
				
				END
				""");
	}

	@Test
	void addASubroutineInAnExternalSubroutine()
	{
		assertCodeActionWithTitle("Declare inline subroutine MY-SUB", "LIBONE", "SUB.NSS", """
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE SUBROUTINE EXT
			PERFORM MY-S${}$UB
			END-SUBROUTINE
			END
			""")
			.fixes(ParserError.UNRESOLVED_IMPORT.id())
			.resultsApplied("""
				DEFINE DATA LOCAL
				END-DEFINE
				DEFINE SUBROUTINE EXT
				PERFORM MY-SUB
				/***********************************************************************
				DEFINE SUBROUTINE MY-SUB
				/***********************************************************************

				IGNORE

				END-SUBROUTINE
				
				END-SUBROUTINE
				END
				""");
	}

}
