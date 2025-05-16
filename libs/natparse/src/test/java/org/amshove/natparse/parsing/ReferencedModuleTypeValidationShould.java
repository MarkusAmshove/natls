package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class ReferencedModuleTypeValidationShould extends ParserIntegrationTest
{
	@Test
	void raiseADiagnosticWhenALdaIsUsedAsParameterUsing(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertRaised(
			"SUBLDA",
			"INVALID_FILE_TYPE should have been raised, because a LDA is used for PARAMETER USING",
			project,
			ISubprogram.class
		);
	}

	@Test
	void raiseADiagnosticWhenAGdaIsUsedAsParameterUsing(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertRaised(
			"SUBGDA",
			"INVALID_FILE_TYPE should have been raised, because a GDA is used for PARAMETER USING",
			project,
			ISubprogram.class
		);
	}

	@Test
	void raiseNoDiagnosticWhenAPdaIsUsedAsParameterUsing(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertNotRaised(
			"SUBPDA",
			"INVALID_FILE_TYPE should not have been raised, because a PDA is used for PARAMETER USING",
			project,
			ISubprogram.class
		);
	}

	@Test
	void raiseADiagnosticIfAGdaIsUsedAsLocalUsing(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertRaised(
			"LUSEGDA",
			"INVALID_FILE_TYPE should have been raised, because a GDA is used for LOCAL USING",
			project,
			ISubprogram.class
		);
	}

	@Test
	void raiseNoDiagnosticIfALdaIsUsedAsLocalUsing(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertNotRaised(
			"LUSELDA",
			"INVALID_FILE_TYPE should not have been raised, because a LDA is used for LOCAL USING",
			project,
			ISubprogram.class
		);
	}

	@Test
	void raiseNoDiagnosticIfAPdaIsUsedAsLocalUsing(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertNotRaised(
			"LUSEPDA",
			"INVALID_FILE_TYPE should not have been raised, because a PDA is used for LOCAL USING",
			project,
			ISubprogram.class
		);
	}

	@Test
	void raiseADiagnosticWhenSomethingDifferentThanACopycodeIsUsedForInclude(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertRaised(
			"INCLSUB",
			"INVALID_FILE_TYPE should have been raised, because a Subprogram is used for INCLUDE",
			project,
			ISubprogram.class
		);
	}

	@Test
	void raiseNoDiagnosticWhenACopycodeIsUsedAsInclude(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertNotRaised(
			"INCLCC",
			"INVALID_FILE_TYPE should not have been raised, because a Copycode is used for INCLUDE",
			project,
			ISubprogram.class
		);
	}

	@Test
	void raiseADiagnosticWhenAProgramIsCalledWithCallnat(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertRaised(
			"CALLPROG",
			"INVALID_FILE_TYPE should have been raised, because a Program is called by CALLNAT",
			project,
			ISubprogram.class
		);
	}

	@Test
	void raiseNoDiagnosticWhenASubProgramIsCalledWithCallnat(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertNotRaised(
			"CALLSUB",
			"INVALID_FILE_TYPE should not have been raised, because a Subprogram is called by CALLNAT",
			project,
			ISubprogram.class
		);
	}

	@Test
	void raiseADiagnosticWhenASubProgramIsCalledWithFetch(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertRaised(
			"FETCHSUB",
			"INVALID_FILE_TYPE should have been raised, because a Subprogram is called by FETCH",
			project,
			IProgram.class
		);
	}

	@Test
	void raiseNoDiagnosticWhenAProgramIsCalledWithFetch(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertNotRaised(
			"FETCHPRG",
			"INVALID_FILE_TYPE should not have been raised, because a Program is called by FETCH",
			project,
			IProgram.class
		);
	}

	private void assertNotRaised(String moduleName, String reason, NaturalProject project, Class<? extends INaturalModule> typeOfCallingModule)
	{
		var module = assertFileParsesAs(project.findModule("ALIB", moduleName), typeOfCallingModule);
		assertThat(module.diagnostics())
			.as(reason)
			.noneMatch(d -> d.id().equals(ParserError.INVALID_MODULE_TYPE.id()));
	}

	private void assertRaised(String moduleName, String reason, NaturalProject project, Class<? extends INaturalModule> typeOfCallingModule)
	{
		var module = assertFileParsesAs(project.findModule("ALIB", moduleName), typeOfCallingModule);
		assertThat(module.diagnostics())
			.as(reason)
			.anyMatch(d -> d.id().equals(ParserError.INVALID_MODULE_TYPE.id()));
	}
}
