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
	void containASubroutine()
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
			.assertContains("MY-SUB", CompletionItemKind.Method);
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
}
