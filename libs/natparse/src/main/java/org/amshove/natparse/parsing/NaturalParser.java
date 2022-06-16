package org.amshove.natparse.parsing;

import org.amshove.natparse.ReadOnlyList;
import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.*;
import org.amshove.natparse.natural.project.NaturalFile;
import org.amshove.natparse.natural.project.NaturalFileType;

import java.util.ArrayList;

public class NaturalParser
{
	private final IModuleProvider moduleProvider;

	public NaturalParser()
	{
		this(null);
	}

	public NaturalParser(IModuleProvider moduleProvider)
	{
		this.moduleProvider = moduleProvider;
	}

	public INaturalModule parse(NaturalFile file, TokenList tokens)
	{
		var moduleProviderToUse = moduleProvider;
		if (moduleProviderToUse == null)
		{
			// The caching module provider uses a static field to cache, so
			// we can instantiate it new for every module to be parsed.
			// In fact, currently it has to be instantiated every time, because
			// it saves the file to get the library.
			moduleProviderToUse = new CachingModuleProvider(file);
		}

		var naturalModule = new NaturalModule(file);
		naturalModule.addDiagnostics(tokens.diagnostics());
		var topLevelNodes = new ArrayList<ISyntaxNode>();

		if (tokens.peek().kind() == SyntaxKind.DEFINE && tokens.peek(1).kind() == SyntaxKind.DATA)
		{
			var defineDataParser = new DefineDataParser(moduleProviderToUse);
			var result = defineDataParser.parse(tokens);
			naturalModule.addDiagnostics(result.diagnostics());
			var defineData = result.result();
			if(defineData != null)
			{
				naturalModule.setDefineData(defineData);
				topLevelNodes.add(defineData);
				naturalModule.addReferencableNodes(defineData.variables().stream().map(n -> (IReferencableNode) n).toList());
			}
		}

		if (file.getFiletype().hasBody())
		{
			var statementParser = new StatementListParser(moduleProviderToUse);
			var result = statementParser.parse(tokens);
			naturalModule.addReferencableNodes(statementParser.getReferencableNodes());
			addRelevantParserDiagnostics(naturalModule, result);
			naturalModule.setBody(result.result());

			new ReferenceResolver().resolveReferences(naturalModule);
			topLevelNodes.add(result.result());
		}

		naturalModule.setSyntaxTree(SyntaxTree.create(ReadOnlyList.from(topLevelNodes)));

		return naturalModule;
	}

	private void addRelevantParserDiagnostics(NaturalModule naturalModule, ParseResult<IStatementListNode> result)
	{
		for (var diagnostic : result.diagnostics())
		{
			if(diagnostic.id().equals(ParserError.UNRESOLVED_IMPORT.id()))
			{
				if(naturalModule.isTestCase() && diagnostic.message().contains("module TEARDOWN") || diagnostic.message().contains("module SETUP"))
				{
					// Skip these unresolved subroutines.
					// These are special cases for NatUnit, because it doesn't force you to implement them.
					// It however calls them if they're present.
					continue;
				}
			}

			if(naturalModule.file().getFiletype() == NaturalFileType.COPYCODE)
			{
				if(ParserError.isUnresolvedError(diagnostic.id()))
				{
					// When parsing a copycode we don't want to report any unresolved references, because we simply don't know
					// if they are declared where the copycode is used.
					// They do however get reported in the module including the copycode.
					continue;
				}
			}

			naturalModule.addDiagnostics(result.diagnostics());
		}
	}
}
