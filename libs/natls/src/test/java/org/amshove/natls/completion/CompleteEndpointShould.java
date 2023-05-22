package org.amshove.natls.completion;

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
}
