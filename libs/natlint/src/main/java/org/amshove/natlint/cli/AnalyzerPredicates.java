package org.amshove.natlint.cli;

import org.amshove.natlint.cli.predicates.DiagnosticPredicate;
import org.amshove.natlint.cli.predicates.IFilePredicate;
import org.amshove.natparse.DiagnosticSeverity;
import org.amshove.natparse.IDiagnostic;
import org.amshove.natparse.natural.project.NaturalFile;

import java.util.ArrayList;
import java.util.List;

public class AnalyzerPredicates
{
	private final List<IFilePredicate> filePredicates = new ArrayList<>();
	private final List<DiagnosticPredicate> diagnosticPredicates = new ArrayList<>();
	private final DiagnosticSeverity minimumSeverity;

	public AnalyzerPredicates(DiagnosticSeverity minimumSeverity)
	{
		this.minimumSeverity = minimumSeverity;
	}

	void addFilePredicate(IFilePredicate predicate)
	{
		filePredicates.add(predicate);
	}

	void addDiagnosticPredicate(DiagnosticPredicate predicate)
	{
		diagnosticPredicates.add(predicate);
	}

	public boolean shouldAnalyzeFile(NaturalFile file)
	{
		return filePredicates.isEmpty() || filePredicates.stream().anyMatch(p -> p.predicate().test(file));
	}

	public boolean shouldPrintDiagnostic(IDiagnostic diagnostic)
	{
		if (!diagnostic.severity().isWorseOrEqualTo(minimumSeverity))
		{
			return false;
		}

		return diagnosticPredicates.isEmpty() || diagnosticPredicates.stream().anyMatch(p -> p.predicate().test(diagnostic));
	}

	public void printSettings()
	{
		var hasPredicates = minimumSeverity != DiagnosticSeverity.INFO
			|| !filePredicates.isEmpty()
			|| !diagnosticPredicates.isEmpty();

		if (!hasPredicates)
		{
			return;
		}

		if (!filePredicates.isEmpty())
		{
			System.out.println("Only the following files will be analyzed:");
			filePredicates.forEach(fp -> System.out.println("- " + fp.description()));
		}

		System.out.println("Only the following diagnostics will be printed:");
		System.out.printf("- minimum severity %s%n", minimumSeverity);
		diagnosticPredicates.forEach(dp -> System.out.println("- " + dp.description()));

		System.out.println();
	}
}
