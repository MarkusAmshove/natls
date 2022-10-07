package org.amshove.natparse.parsing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class DefineDataParserDiagnosticTest extends ResourceFolderBasedTest
{
	@TestFactory
	Iterable<DynamicTest> diagnosticTests()
	{
		return testFolder("definedatadiagnostics");
	}
}
