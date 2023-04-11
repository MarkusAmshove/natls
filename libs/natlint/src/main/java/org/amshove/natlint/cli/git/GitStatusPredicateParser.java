package org.amshove.natlint.cli.git;

import org.amshove.natparse.natural.project.NaturalFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class GitStatusPredicateParser
{
	public List<Predicate<NaturalFile>> parseStatusToPredicates(List<String> gitStatusLines)
	{
		var predicates = new ArrayList<Predicate<NaturalFile>>();
		for (var gitStatusLine : gitStatusLines)
		{
			var trimmedLine = gitStatusLine.trim();
			if (trimmedLine.startsWith("D"))
			{
				// can't analyze deleted files :-)
				continue;
			}

			var changedFilePath = trimmedLine.split(" ", 2)[1];
			if (trimmedLine.startsWith("R"))
			{
				changedFilePath = changedFilePath.split("->", 2)[1].trim();
			}

			var theChangedPath = changedFilePath;
			predicates.add(f -> f.getProjectRelativePath().equals(Paths.get(theChangedPath)));
		}

		return predicates;
	}
}
