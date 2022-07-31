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
			PARAMETER
			1 #APARAM (A10) /* Parameter documentation
			1 #ANOTHER (N5) /* Second parameter documentation
			1 #OPTIONAL (N5) OPTIONAL /* Optional parameter documentation
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
			PARAMETER 1 #ANOTHER (N5) /* Second parameter documentation
			PARAMETER 1 #OPTIONAL (N5) OPTIONAL /* Optional parameter documentation
			```""");
	}

	@Test
	void hoverExternalSubroutines()
	{
		createOrSaveFile("LIBONE", "EXTERN.NSS", """
			/* MODULE DOCUMENTATION
			DEFINE DATA
			PARAMETER 1 #EXTSUB-PARAM (A10) /* Parameter documentation
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
			```natural
			PARAMETER 1 #EXTSUB-PARAM (A10) /* Parameter documentation
			```""");
	}

	@Test
	void includeTheParameterInOrderOfDeclaration()
	{
		createOrSaveFile("LIBONE", "EXTPROG.NSN", """
			DEFINE DATA
			PARAMETER USING FIRSTPDA
			PARAMETER
			1 #APARAM (A10)
			PARAMETER USING SECONDPDA
			PARAMETER
			1 #ANOTHER (N5)
			1 #OPTIONAL (N5) OPTIONAL
			END-DEFINE
			""");

		assertHover("""
			DEFINE DATA LOCAL
			END-DEFINE
			CALLNAT 'EXT${}$PROG' 'A' 5
			END
			""", """
			**LIBONE.EXTPROG**

			*Parameter:*
			```natural
			PARAMETER USING FIRSTPDA
			PARAMETER 1 #APARAM (A10)
			PARAMETER USING SECONDPDA
			PARAMETER 1 #ANOTHER (N5)
			PARAMETER 1 #OPTIONAL (N5) OPTIONAL
			```""");
	}
}
