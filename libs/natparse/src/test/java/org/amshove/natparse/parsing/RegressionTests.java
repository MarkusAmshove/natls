package org.amshove.natparse.parsing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class RegressionTests extends ResourceFolderBasedTest
{
	@TestFactory
	Iterable<DynamicTest> testForRegressions()
	{
		return testFolder("regressiontests");
	}
}
