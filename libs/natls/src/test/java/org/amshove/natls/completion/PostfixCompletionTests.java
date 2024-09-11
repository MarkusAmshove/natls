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

	@Nested
	class TheValSnippetShould
	{
		@ParameterizedTest
		@ValueSource(
			strings =
			{
				"N2", "P4", "I4", "L", "C"
			}
		)
		void notBeApplicableOnVariablesThatAreNumericFamily(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertDoesNotContain("val");
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
				.assertContains("val", CompletionItemKind.Snippet);
		}

		@Test
		void createAValCall()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL
				1 #VARA (A10)
				1 #VARN (A10)
				END-DEFINE
				#VARN := #VARA.${}$
				END
				""")
				.assertContainsCompletionResultingIn("val", """
				DEFINE DATA
				LOCAL
				1 #VARA (A10)
				1 #VARN (A10)
				END-DEFINE
				#VARN := VAL(#VARA)
				END
				""");
		}
	}

	@Nested
	class TrimSnippetsShould
	{
		@ParameterizedTest
		@ValueSource(
			strings =
			{
				"N2", "P4", "I4", "L", "C"
			}
		)
		void notBeApplicableOnVariablesThatAreAlphanumericFamily(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertDoesNotContain("trim")
				.assertDoesNotContain("trimTrailing")
				.assertDoesNotContain("trimLeading");
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
				.assertContains("trim", CompletionItemKind.Snippet)
				.assertContains("trimTrailing", CompletionItemKind.Snippet)
				.assertContains("trimLeading", CompletionItemKind.Snippet);
		}

		@Test
		void createATrimCall()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("trim", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				*TRIM(#VAR)
				END
				""");
		}

		@Test
		void createATrimLeadingCall()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("trimLeading", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				*TRIM(#VAR, LEADING)
				END
				""");
		}

		@Test
		void createATrimTrailingCall()
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				#VAR.${}$
				END
				""")
				.assertContainsCompletionResultingIn("trimTrailing", """
				DEFINE DATA
				LOCAL 1 #VAR (A10)
				END-DEFINE
				*TRIM(#VAR, TRAILING)
				END
				""");
		}
	}

	@Nested
	class NumericOperationSnippetsShould
	{
		@ParameterizedTest
		@ValueSource(
			strings =
			{
				"N2", "P4", "I4"
			}
		)
		void containIncrementSnippetForNumericTypes(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertContainsCompletionResultingIn("increment", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				ADD 1 TO #VAR
				END
				""".formatted(type));
		}

		@ParameterizedTest
		@ValueSource(
			strings =
			{
				"N2", "P4", "I4"
			}
		)
		void containDecrementSnippetForNumericTypes(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertContainsCompletionResultingIn("decrement", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				SUBTRACT 1 FROM #VAR
				END
				""".formatted(type));
		}

		@ParameterizedTest
		@ValueSource(
			strings =
			{
				"A4", "B4", "U4", "L", "C", "T", "D"
			}
		)
		void notContainIncrementSnippetForNonNumericTypes(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertDoesNotContain("increment");
		}

		@ParameterizedTest
		@ValueSource(
			strings =
			{
				"A4", "B4", "U4", "L", "C", "T", "D"
			}
		)
		void notContainDecrementSnippetForNonNumericTypes(String type)
		{
			assertCompletions("LIBONE", "SUB.NSN", ".", """
				DEFINE DATA
				LOCAL 1 #VAR (%s)
				END-DEFINE
				#VAR.${}$
				END
				""".formatted(type))
				.assertDoesNotContain("decrement");
		}
	}
}
