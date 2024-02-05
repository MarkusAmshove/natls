package org.amshove.natls.completion;

import org.junit.jupiter.api.Test;

class PostfixCompletionShould extends CompletionTest
{
	@Test
	void createAForLoopForArrays()
	{
		assertCompletions("LIBONE", "SUB.NSN", ".", """
            DEFINE DATA LOCAL
            1 ARR (A10/*)
            END-DEFINE
            ARR.${}$
            END
            """)
			.assertContainsCompletionResultingIn("for", """
            DEFINE DATA LOCAL
            1 #S-ARR (I4)
            1 #I-ARR (I4)
            1 ARR (A10/*)
            END-DEFINE
            #S-ARR := *OCC(ARR)
            FOR #I-ARR := 1 TO #S-ARR
              IGNORE
            END-FOR

            END
            """);
	}
}
