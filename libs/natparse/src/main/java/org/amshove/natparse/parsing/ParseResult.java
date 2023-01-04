package org.amshove.natparse.parsing;

import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.ReadOnlyList;

public record ParseResult<T> (T result, ReadOnlyList<IDiagnostic> diagnostics)
{}
