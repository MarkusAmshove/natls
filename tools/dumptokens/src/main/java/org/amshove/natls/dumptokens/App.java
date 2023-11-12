package org.amshove.natls.dumptokens;

import org.amshove.natparse.infrastructure.ActualFilesystem;
import org.amshove.natparse.lexing.CopyCodeResolver;
import org.amshove.natparse.lexing.Lexer;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.project.NaturalProjectFileIndexer;
import org.amshove.natparse.parsing.DefaultModuleProvider;
import org.amshove.natparse.parsing.project.BuildFileProjectReader;

import java.nio.file.Path;

public class App
{
	private static final ActualFilesystem fs = new ActualFilesystem();
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			System.err.println("Path to Natural file expected");
			return;
		}

		var filePath = Path.of(args[0]);
		var projectFile = findProjectFile(filePath);
		var project = new BuildFileProjectReader().getNaturalProject(projectFile);
		new NaturalProjectFileIndexer().indexProject(project);
		var naturalFile = project.findModule(filePath);
		var resolver = new CopyCodeResolver();
		var lexer = new Lexer();
		var tokens = lexer.lex(fs.readFile(filePath), filePath);
		var resolvedTokens = resolver.resolve(tokens, new DefaultModuleProvider(naturalFile));
		printTokens(resolvedTokens);
	}

	private static void printTokens(TokenList tokens)
	{
		var previousPath = tokens.peek().filePath();
		var previousLine = tokens.peek().line();
		while (!tokens.isAtEnd())
		{
			var token = tokens.advance();
			if (!previousPath.equals(token.filePath()) || previousLine != token.line())
			{
				System.out.print(System.lineSeparator());
			}
			previousPath = token.filePath();
			previousLine = token.line();
			System.out.print(token.source() + " ");
		}
	}

	private static Path findProjectFile(Path startPath)
	{
		if (!startPath.toFile().isDirectory())
		{
			startPath = startPath.getParent();
		}

		var folder = startPath;
		return fs.findNaturalProjectFile(startPath).orElseGet(() -> findProjectFile(folder.getParent()));
	}
}
