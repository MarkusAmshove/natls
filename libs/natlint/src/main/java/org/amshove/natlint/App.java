package org.amshove.natlint;

import org.amshove.natlint.cli.AnalyzeCommand;
import picocli.CommandLine;

public class App
{
	public static void main(String[] args)
	{
		System.exit(new CommandLine(new AnalyzeCommand()).execute(args));
	}
}
