package org.amshove.natqube.sensor;

import org.amshove.natparse.lexing.LexerError;
import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.natparse.parsing.ParserError;
import org.amshove.natqube.TestContext;
import org.amshove.testhelpers.IntegrationTest;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.sensor.issue.Issue;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SuppressWarnings("DataFlowIssue")
@IntegrationTest
class NatlintSensorShould
{
	private static TestContext testContext;
	private NatlintSensor sut;

	@BeforeAll
	static void beforeAll(@ProjectName("sensor") NaturalProject project)
	{
		testContext = TestContext.fromProject(project);
	}

	@BeforeEach
	void setUp()
	{
		sut = new NatlintSensor(testContext.sensorContext().config());
		sut.execute(testContext.sensorContext());
	}

	@Test
	@Disabled("Somehow flaky")
	void addIssuesWithAdditionalPositions()
	{
		var diagnostic = issuesFor("SUBPROG").get(0);
		assertThat(diagnostic.ruleKey().rule()).isEqualTo("NL015");

		var primaryLocation = diagnostic.primaryLocation();
		assertThat(primaryLocation.message()).isEqualTo("#PATH is used as a path for a work file and this COMPRESS leaves space between the operands. Did you mean to add LEAVING NO SPACE?");
		assertThat(primaryLocation.inputComponent().key()).isEqualTo(testContext.findInputFile("LIB", "SUBPROG").key());
		assertThat(primaryLocation.textRange().start().line()).isEqualTo(10);

		var additionalLocations = diagnostic.flows().get(0).locations();
		assertThat(additionalLocations).hasSize(1);
		var additionalLocation = additionalLocations.get(0);
		assertThat(additionalLocation.message()).isEqualTo("Variable is used here as work file path");
		assertThat(additionalLocation.textRange().start().line()).isEqualTo(12);
	}

	@Test
	void addLexerDiagnostics()
	{
		var diagnostic = issuesFor("LEXISS").get(0);
		assertThat(diagnostic.ruleKey().rule()).isEqualTo(LexerError.INVALID_STRING_LENGTH.id());

		assertThat(diagnostic.primaryLocation().textRange().start().line()).isEqualTo(6);
	}

	@Test
	@Disabled("Somehow flaky")
	void addParserDiagnostics()
	{
		var diagnostic = issuesFor("PARSEISS").stream().filter(i -> i.ruleKey().rule().startsWith("NPP")).toList().get(0);
		assertThat(diagnostic.ruleKey().rule()).isEqualTo(ParserError.UNEXPECTED_TOKEN_EXPECTED_IDENTIFIER.id());

		assertThat(diagnostic.primaryLocation().textRange().start().line()).isEqualTo(6);
	}

	private List<Issue> issuesFor(String module)
	{
		return testContext.sensorContext().allIssues().stream().filter(i -> i.primaryLocation().inputComponent().key().equals(testContext.findInputFile("LIB", module).key())).toList();
	}
}
