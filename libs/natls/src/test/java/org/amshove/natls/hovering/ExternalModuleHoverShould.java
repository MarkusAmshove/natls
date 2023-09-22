package org.amshove.natls.hovering;

import org.junit.jupiter.api.Test;

class ExternalModuleHoverShould extends HoveringTest
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

	@Test
	void hoverFunctions()
	{
		createOrSaveFile("LIBONE", "FUNC.NS7", """
			/* MODULE DOCUMENTATION
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA
			PARAMETER
			1 P-PARAM (A10) BY VALUE
			END-DEFINE
			FUNC := TRUE
			END-FUNCTION
			""");

		assertHover("""
			DEFINE DATA
			LOCAL
			END-DEFINE
			
			IF F${}$UNC(<'A'>)
			IGNORE
			END-IF
			
			END
			""", """
			**LIBONE.FUNC**

			```natural
			/* MODULE DOCUMENTATION
			```

			*Result:*
			```natural
			RETURNS (L1)
			```




			*Parameter:*
			```natural
			PARAMETER 1 P-PARAM (A10)
			```""");
	}

	@Test
	void hoverThePassedParameterInsteadOfTheModuleWhenHoveringAParameter()
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
			1 #LOCALVAR (A10)
			END-DEFINE

			PERFORM THE-EXTERNAL-SUB #LOCAL${}$VAR
			END
			""", """
			```natural
			LOCAL 1 #LOCALVAR (A10)
			```""");
	}
}
