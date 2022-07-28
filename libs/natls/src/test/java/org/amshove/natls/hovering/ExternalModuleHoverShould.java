package org.amshove.natls.hovering;

import org.junit.jupiter.api.Test;

public class ExternalModuleHoverShould extends HoveringTest
{
	@Test
	void hoverExternalSubprogramsFromCallnat()
	{
		createOrSaveFile("LIBONE", "EXTPROG.NSN", """
			/** Module documentation
			DEFINE DATA
			PARAMETER 1 #APARAM (A10) /* Parameter documentation
			PARAMETER 1 #ANOTHER (N5) /* Second parameter documentation
			END-DEFINE
			""");
		assertHover("""
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'EXT${}$PROG' 'A' 5
			END
			""", """
			**LIBONE.EXTPROG**

			```natural
			/** Module documentation
			```

			*Parameter:*
			```natural
			PARAMETER 1 #APARAM (A10) /* Parameter documentation
			```

			```natural
			PARAMETER 1 #ANOTHER (N5) /* Second parameter documentation
			```

			Hover v2

			""");
	}

	@Test
	void hoverExternalSubroutines()
	{
		createOrSaveFile("LIBONE", "EXTERN.NSS", """
			/* MODULE DOCUMENTATION
			DEFINE DATA
			1 PARAMETER #EXTSUB-PARAM (A10)
			END-DEFINE
			DEFINE SUBROUTINE THE-EXTERNAL-SUB
			IGNORE
			END-SUBROUTINE
			""");

		assertHover("""
			DEFINE DATA
			LOCAL
			END-DEFINE
			
			PERFORM THE-E${}$XTERNAL-SUB 'AAA'
			END
			""", """
			**LIBONE.THE-EXTERNAL-SUB**

			*File: EXTERN*

			```natural
			/* MODULE DOCUMENTATION
			```

			*Parameter:*
			Hover v2

			""");
	}
}
