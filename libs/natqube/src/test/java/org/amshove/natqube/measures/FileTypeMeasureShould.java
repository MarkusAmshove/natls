package org.amshove.natqube.measures;

import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.natqube.NaturalMetrics;
import org.amshove.natqube.TestContext;
import org.amshove.testhelpers.IntegrationTest;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.measures.Metric;

@IntegrationTest
class FileTypeMeasureShould
{
	private TestContext context;

	@BeforeEach
	void setUp(@ProjectName("filetypemeasure") NaturalProject project)
	{
		context = TestContext.fromProject(project);
	}

	@Test
	void countDdms()
	{
		assertCountedMeasure("MY-DDM", NaturalMetrics.NUMBER_OF_DDMS);
	}

	@Test
	void countSubprograms()
	{
		assertCountedMeasure("SUBPROG", NaturalMetrics.NUMBER_OF_SUBPROGRAMS);
	}

	@Test
	void countPrograms()
	{
		assertCountedMeasure("PROGRAM", NaturalMetrics.NUMBER_OF_PROGRAMS);
	}

	@Test
	void countExternalSubroutines()
	{
		assertCountedMeasure("EXT-SUB", NaturalMetrics.NUMBER_OF_EXTERNAL_SUBROUTINES);
	}

	@Test
	void countHelproutines()
	{
		assertCountedMeasure("HELPR", NaturalMetrics.NUMBER_OF_HELPROUTINES);
	}

	@Test
	void countGdas()
	{
		assertCountedMeasure("MYGDA", NaturalMetrics.NUMBER_OF_GDAS);
	}

	@Test
	void countLdas()
	{
		assertCountedMeasure("MYLDA", NaturalMetrics.NUMBER_OF_LDAS);
	}

	@Test
	void countPdas()
	{
		assertCountedMeasure("MYPDA", NaturalMetrics.NUMBER_OF_PDAS);
	}

	@Test
	void countMaps()
	{
		assertCountedMeasure("MYMAP", NaturalMetrics.NUMBER_OF_MAPS);
	}

	@Test
	void countCopyCodes()
	{
		assertCountedMeasure("CCODE", NaturalMetrics.NUMBER_OF_COPYCODES);
	}

	@Test
	void countFunctions()
	{
		assertCountedMeasure("MYFUNC", NaturalMetrics.NUMBER_OF_FUNCTIONS);
	}

	private void assertCountedMeasure(String filename, Metric<Integer> metric)
	{
		var sut = new FileTypeMeasure();
		var naturalFile = context.findNaturalFile("LIB", filename);
		var inputFile = context.findInputFile("LIB", filename);

		sut.measure(context.sensorContext(), naturalFile, inputFile);

		context.assertMeasure(inputFile, metric, 1);
	}
}
