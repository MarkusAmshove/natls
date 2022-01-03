package org.amshove.natls.natunit;

import java.util.ArrayList;
import java.util.List;

public class NatUnitResult
{
	private final List<NatUnitTestResult> testResults = new ArrayList<>();

	void addTestResult(NatUnitTestResult natUnitTestResult)
	{
		testResults.add(natUnitTestResult);
	}

	public List<NatUnitTestResult> getTestResults()
	{
		return testResults;
	}
}
