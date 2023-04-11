package org.amshove.natlint.cli;

import org.amshove.natparse.natural.project.NaturalProject;
import org.amshove.testhelpers.ProjectName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class NatlintCliShould extends CliTest
{
	@Test
	void returnNonZeroExitCodeWhenDiagnosticsAreFound(@ProjectName("clitest") NaturalProject project)
	{
		var result = runNatlint("-w", project.getRootPath().toAbsolutePath().toString());
		assertThat(result.exitCode()).isPositive();
	}

	@Test
	void printAnErrorWhenGitStatusIsNotWithPorcelain(@ProjectName("clitest") NaturalProject project)
	{
		setStdIn("On branch main");
		var result = runNatlint("-w", project.getRootPath().toAbsolutePath().toString(), "git-status");
		assertThat(result.exitCode()).isPositive();
		assertThat(result.stdErr()).isNotEmpty();
	}

	@Test
	void printAnErrorWhenGitStatusContainsRelativePaths(@ProjectName("clitest") NaturalProject project)
	{
		setStdIn("M ../.editorconfig");
		var result = runNatlint("-w", project.getRootPath().toAbsolutePath().toString(), "git-status");
		assertThat(result.exitCode()).isPositive();
		assertThat(result.stdErr()).isNotEmpty();
	}

	@Test
	void returnNonZeroExitCodeWhenRunWithGitStatusOnFilesThatRaiseDiagnostics(@ProjectName("clitest") NaturalProject project)
	{
		setStdIn(" M Natural-Libraries/LIBONE/SUBONE.NSN");
		var result = runNatlint("-w", project.getRootPath().toAbsolutePath().toString(), "git-status");
		assertThat(result.exitCode()).isPositive();
		assertThat(result.stdErr()).isEmpty(); // no user error raised
	}
}
