package org.amshove.natparse.parsing;

import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CopyCodesShould extends ParserIntegrationTest
{
	@Test
	void notReportDiagnosticsForUnresolvedReferences(@ProjectName("copycodetests") NaturalProject project)
	{
		assertParsesWithoutAnyDiagnostics(project.findModule("LIBONE", "NODIAG"));
	}

	@Test
	void exportDeclaredSubroutinesUpToTheIncludingModule(@ProjectName("copycodetests") NaturalProject project)
	{
		var module = assertParsesWithoutAnyDiagnostics(project.findModule("LIBONE", "USEDECL"));
		assertThat(module.referencableNodes()).anyMatch(n -> n instanceof ISubroutineNode subroutine && subroutine.declaration().symbolName().equals("INSIDE-CCODE"));
	}

	@Test
	void relocateDiagnosticsFromCopyCodesToTheirIncludeStatement(@ProjectName("copycodetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "SUBPROG"), ISubprogram.class);
		assertThat(subprogram.diagnostics()).hasSize(2);
		for (var diagnostic : subprogram.diagnostics())
		{
			assertThat(diagnostic.line()).as("Line mismatch for: " + diagnostic.message()).isEqualTo(6);
			assertThat(diagnostic.offsetInLine()).isEqualTo(8);
		}
	}

	@Test
	void relocateDiagnosticsFromDeeplyNestedCopyCodesToTheirIncludeStatement(@ProjectName("copycodetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "DANEST"), ISubprogram.class);
		assertThat(subprogram.diagnostics()).hasSize(2);
		for (var diagnostic : subprogram.diagnostics())
		{
			assertThat(diagnostic.line()).as("Line mismatch for: " + diagnostic.message()).isEqualTo(8);
			assertThat(diagnostic.offsetInLine()).isEqualTo(8);
		}
	}

	@Test
	void notReportDiagnosticsForCopycodeParameterThatAreQualified(@ProjectName("copycodetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "USEQVAR"), ISubprogram.class);
		assertThat(subprogram.diagnostics()).hasSize(0);
	}

	@Test
	void relocateDiagnosticLocationsForCopyCodeNodesThatAreNestedMultipleTimes(@ProjectName("copycodetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "SUBPROG2"), ISubprogram.class);
		assertThat(subprogram.diagnostics()).hasSize(2);
		for (var diagnostic : subprogram.diagnostics())
		{
			assertThat(diagnostic.line()).as("Line mismatch for: " + diagnostic.message()).isEqualTo(6);
			assertThat(diagnostic.offsetInLine()).isEqualTo(8);
		}
	}

	@Test
	void correctlyIncludeStringLiterals(@ProjectName("copycodetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "INCLSTR"), ISubprogram.class);
		var include = (IIncludeNode) subprogram.body().statements().first();
		var write = (IWriteNode) include.body().statements().first();
		var operand = write.operands().first().operand();
		assertThat(((ILiteralNode) operand).token().source()).isEqualTo("\"\"\"Text\"\"\"");
	}

	@Test
	void correctlyIncludeStringConcatLiterals(@ProjectName("copycodetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "INCLMSTR"), ISubprogram.class);
		var include = (IIncludeNode) subprogram.body().statements().first();
		var write = (IWriteNode) include.body().statements().first();

		var firstOperand = write.operands().first().operand();
		assertThat(((VariableReferenceNode) firstOperand).token().source()).isEqualTo("#VAR1");

		var secondOperand = write.operands().last().operand();
		assertThat(((VariableReferenceNode) secondOperand).token().source()).isEqualTo("#VAR2");
	}

	@Test
	void correctlyIncludeStringLiteralsOnNestedLevels(@ProjectName("copycodetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "WRITNEST"), ISubprogram.class);
		var firstInclude = (IIncludeNode) subprogram.body().statements().first();
		var secondInclude = (IIncludeNode) firstInclude.body().statements().first();
		var write = (IWriteNode) secondInclude.body().statements().first();

		var writeOperand = write.operands().first().operand();
		assertThat(((ILiteralNode) writeOperand).token().source()).isEqualTo("\"Text\"");
	}

	@Test
	void raiseADiagnosticForCyclomaticCopycodes(@ProjectName("copycodetests") NaturalProject project)
	{
		var subprogram = assertFileParsesAs(project.findModule("LIBONE", "INCLREC"), ISubprogram.class);
		assertThat(subprogram.diagnostics()).hasSize(1);
		assertThat(subprogram.diagnostics().first().id()).isEqualTo(ParserError.CYCLOMATIC_INCLUDE.id());
		assertThat(subprogram.diagnostics().first().message()).isEqualTo("Cyclomatic INCLUDE found. RECCC is recursively included multiple times.");
	}
}
