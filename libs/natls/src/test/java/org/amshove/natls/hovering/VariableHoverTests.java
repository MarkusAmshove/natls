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
LOCAL 1 #MYVAR (A10)
```

*dimensions:*
- 1:*
- 1:5"""
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
}
