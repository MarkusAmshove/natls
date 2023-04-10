package org.amshove.natlint.cli;

import org.amshove.natlint.cli.git.GitStatusPredicateParser;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "git-status", description = "Analyze files from `git status --porcelain`", mixinStandardHelpOptions = true)
public class AnalyzeGitCommand implements Callable<Integer>
{
	@Override
	public Integer call() throws Exception
	{
		var br = new BufferedReader(new InputStreamReader(System.in));
		var gitChanges = new ArrayList<String>();
		var line = "";
		while ((line = br.readLine()) != null)
		{
			if (line.startsWith("On branch"))
			{
				System.err.printf("Unexpected line: %s%nDid you forget to run git status with --porcelain ?%n", line);
				printUsage();
				return 1;
			}

			if (line.contains(".."))
			{
				System.err.printf("Path seems to be relative: %s%nDid you forget to run git status with --porcelain ?%n", line);
				printUsage();
				return 1;
			}

			gitChanges.add(line);
		}

		var filePredicates = new GitStatusPredicateParser().parseStatusToPredicates(gitChanges);
		return new CliAnalyzer(
			Paths.get(System.getProperty("user.dir")),
			DiagnosticSinkType.STDOUT.createSink(),
			FileStatusSink.dummy(),
			filePredicates,
			List.of(d -> true),
			false
		).run();
	}

	private static void printUsage()
	{
		System.out.println();
		System.out.println("Usage:");
		System.out.println("git status --porcelain | java -jar natlint.jar git-status");
	}
}
