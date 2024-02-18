package org.amshove.natls.completion;

import org.eclipse.lsp4j.CompletionItemKind;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PostfixCompletionTests extends CompletionTest
{
	@Nested
	class TheIfSnippetShould
	{
		@Test
		void addTheSnippetWhenInvokedOnAVariable()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 VAR (A10)
				END-DEFINE
				VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("if", """
				DEFINE DATA LOCAL
				1 VAR (A10)
				END-DEFINE
				IF VAR$1
				  ${0:IGNORE}
				END-IF
				END
				""");
		}

		@Test
		void addTheSnippetWhenInvokedOnAPoundedVariable()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #VAR (A10)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("if", """
				DEFINE DATA LOCAL
				1 #VAR (A10)
				END-DEFINE
				IF #VAR$1
				  ${0:IGNORE}
				END-IF
				END
				""");
		}

		@Test
		void addTheSnippetWhenInvokedOnAPoundedQualifiedVariable()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 GRP
				2 #VAR (A10)
				END-DEFINE
				GRP.#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("if", """
				DEFINE DATA LOCAL
				1 GRP
				2 #VAR (A10)
				END-DEFINE
				IF GRP.#VAR$1
				  ${0:IGNORE}
				END-IF
				END
				""");
		}

		@Test
		void addTheSnippetWhenInvokedOnAPoundedQualifiedVariableWithPoundedGroup()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #GRP
				2 #VAR (A10)
				END-DEFINE
				#GRP.#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("if", """
				DEFINE DATA LOCAL
				1 #GRP
				2 #VAR (A10)
				END-DEFINE
				IF #GRP.#VAR$1
				  ${0:IGNORE}
				END-IF
				END
				""");
		}
	}

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
	class TheIfDefaultSnippetShould
	{
		@ParameterizedTest
		@ValueSource(strings =
		{
			"N12", "I4", "P8", "F8", "B4"
		})
		void createADefaultValueCheckForNumerics(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertContainsCompletionResultingIn("ifDefault", """
					DEFINE DATA LOCAL
					1 #VAR (%s)
					END-DEFINE
					IF #VAR = 0
					  ${0:IGNORE}
					END-IF
					END
					""".formatted(type));
		}

		@ParameterizedTest
		@ValueSource(strings =
		{
			"A", "U"
		})
		void createADefaultValueCheckForAlphanumerics(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #VAR (%s10)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertContainsCompletionResultingIn("ifDefault", """
					DEFINE DATA LOCAL
					1 #VAR (%s10)
					END-DEFINE
					IF #VAR = ' '
					  ${0:IGNORE}
					END-IF
					END
					""".formatted(type));
		}

		@Test
		void createADefaultValueCheckForAlphanumericDynamic()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #VAR (A) DYNAMIC
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("ifDefault", """
					DEFINE DATA LOCAL
					1 #VAR (A) DYNAMIC
					END-DEFINE
					IF #VAR = ' '
					  ${0:IGNORE}
					END-IF
					END
					""");
		}

		@Test
		void createADefaultValueCheckForLogicVariables()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #VAR (L)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("ifDefault", """
					DEFINE DATA LOCAL
					1 #VAR (L)
					END-DEFINE
					IF #VAR = FALSE
					  ${0:IGNORE}
					END-IF
					END
					""");
		}

		@ParameterizedTest
		@ValueSource(strings =
		{
			"D", "T"
		})
		void createADefaultValueCheckForTimeRelatedTypes(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertContainsCompletionResultingIn("ifDefault", """
					DEFINE DATA LOCAL
					1 #VAR (%s)
					END-DEFINE
					IF #VAR = 0
					  ${0:IGNORE}
					END-IF
					END
					""".formatted(type));
		}

		@Test
		void createADefaultValueCheckForAllArrayElementsWhenInvokedWithoutArrayAccess()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #VAR (A1/*)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("ifDefault", """
					DEFINE DATA LOCAL
					1 #VAR (A1/*)
					END-DEFINE
					IF #VAR(*) = ' '
					  ${0:IGNORE}
					END-IF
					END
					""");
		}

		@Test
		void notCreateACompletionItemForTypesWithUnknownDefaultValue()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA LOCAL
				1 #VAR (C)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertDoesNotContain("ifDefault");
		}
	}

	@Nested
	class TheIfSpecifiedSnippedShould
	{
		@Test
		void notBeApplicableOnParametersThatAreNotOptional()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				PARAMETER 1 #PARAM (A10)
				END-DEFINE
				#PARAM.${}$
				END
				""")
				.assertDoesNotContain("ifSpecified");
		}

		@Test
		void createAnIfSpecifiedForOptionalParameter()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				PARAMETER 1 #PARAM (A10) OPTIONAL
				END-DEFINE
				#PARAM.${}$
				END
				""")
				.assertContainsCompletionResultingIn("ifSpecified", """
						DEFINE DATA
						PARAMETER 1 #PARAM (A10) OPTIONAL
						END-DEFINE
						IF #PARAM SPECIFIED
						  ${0:IGNORE}
						END-IF
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
	class TheToLowerCaseSnippetShould
	{
		@ParameterizedTest
		@ValueSource(strings =
		{
			"N2", "P4", "I4", "L", "C"
		})
		void notBeApplicableOnVariablesThatAreNotAlphanumericFamily(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertDoesNotContain("toLowerCase");
		}

		@ParameterizedTest
		@ValueSource(strings =
		{
			"A10", "U10", "B4"
		})
		void beApplicableOnVariablesThatAreAlphanumericFamily(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertContains("toLowerCase", CompletionItemKind.Snippet);
		}

		@Test
		void createATranslateLowerCall()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("toLowerCase", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				*TRANSLATE(#VAR, LOWER)
				END
				""");
		}
	}

	@Nested
	class TheToUpperCaseSnippetShould
	{
		@ParameterizedTest
		@ValueSource(strings =
		{
			"N2", "P4", "I4", "L", "C"
		})
		void notBeApplicableOnVariablesThatAreAlphanumericFamily(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertDoesNotContain("toUpperCase");
		}

		@ParameterizedTest
		@ValueSource(strings =
		{
			"A10", "U10", "B4"
		})
		void beApplicableOnVariablesThatAreAlphanumericFamily(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertContains("toUpperCase", CompletionItemKind.Snippet);
		}

		@Test
		void createATranslateUpperCall()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("toUpperCase", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				*TRANSLATE(#VAR, UPPER)
				END
				""");
		}
	}
}
