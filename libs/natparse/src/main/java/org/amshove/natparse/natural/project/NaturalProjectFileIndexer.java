package org.amshove.natparse.natural.project;

import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;

import java.nio.file.Path;

public class NaturalProjectFileIndexer
{
	private final IFilesystem filesystem;

	public NaturalProjectFileIndexer()
	{
		this(new ActualFilesystem());
	}

	public NaturalProjectFileIndexer(IFilesystem filesystem)
	{
		this.filesystem = filesystem;
	}

	public void indexProject(NaturalProject project)
	{
		indexProject(project, false);
	}

	public void indexProject(NaturalProject project, boolean skipFiltering)
	{
		for (var library : project.getLibraries())
		{
			var files = filesystem.streamFilesRecursively(library.getSourcePath())
				.filter(NaturalFileType::isNaturalFile)
				.map(this::toNaturalFile);

			if (!skipFiltering)
			{
				files = files.filter(f -> !f.isReporting() && !f.isFailedOnInit());
			}

			files.forEach(library::addFile);
		}
	}

	public NaturalFile toNaturalFile(Path path, NaturalLibrary library)
	{
		var filetype = NaturalFileType.fromExtension(path.getFileName().toString().split("\\.")[1]);
		var mode = extractProgrammingMode(path);
		var header = new NaturalHeader(mode, 1);
		try
		{
			return new NaturalFile(getReferableName(path, filetype), path, filetype, header);
		}
		catch (Exception e)
		{
			return new NaturalFile(path, filetype, e);
		}
	}

	private NaturalProgrammingMode extractProgrammingMode(Path path)
	{
		return filesystem.peekFile(path)
			.filter(l -> l.startsWith("* :Mode"))
			.findAny()
			.map(l -> NaturalProgrammingMode.fromString(l.replace("* :Mode", "").trim()))
			.orElse(NaturalProgrammingMode.UNKNOWN);
	}

	private NaturalFile toNaturalFile(Path path)
	{
		return toNaturalFile(path, null);
	}

	private String getReferableName(Path path, NaturalFileType type)
	{
		var filename = path.getFileName().toString().split("\\.")[0];
		return switch (type)
		{
			case SUBPROGRAM, DDM, LDA, PDA, GDA, PROGRAM, FUNCTION, COPYCODE, MAP, HELPROUTINE -> filename;
			case SUBROUTINE -> extractSubroutineName(path);
		};
	}

	private String extractSubroutineName(Path path)
	{
		var lexemes = new Lexer().lex(filesystem.readFile(path), path);
		// Advance directly past the subroutine name, if possible
		if (!lexemes.advanceAfterNextIfFound(SyntaxKind.SUBROUTINE))
		{
			// else advance to END-DEFINE if it exists
			lexemes.advanceAfterNextIfFound(SyntaxKind.END_DEFINE);
			// After this, the first DEFINE token MUST be from the DEFINE [SUBROUTINE]
			if (!lexemes.advanceAfterNext(SyntaxKind.DEFINE))
			{
				throw new RuntimeException("Could not find DEFINE SUBSROUTINE");
			}
		}

		return lexemes.peek().symbolName();
	}
}
