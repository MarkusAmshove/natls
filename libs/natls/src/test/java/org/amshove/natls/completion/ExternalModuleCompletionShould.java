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
	void onlyContainSubroutinesWhenInvokedAfterPerform()
	{
		createOrSaveFile("LIBONE", "SUBS1.NSS", """
			DEFINE DATA PARAMETER
			1 #P-PARAM (A10)
			END-DEFINE
			DEFINE SUBROUTINE EXT-SUBROUTINE-1
			IGNORE
			END-SUBROUTINE
			END
			""");
		createOrSaveFile("LIBONE", "SUBS2.NSS", """
			DEFINE DATA PARAMETER
			1 #P-PARAM (A10)
			END-DEFINE
			DEFINE SUBROUTINE EXT-SUBROUTINE-2
			IGNORE
			END-SUBROUTINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			1 #PRESENT-VAR (A10)
			END-DEFINE
			DEFINE SUBROUTINE LOCAL-SUB
			IGNORE
			END-SUBROUTINE

			PERFORM ${}$
			END
			""")
			.assertContains("EXT-SUBROUTINE-1", CompletionItemKind.Event)
			.assertContains("EXT-SUBROUTINE-2", CompletionItemKind.Event)
			.assertContains("LOCAL-SUB", CompletionItemKind.Method)
			.assertContainsOnlyKinds(CompletionItemKind.Method, CompletionItemKind.Event);
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
	void completeParameterOfExternalSubroutinesWhenPerformAndSubroutineNameIsPresent()
	{
		createOrSaveFile("LIBONE", "SUBR.NSS", """
			DEFINE DATA PARAMETER
			1 #P-PARM (A10)
			END-DEFINE
			DEFINE SUBROUTINE MY-SUBROUTINE
			IGNORE
			END-SUBROUTINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE
			PERFORM MY-SUB ${}$
			END
			""")
			.assertContains("#VAR :(A10) (SUB2)", CompletionItemKind.Variable);
	}

	@Test
	void completeParameterOfCallnatsWhenCallnatAndModuleNameArePresent()
	{
		createOrSaveFile("LIBONE", "SUBN.NSN", """
			DEFINE DATA PARAMETER
			1 #P-PARM (A10)
			END-DEFINE
			IGNORE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE
			CALLNAT 'SUBN' ${}$
			END
			""")
			.assertContains("#VAR :(A10) (SUB2)", CompletionItemKind.Variable);
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
	void completeDataAreasWhenCompletionIsTriggeredAfterUsingInDefineDataWithSomePartsOfTheNamePresent()
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
			LOCAL USING MY${}$
			END-DEFINE
			END
			""")
			.assertContainsCompleting("MYLDA", CompletionItemKind.Struct, "MYLDA")
			.assertContainsCompleting("MYPDA", CompletionItemKind.Struct, "MYPDA");
	}

	@Test
	void onlyContainDataAreasWhenTriggeredToContainDataAreas()
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
			LOCAL USING MY${}$
			LOCAL
			1 #PRESENT-VAR (A10)
			END-DEFINE
			DEFINE SUBROUTINE PRESENT-SUB
			IGNORE
			END-SUBROUTINE
			END
			""")
			.assertContainsOnlyKinds(CompletionItemKind.Struct);
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
	void completeCallnatsWhenStringIsAlreadyPresent()
	{
		createOrSaveFile("LIBONE", "SUBN.NSN", """
			DEFINE DATA PARAMETER
			1 #PARAM (A10)
			END-DEFINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT '${}$'
			END
			""")
			.assertContainsCompleting("SUBN", CompletionItemKind.Class, "SUBN' ${1:#PARAM}%n$0".formatted());
	}

	@Test
	void completeCallnatsWhenStringIsAlreadyPartlyPresent()
	{
		createOrSaveFile("LIBONE", "SUBN.NSN", """
			DEFINE DATA PARAMETER
			1 #PARAM (A10)
			END-DEFINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT '${}$
			END
			""")
			.assertContainsCompleting("SUBN", CompletionItemKind.Class, "SUBN' ${1:#PARAM}%n$0".formatted());
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

	@Test
	void onlyContainCallnatsWhenInvokedAfterCallnat()
	{
		createOrSaveFile("LIBONE", "SUBN.NSN", """
			DEFINE DATA PARAMETER
			1 #P-PARAM (A10)
			END-DEFINE
			END
			""");
		createOrSaveFile("LIBONE", "SUBN2.NSN", """
			DEFINE DATA PARAMETER
			1 #P-PARAM (A10)
			END-DEFINE
			END
			""");

		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			1 #PRESENT-VAR (A10)
			END-DEFINE
			DEFINE SUBROUTINE PRESENT-SUB
			IGNORE
			END-DEFINE
			CALLNAT ${}$
			END
			""")
			.assertContains("SUBN", CompletionItemKind.Class)
			.assertContains("SUBN2", CompletionItemKind.Class)
			.assertContainsOnlyKinds(CompletionItemKind.Class);
	}
	
	@Test
	void onlyContainCopycodesWhenInvokedAfterInclude()
	{
		createOrSaveFile("LIBONE", "SUBN.NSN", """
			DEFINE DATA PARAMETER
			1 #P-PARAM (A10)
			END-DEFINE
			END
			""");
		createOrSaveFile("LIBONE", "CPYC.NSC", """
			WRITE "Copycodes are just compiled verbatim into your program"
			""");
		
		assertCompletions("LIBONE", "PROG1.NSP", """
			DEFINE DATA LOCAL
			01 #LANGUAGE (A7)
			END-DEFINE
			IGNORE
			INCLUDE ${}$
			END
			""")
			.assertContains("CPYC", CompletionItemKind.Module)
			.assertContainsOnlyKinds(CompletionItemKind.Module);
	}
}
