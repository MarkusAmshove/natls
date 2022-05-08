package org.amshove.natls;

import org.amshove.natls.languageserver.NaturalLanguageServer;
import org.eclipse.lsp4j.launch.LSPLauncher;

import java.nio.file.Path;
import java.util.Arrays;

public class App
{
	private static final String APP_NAME = App.class.getPackage().getImplementationTitle();
	private static final String APP_VERSION = App.class.getPackage().getImplementationVersion();

	public static void main(String[] args)
	{
		var arguments = Arrays.stream(args).toList();
		if(arguments.size() == 2 && arguments.contains("--package"))
		{
			new DiagramGenerator().run(Path.of(arguments.get(1)));
			return;
		}

		if(args.length > 0)
		{
			System.out.printf("%s - Version %s%n", APP_NAME, APP_VERSION);
			return;
		}

		var server = new NaturalLanguageServer();
		var launcher = LSPLauncher.createServerLauncher(server, System.in, System.out);
		server.connect(launcher.getRemoteProxy());
		launcher.startListening();
	}
}
