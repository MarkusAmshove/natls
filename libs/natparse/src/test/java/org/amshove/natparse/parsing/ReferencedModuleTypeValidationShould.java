package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ISubprogram;
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
			project
		);
	}

	@Test
	void raiseADiagnosticWhenAGdaIsUsedAsParameterUsing(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertRaised(
			"SUBGDA",
			"INVALID_FILE_TYPE should have been raised, because a GDA is used for PARAMETER USING",
			project
		);
	}

	@Test
	void raiseNoDiagnosticWhenAPdaIsUsedAsParameterUsing(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertNotRaised(
			"SUBPDA",
			"INVALID_FILE_TYPE should not have been raised, because a PDA is used for PARAMETER USING",
			project
		);
	}

	@Test
	void raiseADiagnosticWhenSomethingDifferentThanACopycodeIsUsedForInclude(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertRaised(
			"INCLSUB",
			"INVALID_FILE_TYPE should have been raised, because a Subprogram is used for INCLUDE",
			project
		);
	}

	@Test
	void raiseNoDiagnosticWhenACopycodeIsUsedAsInclude(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		assertNotRaised(
			"INCLCC",
			"INVALID_FILE_TYPE should not have been raised, because a Copycode is used for INCLUDE",
			project
		);
	}

	private void assertNotRaised(String moduleName, String reason, NaturalProject project)
	{
		var module = assertFileParsesAs(project.findModule("ALIB", moduleName), ISubprogram.class);
		assertThat(module.diagnostics())
			.as(reason)
			.noneMatch(d -> d.id().equals(ParserError.INVALID_MODULE_TYPE.id()));
	}

	private void assertRaised(String moduleName, String reason, NaturalProject project)
	{
		var module = assertFileParsesAs(project.findModule("ALIB", moduleName), ISubprogram.class);
		assertThat(module.diagnostics())
			.as(reason)
			.anyMatch(d -> d.id().equals(ParserError.INVALID_MODULE_TYPE.id()));
	}
}
