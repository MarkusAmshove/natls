package org.amshove.natls.completion;

import org.eclipse.lsp4j.CompletionItemKind;
import org.junit.jupiter.api.Test;

class ExternalModuleCompletionShould extends CompletionTest
{
	@Test
	void completeFunctions()
	{
		createOrSaveFile("LIBONE", "FUNC.NS7", """
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA LOCAL
			END-DEFINE
			FUNC := TRUE
			END-FUNCTION
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			IF ${}$
			IGNORE
			END-IF
			END
			""")
			.assertContainsCompleting("FUNC", CompletionItemKind.Function, "FUNC(<>)$0");
	}

	@Test
	void completeFunctionsWithParameter()
	{
		createOrSaveFile("LIBONE", "FUNC.NS7", """
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA
			PARAMETER
			1 P-PARAM (A1)
			END-DEFINE
			FUNC := TRUE
			END-FUNCTION
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			IF ${}$
			IGNORE
			END-IF
			END
			""")
			.assertContainsCompleting("FUNC", CompletionItemKind.Function, "FUNC(<${1:P-PARAM}>)$0");
	}

	@Test
	void completeExternalSubroutines()
	{
		createOrSaveFile("LIBONE", "SUBR.NSS", """
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE SUBROUTINE MY-SUBROUTINE
			IGNORE
			END-SUBROUTINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			${}$
			END
			""")
			.assertContainsCompleting("MY-SUBROUTINE", CompletionItemKind.Event, "PERFORM MY-SUBROUTINE%n$0".formatted());
	}

	@Test
	void completeExternalSubroutinesWithParameter()
	{
		createOrSaveFile("LIBONE", "SUBR.NSS", """
			DEFINE DATA
			PARAMETER
			1 #P-PARM1 (A10)
			1 #P-PARM2 (A10)
			END-DEFINE
			DEFINE SUBROUTINE MY-SUBROUTINE
			IGNORE
			END-SUBROUTINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			${}$
			END
			""")
			.assertContainsCompleting("MY-SUBROUTINE", CompletionItemKind.Event, "PERFORM MY-SUBROUTINE ${1:#P-PARM1} ${2:#P-PARM2}%n$0".formatted());
	}

	@Test
	void completeExternalSubroutinesWithoutPerformKeywordWhenPerformIsAlreadyPresent()
	{
		createOrSaveFile("LIBONE", "SUBR.NSS", """
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE SUBROUTINE MY-SUBROUTINE
			IGNORE
			END-SUBROUTINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			PERFORM ${}$
			END
			""")
			.assertContainsCompleting("MY-SUBROUTINE", CompletionItemKind.Event, "MY-SUBROUTINE%n$0".formatted());
	}

	@Test
	void completeExternalSubroutinesWithoutPerformKeywordWhenPerformIsAlreadyPresentAndSomeOfTheNameOfTheSubroutineWasTyped()
	{
		createOrSaveFile("LIBONE", "SUBR.NSS", """
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE SUBROUTINE MY-SUBROUTINE
			IGNORE
			END-SUBROUTINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			PERFORM MY-SUB${}$
			END
			""")
			.assertContainsCompleting("MY-SUBROUTINE", CompletionItemKind.Event, "MY-SUBROUTINE%n$0".formatted());
	}

	@Test
	void completeDataAreasWhenCompletionIsTriggeredAfterUsingInDefineData()
	{
		createOrSaveFile("LIBONE", "MYLDA.NSL", """
			DEFINE DATA LOCAL
			1 #INLDA (A10)
			END
			""");

		createOrSaveFile("LIBONE", "MYPDA.NSA", """
			DEFINE DATA PARAMETER
			1 #INPDA (A10)
			END
			""");

		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA
			LOCAL USING ${}$
			END-DEFINE
			END
			""")
			.assertContainsCompleting("MYLDA", CompletionItemKind.Struct, "MYLDA")
			.assertContainsCompleting("MYPDA", CompletionItemKind.Struct, "MYPDA");
	}

	@Test
	void completeDataAreasAfterUsingEvenIfThereAlreadyIsAVariableDeclarationInSameLine()
	{
		createOrSaveFile("LIBONE", "MYLDA.NSL", """
			DEFINE DATA LOCAL
			1 #INLDA (A10)
			END
			""");

		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA
			LOCAL USING ${}$ LOCAL 1 #VAR (N2)
			END-DEFINE
			END
			""")
			.assertContainsCompleting("MYLDA", CompletionItemKind.Struct, "MYLDA");
	}

	@Test
	void completeCallnats()
	{
		createOrSaveFile("LIBONE", "SUBN.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			${}$
			END
			""")
			.assertContainsCompleting("SUBN", CompletionItemKind.Class, "CALLNAT 'SUBN'%n$0".formatted());
	}

	@Test
	void completeCallnatsWithParameter()
	{
		createOrSaveFile("LIBONE", "SUBN.NSN", """
			DEFINE DATA
			PARAMETER
			1 #P-PARM1 (A10)
			1 #P-PARM2 (A10)
			END-DEFINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			${}$
			END
			""")
			.assertContainsCompleting("SUBN", CompletionItemKind.Class, "CALLNAT 'SUBN' ${1:#P-PARM1} ${2:#P-PARM2}%n$0".formatted());
	}

	@Test
	void completeCallnatsWhenCallnatIsAlreadyPresent()
	{
		createOrSaveFile("LIBONE", "SUBN.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT ${}$
			END
			""")
			.assertContainsCompleting("SUBN", CompletionItemKind.Class, "'SUBN'%n$0".formatted());
	}

	@Test
	void completeCallnatsWithParameterWhenCallnatIsAlreadyPresent()
	{
		createOrSaveFile("LIBONE", "SUBN.NSN", """
			DEFINE DATA PARAMETER
			1 #P-PARAM (A10)
			END-DEFINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT ${}$
			END
			""")
			.assertContainsCompleting("SUBN", CompletionItemKind.Class, "'SUBN' ${1:#P-PARAM}%n$0".formatted());
	}
}
