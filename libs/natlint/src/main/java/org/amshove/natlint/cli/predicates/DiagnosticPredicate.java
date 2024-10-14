package org.amshove.natlint.cli.predicates;

import org.amshove.natparse.IDiagnostic;

import java.util.function.Predicate;

public record DiagnosticPredicate(String description, Predicate<IDiagnostic> predicate)
{}
