package org.amshove.natparse.parsing;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class TypingTests extends ResourceFolderBasedTest
{
	@TestFactory
	Iterable<DynamicTest> testTypings()
	{
		return testFolder("typing");
	}
}
