package org.amshove.natlint;

import org.amshove.natlint.cli.AnalyzeCommand;
import org.amshove.natlint.cli.AnalyzeGitCommand;
import picocli.CommandLine;

import java.io.IOException;

public class App
{
	public static void main(String[] args) throws IOException
	{
		System.exit(new CommandLine(new AnalyzeCommand()).addSubcommand(new AnalyzeGitCommand()).execute(args));
	}
}
