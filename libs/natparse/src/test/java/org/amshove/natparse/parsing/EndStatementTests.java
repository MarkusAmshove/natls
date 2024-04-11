package org.amshove.natparse.parsing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class EndStatementTests extends ResourceFolderBasedTest
{
	@TestFactory
	Iterable<DynamicTest> testForEndStatement()
	{
		return testFolder("endstatement");
	}
}
