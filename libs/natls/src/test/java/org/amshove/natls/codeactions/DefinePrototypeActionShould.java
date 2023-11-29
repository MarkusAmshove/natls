package org.amshove.natls.codeactions;

import org.amshove.natls.refactorings.DefinePrototypeAction;
import org.amshove.natls.testlifecycle.CodeActionTest;
import org.amshove.natls.testlifecycle.LspProjectName;
import org.amshove.natls.testlifecycle.LspTestContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class DefinePrototypeActionShould extends CodeActionTest
{
	@Test
	void beApplicableInFunctions()
	{
		addOthFuncToWorkspace();

		var actions = receiveCodeActions("LIBONE", "FUNC.NS7", """
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA LOCAL
			END-DEFINE
			OTH${}$FUNC(<>)
			END-FUNCTION
			""");

		assertCodeAction(actions, 0)
			.hasTitle("Define Prototype");
	}

	@Test
	void notBeApplicableIfTheCalledFunctionIsNotResolvable()
	{
		var actions = receiveCodeActions("LIBONE", "FUNC.NS7", """
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA LOCAL
			END-DEFINE
			OTH${}$FUNC(<>)
			END-FUNCTION
			""");

		assertThat(actions.codeActions()).isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings =
	{
		"NSN", "NSP", "NSS"
	})
	void notBeApplicableInOtherModulesThanFunctions(String extension)
	{
		addOthFuncToWorkspace();

		var actions = receiveCodeActions("LIBONE", "MOD.%s".formatted(extension), """
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE SUBROUTINE MY-SUB
				OTH${}$FUNC(<>)
			END-SUBROUTINE
			END
			""");

		assertThat(actions.codeActions()).isEmpty();
	}

	@Test
	void addASimplePrototypeWithoutParameter()
	{
		addOthFuncToWorkspace();

		var actions = receiveCodeActions("LIBONE", "FUNC.NS7", """
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA LOCAL
			END-DEFINE
			OTH${}$FUNC(<>)
			END-FUNCTION
			""");

		assertCodeAction(actions, 0)
			.hasTitle("Define Prototype")
			.resultsApplied("""
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE PROTOTYPE OTHFUNC RETURNS (L)
			END-PROTOTYPE
			OTHFUNC(<>)
			END-FUNCTION
			""");
	}

	@Test
	void addAPrototypeWithParameter()
	{
		createOrSaveFile("LIBONE", "OTHFUNC.NS7", """
			DEFINE FUNCTION OTHFUNC
			RETURNS (L)
			DEFINE DATA PARAMETER
			1 #PARM (A10)
			PARAMETER USING APDA
			END-DEFINE
			OTHFUNC := TRUE
			END-FUNCTION
			""");

		var actions = receiveCodeActions("LIBONE", "FUNC.NS7", """
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA LOCAL
			END-DEFINE
			FUNC := OTH${}$FUNC(<>)
			END-FUNCTION
			""");

		assertCodeAction(actions, 0)
			.hasTitle("Define Prototype")
			.resultsApplied("""
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE PROTOTYPE OTHFUNC RETURNS (L)
			  DEFINE DATA
			    PARAMETER 1 #PARM (A10)
			    PARAMETER USING APDA
			  END-DEFINE
			END-PROTOTYPE
			FUNC := OTHFUNC(<>)
			END-FUNCTION
			""");
	}

	@Test
	void addAPrototypeWithParameterGroups()
	{
		createOrSaveFile("LIBONE", "OTHFUNC.NS7", """
			DEFINE FUNCTION OTHFUNC
			RETURNS (L)
			DEFINE DATA PARAMETER
			1 #PARMGRP
			2 #GRPVAR (A10)
			PARAMETER USING APDA
			END-DEFINE
			OTHFUNC := TRUE
			END-FUNCTION
			""");

		var actions = receiveCodeActions("LIBONE", "FUNC.NS7", """
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA LOCAL
			END-DEFINE
			FUNC := OTH${}$FUNC(<>)
			END-FUNCTION
			""");

		assertCodeAction(actions, 0)
			.hasTitle("Define Prototype")
			.resultsApplied("""
			DEFINE FUNCTION FUNC
			RETURNS (L)
			DEFINE DATA LOCAL
			END-DEFINE
			DEFINE PROTOTYPE OTHFUNC RETURNS (L)
			  DEFINE DATA
			    PARAMETER 1 #PARMGRP
			    PARAMETER 2 #GRPVAR (A10)
			    PARAMETER USING APDA
			  END-DEFINE
			END-PROTOTYPE
			FUNC := OTHFUNC(<>)
			END-FUNCTION
			""");
	}

	@Override
	protected ICodeActionProvider getCodeActionUnderTest()
	{
		return new DefinePrototypeAction();
	}

	private static LspTestContext testContext;

	@BeforeAll
	static void setupProject(@LspProjectName("emptyproject") LspTestContext testContext)
	{
		DefinePrototypeActionShould.testContext = testContext;
	}

	@Override
	protected LspTestContext getContext()
	{
		return testContext;
	}

	private void addOthFuncToWorkspace()
	{
		createOrSaveFile("LIBONE", "OTHFUNC.NS7", """
			DEFINE FUNCTION OTHFUNC
			RETURNS (L)
			DEFINE DATA LOCAL
			END-DEFINE
			OTHFUNC := TRUE
			END-FUNCTION
			""");
	}
}
