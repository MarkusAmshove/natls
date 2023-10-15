package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.ISubprogram;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("DataFlowIssue")
class TypeCheckerRelocationTests extends ParserIntegrationTest
{
	private NaturalProject project;

	@BeforeEach
	void setUp(@ProjectName("copycodetests") NaturalProject project)
	{
		this.project = project;
	}

	@Test
	void typeCheckingDiagnosticsShouldBeRelocatedToTheInclude()
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "RELOCSUB"), ISubprogram.class);
		assertThat(subprogram.diagnostics()).hasSize(1);

		var diagnostic = subprogram.diagnostics().first();
		assertThat(diagnostic.filePath()).isEqualTo(subprogram.file().getPath());
		assertThat(diagnostic.additionalInfo()).hasSize(1);
		assertThat(diagnostic.additionalInfo().first().position().filePath())
			.isEqualTo(project.findModule("LIBONE", "RELOCCC").getPath());
	}
}
