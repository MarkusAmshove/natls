package org.amshove.natls.hovering;

import org.junit.jupiter.api.Test;

public class SystemVariableHoverShould extends HoveringTest
{
	@Test
	void provideHoverForSystemVariables()
	{
		assertHover("""
			WRITE *TI${}$MX
			""", """
			```natural
			*TIMX : (T)
			```

			---

			**unmodifiable**

			Returns the current time of the day as builtin time format""");
	}

	@Test
	void hoverAVariableAndIncludeThatTheContentIsNotModifiable()
	{
		assertHoverContains("""
			WRITE *TI${}$MX
			""", "unmodifiable");
	}

	@Test
	void hoverAVariableAndIncludeThatTheContentIsModifiable()
	{
		assertHoverContains("""
			WRITE *ER${}$ROR-NR
			""", "modifiable");
	}
}
