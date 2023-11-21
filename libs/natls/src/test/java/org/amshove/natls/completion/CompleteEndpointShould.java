package org.amshove.natls.completion;

import org.amshove.natls.config.LSConfiguration;
import org.eclipse.lsp4j.CompletionItemKind;
import org.junit.jupiter.api.Test;

class CompleteEndpointShould extends CompletionTest
{
	@Test
	void containALocalVariable()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #VAR (A10)
			END-DEFINE

			${}$
			END
			""")
			.assertContainsVariable("#VAR :(A10) (SUB)");
	}

	@Test
	void containAnInlineSubroutine()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE

			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE

			${}$
			END
			""")
			.assertContainsCompleting("MY-SUB", CompletionItemKind.Method, "PERFORM MY-SUB");
	}

	@Test
	void containAnInlineSubroutineWithoutPerformIfPerformKeywordIsAlreadyPresent()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE

			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE

			PERFORM ${}$
			END
			""")
			.assertContainsCompleting("MY-SUB", CompletionItemKind.Method, "MY-SUB");
	}

	@Test
	void containAnInlineSubroutineWithoutPerformIfPerformKeywordIsAlreadyPresentAndSomePartsOfTheSubroutineNameIsPresent()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE

			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE

			PERFORM MY-${}$
			END
			""")
			.assertContainsCompleting("MY-SUB", CompletionItemKind.Method, "MY-SUB");
	}

	@Test
	void containAnInlineSubroutineWithPerformEvenIfThePreviousLineHasACompletePerformStatement()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE

			DEFINE SUBROUTINE MY-SUB
			IGNORE
			END-SUBROUTINE

			PERFORM MY-SUB
			${}$
			END
			""")
			.assertContainsCompleting("MY-SUB", CompletionItemKind.Method, "PERFORM MY-SUB");
	}

	@Test
	void containsSystemVariables()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE

			${}$
			END
			""")
			.assertContainsVariable("*PID :(A32)");
	}

	@Test
	void containsSystemFunctions()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			END-DEFINE

			${}$
			END
			""")
			.assertContains("*OCC :(I4)", CompletionItemKind.Function);
	}

	@Test
	void completeSystemVariablesIfAsteriskIsPresent()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #VAR (I4)
			END-DEFINE

			#VAR := *${}$
			END
			""")
			.assertContainsCompleting("*PID :(A32)", CompletionItemKind.Variable, "PID");
	}

	@Test
	void completeSystemFunctionsWithAsteriskPresent()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #OCC (I4)
			END-DEFINE

			#OCC := *${}$
			END
			""")
			.assertContainsCompleting("*OCC :(I4)", CompletionItemKind.Function, "OCC($1)$0");
	}

	@Test
	void completeSystemVariablesIfAsteriskAndPartOfNameIsPresent()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #VAR (I4)
			END-DEFINE

			#VAR := *PI${}$
			END
			""")
			.assertContainsCompleting("*PID :(A32)", CompletionItemKind.Variable, "PID");
	}

	@Test
	void completeSystemFunctionsWithAsteriskAndPartOfNamePresent()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #OCC (I4)
			END-DEFINE

			#OCC := *OC${}$
			END
			""")
			.assertContainsCompleting("*OCC :(I4)", CompletionItemKind.Function, "OCC($1)$0");
	}

	@Test
	void completeArrayAccessWithIndexSnippet()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA LOCAL
			1 #ARR (A10/*)
			END-DEFINE

			${}$
			END
			""")
			.assertContainsVariableCompleting("#ARR :(A10/1:*) (SUB)", "#ARR($1)$0");
	}

	@Test
	void onlyUseQualifiedVariableIfExpectedByConfig()
	{
		var config = LSConfiguration.createDefault();
		config.getCompletion().setQualify(true);

		configureLSConfig(config);
		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			1 #GRP
			2 #VAR (A1)
			END-DEFINE

			${}$
			END
			""")
			.assertContainsVariableCompleting("#GRP.#VAR :(A1) (SUB2)", "#GRP.#VAR");
	}

	@Test
	void notShowQualifiedVariablesThatDontNeedToBeQualifiedWhenTurnedOff()
	{
		var config = LSConfiguration.createDefault();
		config.getCompletion().setQualify(false);

		configureLSConfig(config);
		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			1 #GRP
			2 #VAR (A1)
			END-DEFINE

			${}$
			END
			""")
			.assertContainsVariableCompleting("#VAR :(A1) (SUB2)", "#VAR");
	}

	@Test
	void alwaysQualifyVariablesIfQualificationIsNeededEvenIfConfiguredToPreferNoQualification()
	{
		var config = LSConfiguration.createDefault();
		config.getCompletion().setQualify(false);

		configureLSConfig(config);
		assertCompletions("LIBONE", "SUB2.NSN", """
			DEFINE DATA LOCAL
			1 #GRP
			2 #VAR (A1)
			1 #GRP2
			2 #VAR (A1)
			END-DEFINE

			${}$
			END
			""")
			.assertContainsVariableCompleting("#GRP.#VAR :(A1) (SUB2)", "#GRP.#VAR")
			.assertContainsVariableCompleting("#GRP2.#VAR :(A1) (SUB2)", "#GRP2.#VAR");
	}

	@Test
	void filterVariableCompletionBasedOnTypedQualification()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA
			LOCAL
			1 #GRP1
			2 #VAR (A10)
			1 #GRP2
			2 #VAR (A10)
			END-DEFINE
			WRITE #GRP1.${}$
			END
			""")
			.assertDoesNotContainVariable("#GRP2.#VAR")
			.assertContainsVariableCompleting("#VAR :(A10) (SUB)", "#VAR");
	}

	@Test
	void filterVariableCompletionBasedOnTypedQualificationWhenVariablesAreNotAmbiguous()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA
			LOCAL
			1 #GRP1
			2 #VARIABLE (A10)
			END-DEFINE
			WRITE #GRP1.${}$
			END
			""")
			.assertContainsVariableCompleting("#VARIABLE :(A10) (SUB)", "#VARIABLE");
	}

	@Test
	void filterVariableCompletionBasedOnTypedQualificationOnTriggerChar()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA
			LOCAL
			1 #GRP1
			2 #VAR (A10)
			1 #GRP2
			2 #VAR (A10)
			END-DEFINE
			WRITE #GRP1.${}$
			END
			""")
			.assertDoesNotContainVariable("#GRP2.#VAR")
			.assertContainsVariableCompleting("#VAR :(A10) (SUB)", "#VAR");
	}

	@Test
	void filterVariableCompletionBasedOnTypedQualificationOnTriggerCharWhenVariablesAreNotAmbiguous()
	{
		assertCompletions("LIBONE", "SUB.NSN", """
			DEFINE DATA
			LOCAL
			1 #GRP1
			2 #VARIABLE (A10)
			END-DEFINE
			WRITE #GRP1.${}$
			END
			""")
			.assertContainsVariableCompleting("#VARIABLE :(A10) (SUB)", "#VARIABLE");
	}
}
