package org.amshove.natls.codelens;

import org.amshove.natls.CustomCommands;
import org.amshove.natls.languageserver.LspUtil;
import org.amshove.natls.project.LanguageServerFile;
import org.amshove.natls.project.ModuleReferenceCache;
import org.amshove.natparse.IPosition;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class NatUnitCodeLensProvider implements ICodeLensProvider
{
	@Override
	public List<CodeLens> provideCodeLens(LanguageServerFile file)
	{
		var parsedCallingTestCases = file.getIncomingReferences().stream()
			.map(LanguageServerFile::getPath)
			.filter(NatUnitCodeLensProvider::isTestCase);
		var cachedCallingTestCases = ModuleReferenceCache.retrieveCachedPositions(file).stream()
			.map(IPosition::filePath)
			.filter(NatUnitCodeLensProvider::isTestCase);

		var totalTestCases = Stream.concat(parsedCallingTestCases, cachedCallingTestCases).distinct().count();
		if (totalTestCases == 0)
		{
			return List.of();
		}

		var firstNodeRange = LspUtil.toRange(file.module().syntaxTree().descendants().first().position());
		var label = totalTestCases > 1 ? "testcases" : "testcase";
		return List.of(
			new CodeLens(
				firstNodeRange,
				new Command(
					"$(beaker) %d %s".formatted(totalTestCases, label),
					CustomCommands.CODELENS_SHOW_TESTS,
					Arrays.asList(file.getUri(), firstNodeRange)
				),
				null
			)
		);
	}

	private static boolean isTestCase(Path path)
	{
		var filename = path.getFileName().toString();
		return filename.startsWith("TC") && filename.endsWith(".NSN");
	}
}
