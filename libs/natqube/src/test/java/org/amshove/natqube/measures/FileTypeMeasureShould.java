package org.amshove.natqube.measures;

import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.natqube.NaturalMetrics;
import org.amshove.natqube.TestContext;
import org.amshove.testhelpers.IntegrationTest;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.measures.Metric;
import org.sonar.api.testfixtures.measure.TestComponent;
import org.sonar.api.testfixtures.measure.TestMeasureComputerContext;
import org.sonar.api.testfixtures.measure.TestMeasureComputerDefinitionContext;
import org.sonar.api.testfixtures.measure.TestSettings;

import java.util.Objects;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

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

	@Test
	void aggregateMeasuresOnFolderLevel()
	{
		var aggregator = new AggregateFileTypeMeasure();

		var context = new TestMeasureComputerContext(
			new TestComponent(TestContext.MODULE_KEY + ":Natural-Libraries/LIB", Component.Type.DIRECTORY, null),
			new TestSettings(),
			aggregator.define(new TestMeasureComputerDefinitionContext())
		);
		context.addChildrenMeasures(NaturalMetrics.NUMBER_OF_COPYCODES.key(), 10, 5);
		context.addChildrenMeasures(NaturalMetrics.NUMBER_OF_SUBPROGRAMS.key(), 2, 2, 3);

		aggregator.compute(context);
		assertThat(Objects.requireNonNull(context.getMeasure(NaturalMetrics.NUMBER_OF_COPYCODES.key())).getIntValue()).isEqualTo(15);
		assertThat(Objects.requireNonNull(context.getMeasure(NaturalMetrics.NUMBER_OF_SUBPROGRAMS.key())).getIntValue()).isEqualTo(7);
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
