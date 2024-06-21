package org.amshove.natls.hovering;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VariableHoverTests extends HoveringTest
{
	@Test
	void levelOneVariablesShouldBeHoveredCorrectly()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL 1 #MY${}$VAR (A10)
			END-DEFINE
			END
			""",
			"""
```natural
LOCAL 1 #MYVAR (A10)
```"""
		);
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"CONST", "INIT"
	})
	void initAndConstValuesShouldBeIncludedInHover(String keyword)
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL 1 #MY${}$VAR (A10) %s<'ABC'>
			END-DEFINE
			END
			""".formatted(keyword),
			"""
```natural
LOCAL 1 #MYVAR (A10) %s<'ABC'>
```""".formatted(keyword)
		);
	}

	@Test
	void arrayDimensionsShouldBeIncluded()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL 1 #MY${}$VAR (A10/1:*,1:5)
			END-DEFINE
			END
			""",
			"""
```natural
LOCAL 1 #MYVAR (A10/1:*,1:5)
```"""
		);
	}

	@Test
	void dynamicAlphanumericArraysShouldBeFormatted()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL 1 #MY${}$VAR (A/1:*,1:5) DYNAMIC
			END-DEFINE
			END
			""",
			"""
```natural
LOCAL 1 #MYVAR (A/1:*,1:5) DYNAMIC
```"""
		);
	}

	@Test
	void byValueParameterShouldBeFormatted()
	{
		assertHover(
			"""
			DEFINE DATA
			PARAMETER 1 #MY${}$PAR (A10) BY VALUE
			END-DEFINE
			END
			""",
			"""
```natural
PARAMETER 1 #MYPAR (A10) BY VALUE
```"""
		);
	}

	@Test
	void byValueResultParameterShouldBeFormatted()
	{
		assertHover(
			"""
			DEFINE DATA
			PARAMETER 1 #MY${}$PAR (A10) BY VALUE RESULT
			END-DEFINE
			END
			""",
			"""
```natural
PARAMETER 1 #MYPAR (A10) BY VALUE RESULT
```"""
		);
	}

	@Test
	void dynamicByValueParameterShouldBeFormatted()
	{
		assertHover(
			"""
			DEFINE DATA
			PARAMETER 1 #MY${}$PAR (A) DYNAMIC BY VALUE
			END-DEFINE
			END
			""",
			"""
```natural
PARAMETER 1 #MYPAR (A) DYNAMIC BY VALUE
```"""
		);
	}

	@Test
	void dynamicByValueResultParameterShouldBeFormatted()
	{
		assertHover(
			"""
			DEFINE DATA
			PARAMETER 1 #MY${}$PAR (A) DYNAMIC BY VALUE RESULT
			END-DEFINE
			END
			""",
			"""
```natural
PARAMETER 1 #MYPAR (A) DYNAMIC BY VALUE RESULT
```"""
		);
	}

	@Test
	void dynamicByValueResultOptionalParameterShouldBeFormatted()
	{
		assertHover(
			"""
			DEFINE DATA
			PARAMETER 1 #MY${}$PAR (A) DYNAMIC BY VALUE RESULT OPTIONAL
			END-DEFINE
			END
			""",
			"""
```natural
PARAMETER 1 #MYPAR (A) DYNAMIC BY VALUE RESULT OPTIONAL
```"""
		);
	}

	@Test
	void levelOneVariablesShouldBeHoveredCorrectlyEvenWhenHoveringTheReference()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL 1 #MYVAR (A10)
			END-DEFINE
			WRITE #MY${}$VAR
			END
			""",
			"""
```natural
LOCAL 1 #MYVAR (A10)
```"""
		);
	}

	@Test
	void inlineCommentsShouldBeIncluded()
	{
		assertHover(
			"""
				DEFINE DATA
				LOCAL 1 #MY${}$VAR (A10) /* Inline comment
				END-DEFINE
				END
				""",
			"""
				```natural
				LOCAL 1 #MYVAR (A10)
				```

				*context:*
				```natural
				1 #MYVAR /* Inline comment
				```"""
		);
	}

	@Test
	void theUsingDataAreaShouldBeIncludedInTheContext()
	{
		createOrSaveFile("LIBONE", "MYLDA.NSL", """
			DEFINE DATA
			LOCAL 1 #MYVAR (A10)
			END-DEFINE
			""");

		assertHover(
			"""
			DEFINE DATA
			LOCAL USING MYLDA
			END-DEFINE

			WRITE #MY${}$VAR
			END""",
			"""
```natural
LOCAL 1 #MYVAR (A10)
```

*context:*
```natural
LOCAL USING MYLDA
1 #MYVAR
```"""
		);
	}

	@Test
	void commentsOnTheUsingOfDataAreasShouldBeIncludedForGroupMembers()
	{
		createOrSaveFile("LIBONE", "MYLDA.NSL", """
			DEFINE DATA LOCAL
			1 #GRP
			2 #MYVAR (A10)
			END-DEFINE
			""");

		assertHover(
			"""
			DEFINE DATA
			LOCAL USING MYLDA /* My using
			END-DEFINE

			WRITE #MY${}$VAR
			END""",
			"""
```natural
LOCAL 2 #MYVAR (A10)
```

*context:*
```natural
LOCAL USING MYLDA /* My using
1 #GRP
2 #MYVAR
```"""
		);
	}

	@Test
	void allRelevantCommentsEncounteredOnTheWayToTheVariableShouldBeIncluded()
	{
		createOrSaveFile("LIBONE", "MYLDA.NSL", """
			DEFINE DATA LOCAL
			1 #GRP /* Important group
			2 #MYVAR (A10) /* The Variable
			END-DEFINE
			""");

		assertHover(
			"""
			DEFINE DATA
			LOCAL USING MYLDA /* My using
			END-DEFINE

			WRITE #MY${}$VAR
			END""",
			"""
```natural
LOCAL 2 #MYVAR (A10)
```

*context:*
```natural
LOCAL USING MYLDA /* My using
1 #GRP /* Important group
2 #MYVAR /* The Variable
```"""
		);
	}

	@Test
	void theLevelOneVariableShouldBeAddedIfHoveringANestedVariable()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #MYGROUP
			2 #VA${}$RINGROUP (N4)
			END-DEFINE
			END
			""",
			"""
```natural
LOCAL 2 #VARINGROUP (N4)
```

*context:*
```natural
1 #MYGROUP
2 #VARINGROUP
```"""
		);
	}

	@Test
	void hoveringAVariableMultipleLevelsDownShouldShowTheCompleteContext()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL
			1 #MYGROUP /* Comment 1
			  2 #GROUP2(1:*)
			    3 #GROUP3 /* Comment 2
			      4 #GROUP4(1:5) /* Comment 3
				    5 #VAR (A10)
			END-DEFINE
			WRITE #V${}$AR
			END
			""",
			"""
```natural
LOCAL 5 #VAR (A10/1:*,1:5)
```

*context:*
```natural
1 #MYGROUP /* Comment 1
2 #GROUP2 (1:*)
3 #GROUP3 /* Comment 2
4 #GROUP4 (1:5) /* Comment 3
5 #VAR
```"""
		);
	}

	@Test
	void hoverOverVariablesInInputsShouldShowTheHover()
	{
		assertHover(
			"""
			DEFINE DATA
			LOCAL 1 #MYVAR (A10)
			END-DEFINE

			INPUT #MY${}$VAR

			END
			""",
			"""
```natural
LOCAL 1 #MYVAR (A10)
```"""
		);
	}
}
