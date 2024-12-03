package org.amshove.natlint.cli.predicates;

import org.amshove.natparse.natural.project.NaturalFile;

import java.util.function.Predicate;

public interface IFilePredicate
{
	String description();

	Predicate<NaturalFile> predicate();
}
