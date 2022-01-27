package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;

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

		if (tokens.peek().kind() == SyntaxKind.DEFINE && tokens.peek(1).kind() == SyntaxKind.DATA)
		{
			var defineDataParser = new DefineDataParser(moduleProviderToUse);
			var result = defineDataParser.parse(tokens);
			naturalModule.addDiagnostics(result.diagnostics());
			var defineData = result.result();
			naturalModule.setDefineData(defineData);
		}

		if (file.getFiletype().hasBody())
		{
			var statementParser = new StatementListParser(moduleProviderToUse);
			var result = statementParser.parse(tokens);
			naturalModule.addDiagnostics(result.diagnostics());
			naturalModule.setBody(result.result());
			resolveReferences(statementParser, naturalModule);
		}

		return naturalModule;
	}

	private void resolveReferences(StatementListParser statementParser, NaturalModule module)
	{
		// This could actually be done in the StatementListParser when encountering
		// a possible reference. But that would need changes in the architecture, since
		// it does not know about declared variables.

		var defineData = module.defineData();
		if (defineData == null || defineData.variables().isEmpty())
		{
			return;
		}

		for (var unresolvedReference : statementParser.getUnresolvedReferences())
		{
			var variable = defineData.findVariable(unresolvedReference.token().symbolName());
			if (variable != null)
			{
				((VariableNode) variable).addReference((SymbolReferenceNode) unresolvedReference);
			}
			else
			{
				// It's currently okay to not find a variable, as long as keywords might be IDENTIFIER_OR_KEYWORD
				// This should add diagnostics in the future.
			}
		}
	}
}
