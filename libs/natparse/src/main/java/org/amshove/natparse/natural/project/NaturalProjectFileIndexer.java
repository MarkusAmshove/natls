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
		for (var library : project.getLibraries())
		{
			var files = filesystem.streamFilesRecursively(library.getSourcePath())
				.filter(NaturalFileType::isNaturalFile)
				.map(this::toNaturalFile);

			files.forEach(library::addFile);
		}
	}

	public NaturalFile toNaturalFile(Path path)
	{
		var filetype = NaturalFileType.fromExtension(path.getFileName().toString().split("\\.")[1]);
		try
		{
			return new NaturalFile(getReferableName(path, filetype), path, filetype);
		}
		catch (Exception e)
		{
			return new NaturalFile(path, filetype, e);
		}
	}

	private String getReferableName(Path path, NaturalFileType type)
	{
		var filename = path.getFileName().toString().split("\\.")[0];
		return switch (type)
		{
			case SUBPROGRAM, DDM, LDA, PDA, GDA, PROGRAM, COPYCODE, MAP, HELPROUTINE -> filename;
			case SUBROUTINE -> extractSubroutineName(path);
			case FUNCTION -> extractFunctionName(path);
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

	private String extractFunctionName(Path path)
	{
		var lexemes = new Lexer().lex(filesystem.readFile(path), path);
		if (!lexemes.advanceAfterNextIfFound(SyntaxKind.FUNCTION))
		{
			throw new RuntimeException("Could not find DEFINE FUNCTION");
		}

		return lexemes.peek().symbolName();
	}
}
