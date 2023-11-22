package org.amshove.natparse.parsing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class AdabasStatementRelatedTests extends ResourceFolderBasedTest
{
	@TestFactory
	Iterable<DynamicTest> testReferencing()
	{
		return testFolder("adabasstatements", "VIEWAMBG.NSN");
	}

	@TestFactory
	Iterable<DynamicTest> testAdabasIndexAccess()
	{
		return testFolder("adabasstatements", "ADAINDX.NSN");
	}
}
