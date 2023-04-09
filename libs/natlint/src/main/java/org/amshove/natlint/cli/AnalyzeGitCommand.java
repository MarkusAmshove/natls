package org.amshove.natlint.cli;

import org.amshove.natparse.natural.project.NaturalFile;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

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
			gitChanges.add(line);
		}

		var interestingGitChanges = new ArrayList<String>();
		for (var gitChange : gitChanges)
		{
			if (gitChange.startsWith("D"))
			{
				continue;
			}

			var statusLine = gitChange.trim().split(" ", 2)[1];
			if (gitChange.trim().startsWith("R"))
			{
				statusLine = statusLine.split("->", 2)[1].trim();
			}

			if (statusLine.startsWith(".."))
			{
				System.err.printf("Path seems to be relative: %s%nDid you forget to run git status with --porcelain ?%n", statusLine);
				printUsage();
				return 1;
			}

			interestingGitChanges.add(statusLine);
		}

		var filePredicates = new ArrayList<Predicate<NaturalFile>>();
		for (String interestingGitChange : interestingGitChanges)
		{
			filePredicates.add(f -> f.getProjectRelativePath().equals(Paths.get(interestingGitChange)));
		}

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
