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
			filesystem.streamFilesRecursively(library.getSourcePath())
				.filter(p -> NaturalFileType.isNaturalFile(p))
				.map(this::toNaturalFile)
				.forEach(library::addFile);
		}
	}

	private NaturalFile toNaturalFile(Path path)
	{
		var filetype = NaturalFileType.fromExtension(path.getFileName().toString().split("\\.")[1]);
		return new NaturalFile(getReferableName(path, filetype), path, filetype);
	}

	private String getReferableName(Path path, NaturalFileType type)
	{
		var filename = path.getFileName().toString().split("\\.")[0];
		return switch(type) {
			case SUBPROGRAM, LDA, PDA, MAP, DDM, PROGRAM -> filename;
			case SUBROUTINE -> extractSubroutineName(path);
		};
	}

	private String extractSubroutineName(Path path)
	{
		var lexemes = new Lexer().lex(filesystem.readFile(path));

		// Skip define data
		if(!lexemes.advanceAfterNext(SyntaxKind.END_DEFINE))
		{
			throw new RuntimeException("Could not find end of DEFINE DATA");
		}

		if(!lexemes.advanceAfterNext(SyntaxKind.DEFINE))
		{
			throw new RuntimeException("Could not find DEFINE SUBSROUTINE");
		}

		if(!lexemes.advanceAfterNext(SyntaxKind.IDENTIFIER_OR_KEYWORD))
		{
			throw new RuntimeException("Could not find keyword SUBROUTINE after DEFINE");
		}

		if(!lexemes.advanceUntil(SyntaxKind.IDENTIFIER_OR_KEYWORD))
		{
			throw new RuntimeException("Could not find name of subroutine");
		}

		return lexemes.peekWithInsignificant().escapedSource();
	}
}
