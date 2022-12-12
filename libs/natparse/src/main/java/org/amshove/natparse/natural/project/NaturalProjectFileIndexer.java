package org.amshove.natparse.natural.project;

import java.nio.file.Path;

import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.infrastructure.IFilesystem;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.TokenList;

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
				.filter(NaturalFileType::isNaturalFile)
				.map(this::toNaturalFile)
				.filter(f -> f.isStructured() && !f.isFailedOnInit())
				.forEach(library::addFile);
		}
	}

	public NaturalFile toNaturalFile(Path path, NaturalLibrary library)
	{
		var lexemes = new Lexer().lex(filesystem.readFile(path), path);
		var filetype = NaturalFileType.fromExtension(path.getFileName().toString().split("\\.")[1]);
		try {
			return new NaturalFile(getReferableName(path, filetype, lexemes), path, filetype, lexemes.sourceHeader());
		} catch (Exception e) {
			return new NaturalFile(path, filetype);
		}
	}

	private NaturalFile toNaturalFile(Path path)
	{
		return toNaturalFile(path, null);
	}

	private String getReferableName(Path path, NaturalFileType type, TokenList lexemes)
	{
		// Lexing everything now to get hold of the NaturalHeader for each file
		var filename = path.getFileName().toString().split("\\.")[0];
		return switch(type) {
			case SUBPROGRAM, DDM, LDA, PDA, GDA, PROGRAM, FUNCTION, COPYCODE, MAP, HELPROUTINE -> filename;
			case SUBROUTINE -> extractSubroutineName(path, lexemes);
		};
	}

	private String extractSubroutineName(Path path, TokenList lexemes)
	{
		// Advance directly past the subroutine name, if possible
		if (!lexemes.advanceAfterNextIfFound(SyntaxKind.SUBROUTINE))
		{
			// else advance to END-DEFINE if it exists
			lexemes.advanceAfterNextIfFound(SyntaxKind.END_DEFINE);
			// After this, the first DEFINE token MUST be from the DEFINE [SUBROUTINE]
			if (!lexemes.advanceAfterNext(SyntaxKind.DEFINE)) {
				throw new RuntimeException("Could not find DEFINE SUBSROUTINE");
			}
		}

		return lexemes.peek().symbolName();
	}
}
