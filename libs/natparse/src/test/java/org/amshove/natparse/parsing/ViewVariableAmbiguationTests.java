package org.amshove.natparse.parsing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class ViewVariableAmbiguationTests extends ResourceFolderBasedTest
{
	@TestFactory
	Iterable<DynamicTest> testReferencing()
	{
		return testFolder("viewvariableambiguation");
	}
}
