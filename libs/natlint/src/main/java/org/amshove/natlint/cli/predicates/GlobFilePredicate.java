package org.amshove.natlint.cli.predicates;

import org.amshove.natparse.natural.project.NaturalFile;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.function.Predicate;

public class GlobFilePredicate implements IFilePredicate
{
	private final String glob;
	private final PathMatcher matcher;

	public GlobFilePredicate(String glob)
	{
		this.glob = glob;
		matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
	}

	@Override
	public String description()
	{
		return "glob: " + glob;
	}

	@Override
	public Predicate<NaturalFile> predicate()
	{
		return f -> matcher.matches(f.getPath());
	}
}
