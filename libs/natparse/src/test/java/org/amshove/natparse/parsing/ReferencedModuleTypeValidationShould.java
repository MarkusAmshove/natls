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
		var module = assertFileParsesAs(project.findModule("ALIB", "SUBLDA"), ISubprogram.class);
		assertThat(module.diagnostics())
			.as("INVALID_FILE_TYPE should have been raised, because a LDA is used for PARAMETER USING")
			.anyMatch(d -> d.id().equals(ParserError.INVALID_MODULE_TYPE.id()));
	}

	@Test
	void raiseADiagnosticWhenAGdaIsUsedAsParameterUsing(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		var module = assertFileParsesAs(project.findModule("ALIB", "SUBGDA"), ISubprogram.class);
		assertThat(module.diagnostics())
			.as("INVALID_FILE_TYPE should have been raised, because a GDA is used for PARAMETER USING")
			.anyMatch(d -> d.id().equals(ParserError.INVALID_MODULE_TYPE.id()));
	}

	@Test
	void raiseNoDiagnosticWhenAPdaIsUsedAsParameterUsing(@ProjectName("invalidModuleTypeTests") NaturalProject project)
	{
		var module = assertFileParsesAs(project.findModule("ALIB", "SUBPDA"), ISubprogram.class);
		assertThat(module.diagnostics())
			.as("INVALID_FILE_TYPE should not have been raised, because a PDA is used for PARAMETER USING")
			.noneMatch(d -> d.id().equals(ParserError.INVALID_MODULE_TYPE.id()));
	}
}
