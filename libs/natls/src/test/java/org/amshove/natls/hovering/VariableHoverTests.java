package org.amshove.natls.hovering;

import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class VariableHoverTests extends HoveringTest
{
	@Test
	void levelOneVariablesShouldBeHoveredCorrectly()
	{
		assertHover("""
			DEFINE DATA
			LOCAL 1 #MY${}$VAR (A10)
			END-DEFINE
			END
			""",
			"""
```natural
LOCAL 1 #MYVAR (A10)
```

""");
	}

	@Test
	void levelOneVariablesShouldBeHoveredCorrectlyEvenWhenHoveringTheReference()
	{
		assertHover("""
			DEFINE DATA
			LOCAL 1 #MYVAR (A10)
			END-DEFINE
			WRITE #MY${}$VAR
			END
			""",
			"""
```natural
LOCAL 1 #MYVAR (A10)
```

""");
	}

	@Test
	void inlineCommentsShouldBeIncluded()
	{
		assertHover("""
				DEFINE DATA
				LOCAL 1 #MY${}$VAR (A10) /* Inline comment
				END-DEFINE
				END
				""",
			"""
				```natural
				LOCAL 1 #MYVAR (A10)
				```

				*comment:*
				```natural
				/* Inline comment
				```

				""");
	}

	@Test
	void theSourceFileOfTheVariableShouldBeAddedIfItDiffersFromTheHoveringFile()
	{
		createOrSaveFile("LIBONE", "MYLDA.NSL", """
			DEFINE DATA
			LOCAL 1 #MYVAR (A10)
			END-DEFINE
			""");

		assertHover("""
			DEFINE DATA
			LOCAL USING MYLDA
			END-DEFINE
			
			WRITE #MY${}$VAR
			END
			""",
			"""
```natural
LOCAL 1 #MYVAR (A10)
```

*source:*

- LIBONE.MYLDA""");
	}

	@Test
	void theLevelOneVariableShouldBeAddedIfHoveringANestedVariable()
	{
		assertHover("""
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

*member of:*

```natural
LOCAL 1 #MYGROUP
```

""");
	}
}
