package org.amshove.natlint;

import org.amshove.natlint.cli.AnalyzeCommand;
import picocli.CommandLine;

import java.io.IOException;

public class App
{
	public static void main(String[] args) throws IOException
	{
		System.exit(new CommandLine(new AnalyzeCommand()).execute(args));
	}
}
