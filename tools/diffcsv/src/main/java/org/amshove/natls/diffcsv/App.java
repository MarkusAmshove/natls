package org.amshove.natls.diffcsv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class App
{
	public static void main(String[] args) throws IOException
	{
		if (args.length < 2)
		{
			System.out.println("Missing arguments");
			System.out.println("java -jar diffcsv.jar <path_to_base_csv> <path_to_compare_csv> [--new]");
			System.exit(1);
		}

		var arguments = Arrays.asList(args);
		var newMode = arguments.remove("--new");
		if (!newMode)
		{
			System.out.println("Running implicitly in `removed` move");
		}

		var oldFile = readFile(arguments.get(0));
		var newFile = readFile(arguments.get(1));

		var removedIssues = 0;
		if (newMode)
		{
			for (var d : newFile)
			{
				if (!oldFile.remove(d))
				{
					System.out.println(d);
					removedIssues++;
				}
			}

			System.out.println();
			System.out.println("These were all new lines that **were not present** in the old file (new diagnostics)");
			System.out.println(removedIssues + " added");
		}
		else
		{
			for (var d : oldFile)
			{
				if (!newFile.remove(d))
				{
					System.out.println(d);
					removedIssues++;
				}
			}

			System.out.println();
			System.out.println("These were all lines that were present in the old file but not the new file (fixed diagnostics)");
			System.out.println(removedIssues + " removed");
		}
	}

	private static Set<String> readFile(String path) throws IOException
	{
		return new HashSet<>(Files.readAllLines(Path.of(path)));
	}
}
