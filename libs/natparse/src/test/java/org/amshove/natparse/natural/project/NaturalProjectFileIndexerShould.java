package org.amshove.natparse.natural.project;

import org.amshove.natparse.parsing.ParserIntegrationTest;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class NaturalProjectFileIndexerShould extends ParserIntegrationTest
{
	private NaturalProject project;

	@BeforeEach
	void setUp(@ProjectName("indexing") NaturalProject project)
	{
		this.project = project;
	}

	@Test
	void indexCopyCodes()
	{
		assertCanFindModule("CCODE", NaturalFileType.COPYCODE);
	}

	@Test
	void indexExternalSubroutines()
	{
		assertCanFindModule("EXTERNAL-SUBROUTINE", "EXTSUB", NaturalFileType.SUBROUTINE);
	}

	@Test
	void indexExternalSubroutinesWithNoDefineDataAndNoSubroutineKeyword()
	{
		assertCanFindModule("SPECIAL-EXTERNAL", "SUBNODDD", NaturalFileType.SUBROUTINE);
	}

	@Test
	void indexFunctions()
	{
		assertCanFindModule("MYFUNC", "FUNC", NaturalFileType.FUNCTION);
	}

	@Test
	void indexHelpRoutines()
	{
		assertCanFindModule("HELPR", NaturalFileType.HELPROUTINE);
	}

	@Test
	void indexDdms()
	{
		assertCanFindModule("MY-DDM", NaturalFileType.DDM);
	}

	@Test
	void indexLdas()
	{
		assertCanFindModule("MYLDA", NaturalFileType.LDA);
	}

	@Test
	void indexMaps()
	{
		assertCanFindModule("MYMAP", NaturalFileType.MAP);
	}

	@Test
	void indexProgams()
	{
		assertCanFindModule("PROG", NaturalFileType.PROGRAM);
	}

	@Test
	void indexSubprograms()
	{
		assertCanFindModule("SUBPROG", NaturalFileType.SUBPROGRAM);
	}

	private void assertCanFindModule(String referableName, NaturalFileType type)
	{
		assertCanFindModule(referableName, referableName, type);
	}

	private void assertCanFindModule(String referableName, String filename, NaturalFileType type)
	{
		var module = project.findModule("LIB", referableName);
		assertThat(module).as("Module not found").isNotNull();
		assertThat(Objects.requireNonNull(module).getFilenameWithoutExtension()).isEqualTo(filename);
		assertThat(Objects.requireNonNull(module).getFiletype()).isEqualTo(type);
	}
}
