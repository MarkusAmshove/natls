package org.amshove.natparse.parsing;

import org.amshove.natparse.lexing.SyntaxKind;
import org.amshove.natparse.lexing.TokenList;
import org.amshove.natparse.natural.INaturalModule;
import org.amshove.natparse.natural.project.NaturalFile;

public class NaturalParser
{
	public INaturalModule parse(NaturalFile file, TokenList tokens)
	{
		var naturalModule = new NaturalModule(file);
		naturalModule.addDiagnostics(tokens.diagnostics());

		if(tokens.peek().kind() == SyntaxKind.DEFINE && tokens.peek(1).kind() == SyntaxKind.DATA)
		{
			var defineDataParser = new DefineDataParser();
			var result = defineDataParser.parse(tokens);
			naturalModule.addDiagnostics(result.diagnostics());
			var defineData = result.result();
			naturalModule.setDefineData(defineData);
		}

		return naturalModule;
	}
}
