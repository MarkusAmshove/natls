package org.amshove.natparse.parsing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class DbmsTests extends ResourceFolderBasedTest
{
	@TestFactory
	Iterable<DynamicTest> testForDbms()
	{
		return testFolder("regressiontests");
	}
}
