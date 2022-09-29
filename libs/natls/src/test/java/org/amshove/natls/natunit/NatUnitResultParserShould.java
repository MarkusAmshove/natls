package org.amshove.natls.natunit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NatUnitResultParserShould
{
	@Test
	void parseAResult(@TempDir Path tempDirectory) throws IOException
	{
		var resultFile = tempDirectory.resolve("result.xml");
		Files.writeString(resultFile, """
<?xml version='1.0' encoding='ISO-8859-1'?>
<testsuite name='LIB.TCABC' tests='1' failures='0' errors='0' skipped='0' time='000.100'>
<testcase classname='ACC.TCABC' name='This test passed' time='000.000' />
<testcase classname='ACC.TCABC' name='This test failed' time='000.000'>
<failure message="A failure message" />
</testcase>
<testcase classname='ACC.TCABC' name='This test skipped' time='000.000'>
<skipped />
</testcase>
</testsuite>
			""");

		var sut = new NatUnitResultParser();
		var result = sut.parse(resultFile);

		assertThat(result.getTestResults().get(0).hasFailed()).isFalse();
		assertThat(result.getTestResults().get(0).name()).isEqualTo("This test passed");
		assertThat(result.getTestResults().get(0).message()).isEmpty();

		assertThat(result.getTestResults().get(1).hasFailed()).isTrue();
		assertThat(result.getTestResults().get(1).name()).isEqualTo("This test failed");
		assertThat(result.getTestResults().get(1).message()).isEqualTo("A failure message");

		assertThat(result.getTestResults().get(2).hasFailed()).isFalse();
		assertThat(result.getTestResults().get(2).name()).isEqualTo("This test skipped");
		assertThat(result.getTestResults().get(2).message()).isEmpty();
	}

	@Test
	void returnAnEmptyResultOnError(@TempDir Path tempDirectory) throws IOException
	{
		var resultFile = tempDirectory.resolve("result.xml");
		Files.writeString(resultFile, "<?xml version='1.0' encoding='ISO-");

		var sut = new NatUnitResultParser();
		var result = sut.parse(resultFile);

		assertThat(result.getTestResults()).isEmpty();
	}
}
