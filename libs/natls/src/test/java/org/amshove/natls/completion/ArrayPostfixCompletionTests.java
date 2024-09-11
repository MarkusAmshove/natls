package org.amshove.natls.completion;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ArrayPostfixCompletionTests extends CompletionTest
{
	@Nested
	class TheForSnippetShould
	{
		@ParameterizedTest
		@ValueSource(strings =
		{
			"A10", "N5", "L", "C"
		})
		void notBeApplicableWhenNotInvokedOnAnArray(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type)).assertDoesNotContain("for");
		}

		@Test
		void createAForLoopWhenInvokedOnAnArray()
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
					  ${0:IGNORE}
					END-FOR
					END
					""");
		}

		@Test
		void notCreateIteratorVariablesIfTheyAlreadyExist()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 ARR (A10/*)
				1 #S-ARR (I4)
				1 #I-ARR (I4)
				END-DEFINE
				ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("for", """
					DEFINE DATA LOCAL
					1 ARR (A10/*)
					1 #S-ARR (I4)
					1 #I-ARR (I4)
					END-DEFINE
					#S-ARR := *OCC(ARR)
					FOR #I-ARR := 1 TO #S-ARR
					  ${0:IGNORE}
					END-FOR
					END
					""");
		}

		@Test
		void createAForLoopWhenInvokedOnAQualifiedArray()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 GRP
				2 ARR (A10/*)
				END-DEFINE
				GRP.ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("for", """
					DEFINE DATA LOCAL
					1 #S-GRP-ARR (I4)
					1 #I-GRP-ARR (I4)
					1 GRP
					2 ARR (A10/*)
					END-DEFINE
					#S-GRP-ARR := *OCC(GRP.ARR)
					FOR #I-GRP-ARR := 1 TO #S-GRP-ARR
					  ${0:IGNORE}
					END-FOR
					END
					""");
		}

		@Test
		void createAForLoopWhenInvokedOnAnArrayWithPoundName()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #ARR (A10/*)
				END-DEFINE
				#ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("for", """
					DEFINE DATA LOCAL
					1 #S-#ARR (I4)
					1 #I-#ARR (I4)
					1 #ARR (A10/*)
					END-DEFINE
					#S-#ARR := *OCC(#ARR)
					FOR #I-#ARR := 1 TO #S-#ARR
					  ${0:IGNORE}
					END-FOR
					END
					""");
		}

		@Test
		void createAForLoopWhenInvokedOnAQualifiedArrayWithVarPound()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 GRP
				2 #ARR (A10/*)
				END-DEFINE
				GRP.#ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("for", """
					DEFINE DATA LOCAL
					1 #S-GRP-#ARR (I4)
					1 #I-GRP-#ARR (I4)
					1 GRP
					2 #ARR (A10/*)
					END-DEFINE
					#S-GRP-#ARR := *OCC(GRP.#ARR)
					FOR #I-GRP-#ARR := 1 TO #S-GRP-#ARR
					  ${0:IGNORE}
					END-FOR
					END
					""");
		}

		@Test
		void createAForLoopWhenInvokedOnAQualifiedArrayWithGroupPound()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #GRP
				2 ARR (A10/*)
				END-DEFINE
				#GRP.ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("for", """
					DEFINE DATA LOCAL
					1 #S-#GRP-ARR (I4)
					1 #I-#GRP-ARR (I4)
					1 #GRP
					2 ARR (A10/*)
					END-DEFINE
					#S-#GRP-ARR := *OCC(#GRP.ARR)
					FOR #I-#GRP-ARR := 1 TO #S-#GRP-ARR
					  ${0:IGNORE}
					END-FOR
					END
					""");
		}

		@Test
		void createAForLoopWhenInvokedOnAQualifiedArrayWithBothPound()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #GRP
				2 #ARR (A10/*)
				END-DEFINE
				#GRP.#ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("for", """
					DEFINE DATA LOCAL
					1 #S-#GRP-#ARR (I4)
					1 #I-#GRP-#ARR (I4)
					1 #GRP
					2 #ARR (A10/*)
					END-DEFINE
					#S-#GRP-#ARR := *OCC(#GRP.#ARR)
					FOR #I-#GRP-#ARR := 1 TO #S-#GRP-#ARR
					  ${0:IGNORE}
					END-FOR
					END
					""");
		}

		@Test
		void createAForLoopWhenInvokedOnAGroupArrayAndUseAChildVariable()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #GRP (1:*)
				2 #ARR (A10)
				END-DEFINE
				#GRP.${}$
				END
				""")
				.assertContainsCompletionResultingIn("for", """
					DEFINE DATA LOCAL
					1 #S-#GRP (I4)
					1 #I-#GRP (I4)
					1 #GRP (1:*)
					2 #ARR (A10)
					END-DEFINE
					#S-#GRP := *OCC(#GRP.#ARR)
					FOR #I-#GRP := 1 TO #S-#GRP
					  ${0:IGNORE}
					END-FOR
					END
					""");
		}
	}

	@Nested
	class TheOccSnippetShould
	{
		@Test
		void notBeApplicableOnVariablesThatAreNotArrays()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertDoesNotContain("occ");
		}

		@Test
		void createAnOccInvocationForArrays()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (A10/*)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("occ", """
					DEFINE DATA
					LOCAL 1 #VAR (A10/*)
					END-DEFINE
					*OCC(#VAR)
					END
					""");
		}

		@Test
		void createAnOccInvocationForArraysWhenInvokedOnGroup()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL
				1 GRP (1:*)
				2 #VAR (A10)
				END-DEFINE
				GRP.${}$
				END
				""")
				.assertContainsCompletionResultingIn("occ", """
					DEFINE DATA
					LOCAL
					1 GRP (1:*)
					2 #VAR (A10)
					END-DEFINE
					*OCC(GRP.#VAR)
					END
					""");
		}
	}

	@Nested
	class CollectionMatchExpressionsShould
	{
		@Test
		void createAContainsExpression()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #ARR (A1/*)
				END-DEFINE
				IF #ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("contains", """
				DEFINE DATA LOCAL
				1 #ARR (A1/*)
				END-DEFINE
				IF #ARR(*) = ' '
				END
				""");
		}

		@Test
		void createAContainsExpressionForQualifiedReferences()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 GRP
				2 #ARR (A1/*)
				END-DEFINE
				IF GRP.#ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("contains", """
				DEFINE DATA LOCAL
				1 GRP
				2 #ARR (A1/*)
				END-DEFINE
				IF GRP.#ARR(*) = ' '
				END
				""");
		}

		@Test
		void createANoneIsExpression()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #ARR (A1/*)
				END-DEFINE
				IF #ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("noneIs", """
				DEFINE DATA LOCAL
				1 #ARR (A1/*)
				END-DEFINE
				IF NOT #ARR(*) = ' '
				END
				""");
		}

		@Test
		void createAnAnyIsNotExpression()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #ARR (A1/*)
				END-DEFINE
				IF #ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("anyIsNot", """
				DEFINE DATA LOCAL
				1 #ARR (A1/*)
				END-DEFINE
				IF #ARR(*) <> ' '
				END
				""");
		}

		@Test
		void createAnAllAreExpression()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #ARR (A1/*)
				END-DEFINE
				IF #ARR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("allAre", """
				DEFINE DATA LOCAL
				1 #ARR (A1/*)
				END-DEFINE
				IF NOT #ARR(*) <> ' '
				END
				""");
		}
	}
}
