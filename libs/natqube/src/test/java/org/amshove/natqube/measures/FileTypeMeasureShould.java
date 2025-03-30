package org.amshove.natqube.measures;

import org.amshove.natqube.NaturalMetrics;
import org.amshove.natqube.SonarQubeTest;
import org.junit.jupiter.api.Test;

class FileTypeMeasureShould extends SonarQubeTest
{
	private final FileTypeMeasure sut = new FileTypeMeasure();

	@Test
	void countDdms()
	{
		var file = addNaturalFile("MYDDM.NSD", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_DDMS, 1, file);
	}

	@Test
	void countSubprograms()
	{
		var file = addNaturalFile("MYDDM.NSN", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_SUBPROGRAMS, 1, file);
	}

	@Test
	void countPrograms()
	{
		var file = addNaturalFile("MYDDM.NSP", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_PROGRAMS, 1, file);
	}

	@Test
	void countExternalSubroutines()
	{
		var file = addNaturalFile("EXT.NSS", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_EXTERNAL_SUBROUTINES, 1, file);
	}

	@Test
	void countHelpRoutines()
	{
		var file = addNaturalFile("HELP.NSH", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_HELPROUTINES, 1, file);
	}

	@Test
	void countGdas()
	{
		var file = addNaturalFile("MYGDA.NSG", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_GDAS, 1, file);
	}

	@Test
	void countLdas()
	{
		var file = addNaturalFile("LALDA.NSL", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_LDAS, 1, file);
	}

	@Test
	void countPdas()
	{
		var file = addNaturalFile("UNEPDA.NSA", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_PDAS, 1, file);
	}

	@Test
	void countMaps()
	{
		var file = addNaturalFile("MAPPI.NSM", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_MAPS, 1, file);
	}

	@Test
	void countCopycodes()
	{
		var file = addNaturalFile("COPY.NSC", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_COPYCODES, 1, file);
	}

	@Test
	void countFunctions()
	{
		var file = addNaturalFile("FUNC.NS7", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_FUNCTIONS, 1, file);
	}

	@Test
	void countClasses()
	{
		var file = addNaturalFile("CLASS.NS4", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_CLASSES, 1, file);
	}

	@Test
	void countTextFiles()
	{
		var file = addNaturalFile("TEXT.NST", "");

		sut.measure(context, file);
		assertMetric(NaturalMetrics.NUMBER_OF_TEXT_FILES, 1, file);
	}

}
